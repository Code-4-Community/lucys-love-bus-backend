package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.EVENTS;

import com.codeforcommunity.api.IPublicEventsProcessor;
import com.codeforcommunity.dataaccess.EventDatabaseOperations;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import com.codeforcommunity.dto.userEvents.responses.GetPublicEventsResponse;
import com.codeforcommunity.dto.userEvents.responses.PublicSingleEventResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.Events;

public class PublicEventsProcessorImpl implements IPublicEventsProcessor {

  private final int registerLeniencyHours = 12;

  private final DSLContext db;
  private final EventDatabaseOperations eventDatabaseOperations;

  public PublicEventsProcessorImpl(DSLContext db) {
    this.db = db;
    this.eventDatabaseOperations = new EventDatabaseOperations(db);
  }

  @Override
  public GetPublicEventsResponse getPublicEvents(List<Integer> eventIds) {

    List<Events> events;

    if (eventIds.isEmpty()) {
      Timestamp startDate =
          Timestamp.from(Instant.now().minus(registerLeniencyHours, ChronoUnit.HOURS));
      events =
          db.selectFrom(EVENTS)
              .where(EVENTS.START_TIME.greaterOrEqual(startDate))
              .orderBy(EVENTS.START_TIME.asc())
              .fetchInto(Events.class);
    } else {
      events = db.selectFrom(EVENTS).where(EVENTS.ID.in(eventIds)).fetchInto(Events.class);
    }
    return new GetPublicEventsResponse(
        listOfEventsToListOfPublicSingleEventResponse(events), events.size());
  }

  /**
   * Turns a list of jOOq Events DTO into one of our Event DTO.
   *
   * @param events jOOq data objects.
   * @return List of our Event data object.
   */
  private List<PublicSingleEventResponse> listOfEventsToListOfPublicSingleEventResponse(
      List<Events> events) {
    return events.stream()
        .map(
            event -> {
              EventDetails details =
                  new EventDetails(
                      event.getDescription(),
                      event.getLocation(),
                      event.getStartTime(),
                      event.getEndTime());
              return new PublicSingleEventResponse(
                  event.getId(),
                  event.getTitle(),
                  eventDatabaseOperations.getSpotsLeft(event.getId()),
                  event.getCapacity(),
                  event.getThumbnail(),
                  details,
                  event.getPrice());
            })
        .collect(Collectors.toList());
  }
}
