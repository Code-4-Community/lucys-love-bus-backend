package com.codeforcommunity.processor;

import com.amazonaws.AmazonServiceException;
import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.exceptions.S3FailedUploadException;
import com.codeforcommunity.requester.S3Requester;
import com.codeforcommunity.dataaccess.EventDatabaseOperations;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.requests.ModifyEventRequest;
import com.codeforcommunity.dto.userEvents.responses.EventIdResponse;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.dto.userEvents.components.Event;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.requests.GetUserEventsRequest;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.BadRequestImageException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jooq.*;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSeekStep1;
import org.jooq.SelectWhereStep;
import org.jooq.generated.tables.pojos.Events;
import org.jooq.generated.tables.records.EventsRecord;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;
import static org.jooq.generated.Tables.USERS;


public class EventsProcessorImpl implements IEventsProcessor {

  private final DSLContext db;
  private final EventDatabaseOperations eventDatabaseOperations;

  public EventsProcessorImpl(DSLContext db) {
    this.db = db;
    this.eventDatabaseOperations = new EventDatabaseOperations(db);
  }

  @Override
  public SingleEventResponse createEvent(CreateEventRequest request, JWTData userData) throws BadRequestImageException {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }

    String title = request.getTitle().replaceAll("[!@#$%^&*()=+./\\\\|<>`~\\[\\]{}?]", "");  // Remove special characters
    String fileName = title.replace(" ", "_").toLowerCase() + "_thumbnail";  // The desired name of the file in S3

    String base64Encoding = request.getThumbnail();  // Expected that the thumbnail field is the base64 encoding of the image

    try {
      String publicImageUrl = S3Requester.validateBase64ImageAndUploadToS3(fileName, S3Requester.DIR_LLB_PUBLIC_EVENTS, base64Encoding);
      if (publicImageUrl == null) {
        throw new BadRequestImageException();  // Null only when the image encoding failed to validate
      }

      request.setThumbnail(publicImageUrl);  // Update the request to contain the URL for the DB and JSON response

      EventsRecord newEventRecord = eventRequestToRecord(request);
      newEventRecord.store();
      return eventPojoToResponse(newEventRecord.into(Events.class));
    } catch (IOException e) {
      throw new BadRequestImageException();  // Image failed to decode
    } catch (AmazonServiceException e) {
      throw new S3FailedUploadException(e.getMessage());  // S3 upload failed
    }
  }

  @Override
  public SingleEventResponse getSingleEvent(int eventId) {
    Events event = db.selectFrom(EVENTS)
        .where(EVENTS.ID.eq(eventId))
        .fetchOneInto(Events.class);
    return eventPojoToResponse(event);
  }

  @Override
  public GetEventsResponse getEvents(List<Integer> eventIds) {
    List<Events> e = db.selectFrom(EVENTS).where(EVENTS.ID.in(eventIds)).fetchInto(Events.class);
    return new GetEventsResponse(listOfEventsToListOfEvent(e), e.size());
  }

  @Override
  public GetEventsResponse getEventsSignedUp(GetUserEventsRequest request, JWTData userData) {

    SelectConditionStep q = db.select(EVENTS.fields())
        .from(USERS
            .join(EVENT_REGISTRATIONS).onKey(EVENT_REGISTRATIONS.USER_ID)
            .join(EVENTS).onKey(EVENT_REGISTRATIONS.EVENT_ID))
        .where(USERS.ID.eq(userData.getUserId()));

    SelectConditionStep afterDateFilter = q;

    if (request.getEndDate().isPresent()) {
      if (request.getStartDate().isPresent()) {
        afterDateFilter = q.and(EVENTS.START_TIME.between(request.getStartDate().get(), request.getEndDate().get()));
      } else {
        afterDateFilter = q.and(EVENTS.START_TIME.lessOrEqual(request.getEndDate().get()));
      }
    } else {
      if (request.getStartDate().isPresent()) {
        afterDateFilter = q.and(EVENTS.START_TIME.greaterOrEqual(request.getStartDate().get()));
      }
    }

    SelectSeekStep1 s = afterDateFilter.orderBy(EVENTS.START_TIME.asc());
    List<Events> eventPojos;

    if (request.getCount().isPresent()) {
      eventPojos = s.limit(request.getCount().get()).fetchInto(Events.class);
    } else {
      eventPojos = s.fetchInto(Events.class);
    }

    List<Event> res = listOfEventsToListOfEvent(eventPojos);
    return new GetEventsResponse(res, res.size());
  }

  @Override
  public GetEventsResponse getEventsQualified(JWTData userData) {

    Timestamp startDate = Timestamp.from(Instant.now());
    Timestamp fiveDays = Timestamp.from(Instant.now().plus(Period.ofDays(5)));
    boolean limitedToFiveDays = userData.getPrivilegeLevel().equals(PrivilegeLevel.GP);

    SelectWhereStep select = db.selectFrom(EVENTS);
    SelectConditionStep afterDateFilter;

    if (limitedToFiveDays) {
      afterDateFilter = select.where(EVENTS.START_TIME.between(startDate, fiveDays));
    } else {
      afterDateFilter = select.where(EVENTS.START_TIME.greaterOrEqual(startDate));
    }

    List<Event> res = listOfEventsToListOfEvent(afterDateFilter.fetchInto(Events.class));

    return new GetEventsResponse(res, res.size());
  }

  @Override
  public SingleEventResponse modifyEvent(int eventId, ModifyEventRequest request,
      JWTData userData) {
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

    return getSingleEvent(eventId);
  }

  @Override
  public void deleteEvent(int eventId, JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }
    db.delete(EVENTS).where(EVENTS.ID.eq(eventId)).execute();
  }

  /**
   * Turns a list of jOOq Events DTO into one of our Event DTO.
   *
   * @param events jOOq data objects.
   * @return List of our Event data object.
   */
  private List<Event> listOfEventsToListOfEvent(List<Events> events) {

    return events.stream().map(event -> {
      EventDetails details = new EventDetails(event.getDescription(), event.getLocation(), event.getStartTime(),
          event.getEndTime());
      Event e = new Event(event.getId(), event.getTitle(),
          eventDatabaseOperations.getSpotsLeft(event.getId()), event.getThumbnail(),
          details);
      return e;
    }).collect(Collectors.toList());

  }

  /**
   * Takes a database representation of a single event and returns the dto representation.
   */
  private SingleEventResponse eventPojoToResponse(Events event) {
    EventDetails details = new EventDetails(event.getDescription(), event.getLocation(),
        event.getStartTime(), event.getEndTime());
    return new SingleEventResponse(event.getId(), event.getTitle(),
        eventDatabaseOperations.getSpotsLeft(event.getId()), event.getCapacity(), event.getThumbnail(), details);
  }

  /**
   * Takes a dto representation of an event and returns the database record representation.
   */
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
