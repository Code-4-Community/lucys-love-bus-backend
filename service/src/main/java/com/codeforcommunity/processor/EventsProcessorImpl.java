package com.codeforcommunity.processor;

import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.dto.userEvents.components.Event;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import com.codeforcommunity.dto.userEvents.requests.GetUserEventsRequest;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.propertiesLoader.PropertiesLoader;
import org.jooq.DSLContext;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.pojos.Events;
import org.jooq.generated.tables.records.EventsRecord;
import org.jooq.impl.DSL;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.USER_EVENTS;
import static org.jooq.impl.DSL.count;

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

  @Override
  public GetEventsResponse getEvents(List<Integer> eventIds) {
    List<Events> e = db.selectFrom(EVENTS).where(EVENTS.ID.in(eventIds)).fetchInto(Events.class);
    return new GetEventsResponse(parseEvents(e), e.size());
  }

  @Override
  public GetEventsResponse getEventsSignedUp(GetUserEventsRequest request, JWTData userData) {
    List<Events> events = db.selectFrom(Tables.USERS.join(Tables.USER_EVENTS)
            .on(Tables.USERS.ID.eq(Tables.USER_EVENTS.USERS_ID))
            .join(Tables.EVENTS).on(Tables.USER_EVENTS.EVENT_ID.eq(Tables.EVENTS.ID)))
            .where(Tables.USERS.ID.eq(userData.getUserId())).fetchInto(Events.class);

    return mapToResponseAndFilter(events, request.getCount(), request.getStartDate(),
            request.getEndDate());
  }

  @Override
  public GetEventsResponse getEventsQualified(JWTData userData) {

    Optional<Timestamp> startDate = Optional.of(Timestamp.from(Instant.now()));
    Optional<Timestamp> fiveDays = Optional.of(Timestamp.from(Instant.now().plusSeconds(432000)));
    Optional<Timestamp> endDate = userData.getPrivilegeLevel().equals(PrivilegeLevel.GP) ? fiveDays : Optional.empty();

    List<Events> e = db.selectFrom(EVENTS).fetchInto(Events.class);
    return mapToResponseAndFilter(e, Optional.empty(), startDate, endDate);

  }

  private List<Event> parseEvents(List<Events> events) {

    return events.stream().map(event -> {
      EventDetails details = new EventDetails(event.getDescription(), event.getLocation(), event.getStartTime(),
              event.getEndTime());
      URL thumbnail;
      try {
        thumbnail = new URL(event.getThumbnail());
      } catch (MalformedURLException me) {
        thumbnail = null; //todo address this exception
      }
      Event e = new Event(event.getId(), event.getTitle(), getSpotsLeft(event.getId()), thumbnail,
              details);
      return e;
    }).collect(Collectors.toList());

  }

  private GetEventsResponse mapToResponseAndFilter(List<Events> events, Optional<Integer> count,
                                                   Optional<Timestamp> startDate, Optional<Timestamp> endDate) {
    List<Event> parsedEvents = parseEvents(events);
    List<Event> limitedEvents = limitNumberOfElements(parsedEvents, count);
    List<Event> filteredEvents =
            filterByDates(limitedEvents, startDate, endDate);

    return new GetEventsResponse(filteredEvents, filteredEvents.size());
  }

  private List<Event> filterByDates(List<Event> events, Optional<Timestamp> startDate, Optional<Timestamp> endDate) {

    Predicate<Event> pred = event -> {

      Timestamp startTimeToCheck = event.getDetails().getStart();
      Timestamp endTimeToCheck = event.getDetails().getEnd();

      boolean validEnd = endDate.isPresent() ? endTimeToCheck.before(endDate.get()) : true;
      boolean validStart = startDate.isPresent() ? startTimeToCheck.after(startDate.get()) : true;

      return validEnd && validStart;

    };

    return events.stream().filter(pred).collect(Collectors.toList());
  }

  private int getSpotsLeft(int eventId) {

    return db.select(EVENTS.CAPACITY.minus(count())).from(EVENTS.join(USER_EVENTS).on(EVENTS.ID.eq(USER_EVENTS.EVENT_ID)))
            .groupBy(EVENTS.ID).fetchOneInto(Integer.class);

  }

  private List<Event> limitNumberOfElements(List<Event> events, Optional<Integer> limit) {
    return limit.isPresent() ? events.subList(0, limit.get()) : events;
  }

  /**
   * Takes a database representation of a single event and returns the dto representation.
   */
  private SingleEventResponse eventPojoToResponse(Events event) {
    EventDetails details = new EventDetails(event.getDescription(), event.getLocation(),
        event.getStartTime(), event.getEndTime());
    return new SingleEventResponse(event.getId(), event.getTitle(),
        event.getCapacity(), event.getThumbnail(), details);
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
