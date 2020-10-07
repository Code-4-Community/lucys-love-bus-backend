package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.CHILDREN;
import static org.jooq.generated.Tables.CONTACTS;
import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;
import static org.jooq.generated.Tables.USERS;

import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dataaccess.EventDatabaseOperations;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import com.codeforcommunity.dto.userEvents.components.RSVP;
import com.codeforcommunity.dto.userEvents.components.Registration;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.requests.GetUserEventsRequest;
import com.codeforcommunity.dto.userEvents.requests.ModifyEventRequest;
import com.codeforcommunity.dto.userEvents.responses.EventRegistrations;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.BadRequestImageException;
import com.codeforcommunity.exceptions.EventDoesNotExistException;
import com.codeforcommunity.exceptions.InvalidEventCapacityException;
import com.codeforcommunity.exceptions.S3FailedUploadException;
import com.codeforcommunity.requester.S3Requester;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSeekStep1;
import org.jooq.generated.tables.pojos.Events;
import org.jooq.generated.tables.records.EventsRecord;

public class EventsProcessorImpl implements IEventsProcessor {

  protected final DSLContext db;
  protected final EventDatabaseOperations eventDatabaseOperations;

  /** Hours after the start of an event that the event will still show on upcoming pages */
  protected final int registerLeniencyHours = 12;
  /** The number of days before an event a GP can register */
  protected final int daysGpCanRegister = 5;

  public EventsProcessorImpl(DSLContext db) {
    this.db = db;
    this.eventDatabaseOperations = new EventDatabaseOperations(db);
  }

  @Override
  public SingleEventResponse getSingleEvent(int eventId, JWTData userData) {
    Events event = db.selectFrom(EVENTS).where(EVENTS.ID.eq(eventId)).fetchOneInto(Events.class);

    if (event == null) {
      throw new EventDoesNotExistException(eventId);
    }

    return eventPojoToResponse(event, userData);
  }

  @Override
  public GetEventsResponse getEvents(List<Integer> eventIds, JWTData userData) {
    List<Events> e = db.selectFrom(EVENTS).where(EVENTS.ID.in(eventIds)).fetchInto(Events.class);
    return new GetEventsResponse(listOfEventsToListOfSingleEventResponse(e, userData), e.size());
  }

  @Override
  public GetEventsResponse getEventsSignedUp(GetUserEventsRequest request, JWTData userData) {

    SelectConditionStep<Record> q =
        db.select(EVENTS.fields())
            .from(
                USERS
                    .join(EVENT_REGISTRATIONS)
                    .onKey(EVENT_REGISTRATIONS.USER_ID)
                    .join(EVENTS)
                    .onKey(EVENT_REGISTRATIONS.EVENT_ID))
            .where(USERS.ID.eq(userData.getUserId()));

    SelectConditionStep<Record> afterDateFilter = q;

    if (request.getEndDate().isPresent()) {
      if (request.getStartDate().isPresent()) {
        afterDateFilter =
            q.and(
                EVENTS.START_TIME.between(
                    request.getStartDate().get(), request.getEndDate().get()));
      } else {
        afterDateFilter = q.and(EVENTS.START_TIME.lessOrEqual(request.getEndDate().get()));
      }
    } else {
      if (request.getStartDate().isPresent()) {
        afterDateFilter = q.and(EVENTS.START_TIME.greaterOrEqual(request.getStartDate().get()));
      }
    }

    SelectSeekStep1<Record, Timestamp> s = afterDateFilter.orderBy(EVENTS.START_TIME.asc());
    List<Events> eventPojos;

    if (request.getCount().isPresent()) {
      eventPojos = s.limit(request.getCount().get()).fetchInto(Events.class);
    } else {
      eventPojos = s.fetchInto(Events.class);
    }

    List<SingleEventResponse> res = listOfEventsToListOfSingleEventResponse(eventPojos, userData);
    return new GetEventsResponse(res, res.size());
  }

