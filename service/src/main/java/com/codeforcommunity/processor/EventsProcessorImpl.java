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
import com.codeforcommunity.enums.EventRegistrationStatus;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.BadRequestImageException;
import com.codeforcommunity.exceptions.EventDoesNotExistException;
import com.codeforcommunity.exceptions.S3FailedUploadException;
import com.codeforcommunity.requester.S3Requester;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.Period;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSeekStep1;
import org.jooq.SelectWhereStep;
import org.jooq.generated.tables.pojos.Events;
import org.jooq.generated.tables.records.EventsRecord;

public class EventsProcessorImpl implements IEventsProcessor {

  private final DSLContext db;
  private final EventDatabaseOperations eventDatabaseOperations;

  public EventsProcessorImpl(DSLContext db) {
    this.db = db;
    this.eventDatabaseOperations = new EventDatabaseOperations(db);
  }

  @Override
  public SingleEventResponse createEvent(CreateEventRequest request, JWTData userData)
      throws BadRequestImageException, S3FailedUploadException {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }

    String publicImageUrl =
        S3Requester.validateUploadImageToS3LucyEvents(request.getTitle(), request.getThumbnail());
    request.setThumbnail(
        publicImageUrl); // Update the request to contain the URL for the DB and JSON response OR
    // null if no image given

