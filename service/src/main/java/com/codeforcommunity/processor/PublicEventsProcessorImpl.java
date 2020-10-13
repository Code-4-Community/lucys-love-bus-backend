package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.EVENTS;

import com.codeforcommunity.api.IPublicEventsProcessor;
import com.codeforcommunity.dataaccess.EventDatabaseOperations;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.Events;

public class PublicEventsProcessorImpl implements IPublicEventsProcessor {

  private final DSLContext db;
  private final EventDatabaseOperations eventDatabaseOperations;

  public PublicEventsProcessorImpl(DSLContext db) {
    this.db = db;
    this.eventDatabaseOperations = new EventDatabaseOperations(db);
  }

  @Override
  public GetEventsResponse getEvents(List<Integer> eventIds) {
    List<Events> e = db.selectFrom(EVENTS).where(EVENTS.ID.in(eventIds)).fetchInto(Events.class);
    return new GetEventsResponse(listOfEventsToListOfSingleEventResponse(e), e.size());
  }

  /**
   * Turns a list of jOOq Events DTO into one of our Event DTO.
   *
   * @param events jOOq data objects.
   * @return List of our Event data object.
   */
  private List<SingleEventResponse> listOfEventsToListOfSingleEventResponse(List<Events> events) {
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
                  event.getPrice());
            })
        .collect(Collectors.toList());
  }
}