  @Override
  public GetEventsResponse getEventsQualified(JWTData userData) {
    Timestamp startDate =
        Timestamp.from(Instant.now().minus(registerLeniencyHours, ChronoUnit.HOURS));

    List<Events> eventsList =
        db.selectFrom(EVENTS)
            .where(EVENTS.START_TIME.greaterOrEqual(startDate))
            .orderBy(EVENTS.START_TIME.asc())
            .fetchInto(Events.class);

    List<SingleEventResponse> res = listOfEventsToListOfSingleEventResponse(eventsList, userData);

    return new GetEventsResponse(res, res.size());
  }

  private Map<Integer, Integer> getTicketCounts(List<Events> events, int userId) {
    List<Integer> ids = events.stream().map(Events::getId).collect(Collectors.toList());
    return db.select(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY)
        .from(EVENTS)
        .join(EVENT_REGISTRATIONS)
        .on(EVENTS.ID.eq(EVENT_REGISTRATIONS.EVENT_ID))
        .where(EVENTS.ID.in(ids))
        .and(EVENT_REGISTRATIONS.USER_ID.eq(userId))
        .and(EVENT_REGISTRATIONS.TICKET_QUANTITY.gt(0))
        .fetchMap(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
  }

  /**
   * Turns a list of jOOq Events DTO into one of our Event DTO.
   *
   * @param events jOOq data objects.
   * @return List of our Event data object.
   */
  protected List<SingleEventResponse> listOfEventsToListOfSingleEventResponse(
      List<Events> events, JWTData userData) {
    Map<Integer, Integer> ticketCounts = getTicketCounts(events, userData.getUserId());
    return events.stream()
        .map(
            event -> {
              EventDetails details =
                  new EventDetails(
                      event.getDescription(),
                      event.getLocation(),
                      event.getStartTime(),
                      event.getEndTime());
              return new SingleEventResponse(
                  event.getId(),
                  event.getTitle(),
                  eventDatabaseOperations.getSpotsLeft(event.getId()),
                  event.getCapacity(),
                  event.getThumbnail(),
                  details,
                  ticketCounts.getOrDefault(event.getId(), 0),
                  canUserRegister(event, userData),
                  event.getPrice());
            })
        .collect(Collectors.toList());
  }

  /** Takes a database representation of a single event and returns the dto representation. */
  protected SingleEventResponse eventPojoToResponse(Events event, JWTData userData) {
    int ticketsBought =
        getTicketCounts(Arrays.asList(event), userData.getUserId()).getOrDefault(event.getId(), 0);

    EventDetails details =
        new EventDetails(
            event.getDescription(), event.getLocation(), event.getStartTime(), event.getEndTime());
    return new SingleEventResponse(
        event.getId(),
        event.getTitle(),
        eventDatabaseOperations.getSpotsLeft(event.getId()),
        event.getCapacity(),
        event.getThumbnail(),
        details,
        ticketsBought,
        canUserRegister(event, userData),
        event.getPrice());
  }

  /**
   * Returns true if the given user is qualified for the event.
   *
   * <p>No user can register for an event that has already ended - GP users can only register for
   * events in the next 5 days
   */
  protected boolean canUserRegister(Events event, JWTData userData) {
    if (event.getEndTime().before(Timestamp.from(Instant.now()))) {
      return false;
    }
    if (userData.getPrivilegeLevel().equals(PrivilegeLevel.GP)) {
      Timestamp fiveDays = Timestamp.from(Instant.now().plus(daysGpCanRegister, ChronoUnit.DAYS));
      return event.getStartTime().before(fiveDays);
    }
    return true;
  }

  /** Takes a dto representation of an event and returns the database record representation. */
  protected EventsRecord eventRequestToRecord(CreateEventRequest request) {
    EventsRecord newRecord = db.newRecord(EVENTS);
    newRecord.setTitle(request.getTitle());
    newRecord.setDescription(request.getDetails().getDescription());
    newRecord.setThumbnail(request.getThumbnail());
    newRecord.setCapacity(request.getSpotsAvailable());
    newRecord.setLocation(request.getDetails().getLocation());
    newRecord.setStartTime(request.getDetails().getStart());
    newRecord.setEndTime(request.getDetails().getEnd());
    newRecord.setPrice(request.getPrice());
    return newRecord;
  }
}
