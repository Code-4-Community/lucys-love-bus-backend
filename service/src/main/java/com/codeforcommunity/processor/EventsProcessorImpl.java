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

  /*
  package com.codeforcommunity.processor;

import com.codeforcommunity.api.IUserEventsProcessor;
import com.codeforcommunity.dto.userEvents.components.Event;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import com.codeforcommunity.dto.userEvents.requests.*;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.exceptions.EventDoesNotExistException;
import com.codeforcommunity.exceptions.UserDoesNotExistException;
import com.codeforcommunity.propertiesLoader.PropertiesLoader;
import org.jooq.DSLContext;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.pojos.Events;
import org.jooq.impl.DSL;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UserEventsProcessorImpl implements IUserEventsProcessor {
    //stupid thing for testing
    public static void main(String[] args) {
        Properties dbProperties = PropertiesLoader.getDbProperties();
        DSLContext db = DSL.using(dbProperties.getProperty("database.url"),
                dbProperties.getProperty("database.username"),
                dbProperties.getProperty("database.password"));
        UserEventsProcessorImpl testBih = new UserEventsProcessorImpl(db);
        testBih.getUserEventsSignedUp(new GetUserEventsRequest() {{
            setUserId(1);
        }});
    }

    private final DSLContext db;

    public UserEventsProcessorImpl(DSLContext db) {
        this.db = db;
    }

    @Override
    public GetEventsResponse getAllEvents(GetAllEventsRequest request) {
       List<Events> events = db.selectFrom(Tables.EVENTS).orderBy(Tables.EVENTS.START_TIME.desc()).fetchInto(Events.class);
       return handleEventRequestParams(events, request.getCount(), request.getStartDate(),
               request.getEndDate());
    }

    @Override
    public GetEventsResponse getEventsById(GetEventsByIdRequest request) throws EventDoesNotExistException {
        List<Events> events = db.selectFrom(Tables.EVENTS).where(Tables.EVENTS.ID.eq(request.getId()))
                .orderBy(Tables.EVENTS.START_TIME.desc()).fetchInto(Events.class);
        return handleEventRequestParams(events, request.getCount(), request.getStartDate(),
                request.getEndDate());
    }

    @Override
    public GetEventsResponse getUserEventsSignedUp(GetUserEventsRequest request) throws UserDoesNotExistException {
        List<Events> events = db.selectFrom(Tables.USERS.join(Tables.USER_EVENTS)
                .on(Tables.USERS.ID.eq(Tables.USER_EVENTS.USERS_ID))
                .join(Tables.EVENTS).on(Tables.USER_EVENTS.EVENT_ID.eq(Tables.EVENTS.ID)))
                .where(Tables.USERS.ID.eq(request.getUserId())).fetchInto(Events.class);

        return handleEventRequestParams(events, request.getCount(), request.getStartDate(),
                request.getEndDate());
    }

    @Override
    public GetEventsResponse getUserEventsQualified(GetUserEventsRequest request) throws UserDoesNotExistException {
        return null;
    }

    @Override
    public GetEventsResponse getUserEventsStarred(GetUserEventsRequest request) throws UserDoesNotExistException {
        return null;
    }

    @Override
    public void postUserCheckout(PostUserCheckoutRequest request) throws EventDoesNotExistException, UserDoesNotExistException {

    }

    @Override
    public void postUserStarred(PostUserStarredRequest request) throws EventDoesNotExistException, UserDoesNotExistException {

    }

    @Override
    public void deleteUserStarred(DeleteUserStarredRequest request) throws EventDoesNotExistException, UserDoesNotExistException {

    }

    private GetEventsResponse handleEventRequestParams(List<Events> events, Optional<Integer> count,
                                                 Optional<Timestamp> startDate, Optional<Timestamp> endDate) {
        List<Event> parsedEvents = parseEvents(events);
        List<Event> limitedEvents = limitNumberOfElements(parsedEvents, count);
        List<Event> filteredEvents =
                filterByDates(limitedEvents, startDate, endDate);

        return new GetEventsResponse(filteredEvents, events.size());
    }

    //turns a jooq event into one of our events
    private List<Event> parseEvents(List<Events> events) {

        return events.stream().map(event -> {
            EventDetails details = new EventDetails(event.getDescription(), event.getLocation(), event.getStartTime(),
                    event.getEndTime()); //TODO do we need to do some logic of spots left vs capacity?
            Event e = new Event(event.getId(), event.getTitle(), event.getCapacity(), null ,
  details);
            return e;
}).collect(Collectors.toList());

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

private List<Event> limitNumberOfElements(List<Event> events, Optional<Integer> limit) {
        return limit.isPresent() ? events.subList(0, limit.get()) : events;
        }

        }


        */
}