    EventsRecord newEventRecord = eventRequestToRecord(request);
    newEventRecord.store();
    return eventPojoToResponse(newEventRecord.into(Events.class), userData.getUserId());
  }

  @Override
  public SingleEventResponse getSingleEvent(int eventId, JWTData userData) {
    Events event = db.selectFrom(EVENTS).where(EVENTS.ID.eq(eventId)).fetchOneInto(Events.class);

    if (event == null) {
      throw new EventDoesNotExistException(eventId);
    }

    return eventPojoToResponse(event, userData.getUserId());
  }

  @Override
  public GetEventsResponse getEvents(List<Integer> eventIds, JWTData userData) {
    List<Events> e = db.selectFrom(EVENTS).where(EVENTS.ID.in(eventIds)).fetchInto(Events.class);
    return new GetEventsResponse(
        listOfEventsToListOfSingleEventResponse(e, userData.getUserId()), e.size());
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

    List<SingleEventResponse> res =
        listOfEventsToListOfSingleEventResponse(eventPojos, userData.getUserId());
    return new GetEventsResponse(res, res.size());
  }

  @Override
  public GetEventsResponse getEventsQualified(JWTData userData) {

    Timestamp startDate = Timestamp.from(Instant.now());
    Timestamp fiveDays = Timestamp.from(Instant.now().plus(Period.ofDays(5)));
    boolean limitedToFiveDays = userData.getPrivilegeLevel().equals(PrivilegeLevel.GP);

    SelectWhereStep<EventsRecord> select = db.selectFrom(EVENTS);
    SelectConditionStep<EventsRecord> afterDateFilter;

    if (limitedToFiveDays) {
      afterDateFilter = select.where(EVENTS.START_TIME.between(startDate, fiveDays));
    } else {
      afterDateFilter = select.where(EVENTS.START_TIME.greaterOrEqual(startDate));
    }
    List<Events> eventsList =
        afterDateFilter.orderBy(EVENTS.START_TIME.asc()).fetchInto(Events.class);

    List<SingleEventResponse> res =
        listOfEventsToListOfSingleEventResponse(eventsList, userData.getUserId());

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
        .and(EVENT_REGISTRATIONS.REGISTRATION_STATUS.eq(EventRegistrationStatus.ACTIVE))
        .and(EVENT_REGISTRATIONS.TICKET_QUANTITY.gt(0))
        .fetchMap(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
  }

  @Override
  public SingleEventResponse modifyEvent(
      int eventId, ModifyEventRequest request, JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }
    EventsRecord record = db.fetchOne(EVENTS, EVENTS.ID.eq(eventId));
    if (request.getTitle() != null) {
      record.setTitle(request.getTitle());
    }
    if (request.getSpotsAvailable() != null) {
      record.setCapacity(request.getSpotsAvailable());
    }
    if (request.getThumbnail() != null) {
      record.setThumbnail(request.getThumbnail());
    }
    if (request.getDetails() != null) {
      EventDetails details = request.getDetails();
      if (details.getDescription() != null) {
        record.setDescription(details.getDescription());
      }
      if (details.getLocation() != null) {
        record.setLocation(details.getLocation());
      }
      if (details.getStart() != null) {
        record.setStartTime(details.getStart());
      }
      if (details.getEnd() != null) {
        record.setEndTime(details.getEnd());
      }
    }
    record.store();

    return getSingleEvent(eventId, userData);
  }

  @Override
  public void deleteEvent(int eventId, JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }
    db.delete(EVENTS).where(EVENTS.ID.eq(eventId)).execute();
  }

  @Override
  public EventRegistrations getEventRegisteredUsers(int eventId, JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }

    if (!db.fetchExists(EVENTS.where(EVENTS.ID.eq(eventId)))) {
      throw new EventDoesNotExistException(eventId);
    }

    List<Registration> regs =
        db.select(
                CONTACTS.FIRST_NAME,
                CONTACTS.LAST_NAME,
                CONTACTS.EMAIL,
                EVENT_REGISTRATIONS.TICKET_QUANTITY)
            .from(EVENT_REGISTRATIONS)
            .join(CONTACTS)
            .on(EVENT_REGISTRATIONS.USER_ID.eq(USERS.ID))
            .where(EVENT_REGISTRATIONS.EVENT_ID.eq(eventId))
            .and(CONTACTS.IS_MAIN_CONTACT.isTrue())
            .fetchInto(Registration.class);

    return new EventRegistrations(regs);
  }

  /**
   * Returns a String that contains the CSV data for the given event's RSVPs.
   *
   * @param eventId The event to get the RSVPs of.
   * @param userData The user.
   * @return The CSV of RSVPs.
   */
  @Override
  public String getEventRSVPs(int eventId, JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }

    if (!db.fetchExists(EVENTS.where(EVENTS.ID.eq(eventId)))) {
      throw new EventDoesNotExistException(eventId);
    }

    List<RSVP> rsvpUsers =
        db.select(
                USERS.ID,
                EVENT_REGISTRATIONS.TICKET_QUANTITY,
                USERS.EMAIL,
                USERS.PRIVILEGE_LEVEL,
                USERS.ADDRESS,
                USERS.CITY,
                USERS.STATE,
                USERS.ZIPCODE)
            .from(EVENT_REGISTRATIONS)
            .join(USERS)
            .on(EVENT_REGISTRATIONS.USER_ID.eq(USERS.ID))
            .where(EVENT_REGISTRATIONS.EVENT_ID.eq(eventId))
            .and(EVENT_REGISTRATIONS.REGISTRATION_STATUS.eq(EventRegistrationStatus.ACTIVE))
            .fetchInto(RSVP.class);

    List<RSVP> rsvpContacts =
        db.select(
                EVENT_REGISTRATIONS.USER_ID,
                CONTACTS.EMAIL,
                CONTACTS.FIRST_NAME,
                CONTACTS.LAST_NAME,
                CONTACTS.IS_MAIN_CONTACT,
                CONTACTS.DATE_OF_BIRTH,
                CONTACTS.PHONE_NUMBER,
                CONTACTS.PRONOUNS,
                CONTACTS.ALLERGIES,
                CONTACTS.DIAGNOSIS,
                CONTACTS.MEDICATIONS,
                CONTACTS.NOTES)
            .from(EVENT_REGISTRATIONS)
            .join(CONTACTS)
            .on(EVENT_REGISTRATIONS.USER_ID.eq(CONTACTS.USER_ID))
            .where(EVENT_REGISTRATIONS.EVENT_ID.eq(eventId))
            .and(EVENT_REGISTRATIONS.REGISTRATION_STATUS.eq(EventRegistrationStatus.ACTIVE))
            .fetchInto(RSVP.class);

    List<RSVP> rsvpChildren =
        db.select(
                EVENT_REGISTRATIONS.USER_ID,
                CHILDREN.FIRST_NAME,
                CHILDREN.LAST_NAME,
                CHILDREN.DATE_OF_BIRTH,
                CHILDREN.PRONOUNS,
                CHILDREN.ALLERGIES,
                CHILDREN.DIAGNOSIS,
                CHILDREN.MEDICATIONS,
                CHILDREN.NOTES,
                CHILDREN.SCHOOL_YEAR,
                CHILDREN.SCHOOL)
            .from(EVENT_REGISTRATIONS)
            .join(CHILDREN)
            .on(EVENT_REGISTRATIONS.USER_ID.eq(CHILDREN.USER_ID))
            .where(EVENT_REGISTRATIONS.EVENT_ID.eq(eventId))
            .and(EVENT_REGISTRATIONS.REGISTRATION_STATUS.eq(EventRegistrationStatus.ACTIVE))
            .fetchInto(RSVP.class);

    rsvpUsers.addAll(rsvpContacts);
    rsvpUsers.addAll(rsvpChildren);
    rsvpUsers.sort(Comparator.comparing(RSVP::getUserId));

    StringBuilder builder = new StringBuilder();
    builder.append(RSVP.toHeaderCSV());
    for (RSVP rsvp : rsvpUsers) {
      builder.append(rsvp.toRowCSV());
    }

    return builder.toString();
  }

  /**
   * Turns a list of jOOq Events DTO into one of our Event DTO.
   *
   * @param events jOOq data objects.
   * @return List of our Event data object.
   */
  private List<SingleEventResponse> listOfEventsToListOfSingleEventResponse(
      List<Events> events, int userId) {
    Map<Integer, Integer> ticketCounts = getTicketCounts(events, userId);
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
                  ticketCounts.getOrDefault(event.getId(), 0));
            })
        .collect(Collectors.toList());
  }

  /** Takes a database representation of a single event and returns the dto representation. */
  private SingleEventResponse eventPojoToResponse(Events event, int userId) {
    int signedUp = getTicketCounts(Arrays.asList(event), userId).getOrDefault(event.getId(), 0);

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
        signedUp);
  }

  /** Takes a dto representation of an event and returns the database record representation. */
  private EventsRecord eventRequestToRecord(CreateEventRequest request) {
    EventsRecord newRecord = db.newRecord(EVENTS);
    newRecord.setTitle(request.getTitle());
    newRecord.setDescription(request.getDetails().getDescription());
    newRecord.setThumbnail(request.getThumbnail());
    newRecord.setCapacity(request.getSpotsAvailable());
    newRecord.setLocation(request.getDetails().getLocation());
    newRecord.setStartTime(request.getDetails().getStart());
    newRecord.setEndTime(request.getDetails().getEnd());
    return newRecord;
  }
}
