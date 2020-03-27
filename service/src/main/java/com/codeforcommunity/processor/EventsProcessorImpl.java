package com.codeforcommunity.processor;

import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.events.CreateEventRequest;
import com.codeforcommunity.dto.events.EventDetails;
import com.codeforcommunity.dto.events.SingleEventResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.Events;
import org.jooq.generated.tables.records.EventsRecord;

import static org.jooq.generated.Tables.EVENTS;

public class EventsProcessorImpl implements IEventsProcessor {

  private final DSLContext db;

  public EventsProcessorImpl(DSLContext db) {
    this.db = db;
  }

  @Override
  public SingleEventResponse createEvent(CreateEventRequest request, JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }

    EventsRecord newEventRecord = eventRequestToRecord(request);
    newEventRecord.store();
    return eventPojoToResponse(newEventRecord.into(Events.class));
  }

  @Override
  public SingleEventResponse getSingleEvent(int eventId) {
    Events event = db.selectFrom(EVENTS)
        .where(EVENTS.ID.eq(eventId))
        .fetchOneInto(Events.class);
    return eventPojoToResponse(event);
  }


  /**
   * Takes a database representation of a single event and returns the dto representation.
   */
  private SingleEventResponse eventPojoToResponse(Events event) {
    EventDetails details = new EventDetails(event.getDescription(), event.getLocation(),
        event.getStartTime(), event.getEndTime());
    return new SingleEventResponse(event.getId(), event.getTitle(),
        event.getCapacity(), "urls still todo", details);
  }

  /**
   * Takes a dto representation of an event and returns the database record representation.
   */
  private EventsRecord eventRequestToRecord(CreateEventRequest request) {
    EventsRecord newRecord = db.newRecord(EVENTS);
    newRecord.setTitle(request.getTitle());
    newRecord.setDescription(request.getDetails().getDescription());
    newRecord.setCapacity(request.getSpotsAvailable());
    newRecord.setLocation(request.getDetails().getLocation());
    newRecord.setStartTime(request.getDetails().getStart());
    newRecord.setEndTime(request.getDetails().getEnd());
    return newRecord;
  }
}
