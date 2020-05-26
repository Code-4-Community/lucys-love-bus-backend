package com.codeforcommunity.dataaccess;

import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;
import static org.jooq.impl.DSL.sum;

import java.util.Optional;
import org.jooq.DSLContext;

public class EventDatabaseOperations {

  private final DSLContext db;

  public EventDatabaseOperations(DSLContext db) {
    this.db = db;
  }

  /**
   * Queries the database to find the number of spots left for a given event by id.
   *
   * @param eventId the event id
   * @return int the number of remaining spots for this event
   */
  public int getSpotsLeft(int eventId) {
    Integer sumRegistrations =
        Optional.ofNullable(
                db.select(sum(EVENT_REGISTRATIONS.TICKET_QUANTITY))
                    .from(EVENT_REGISTRATIONS)
                    .where(EVENT_REGISTRATIONS.EVENT_ID.eq(eventId))
                    .fetchOneInto(Integer.class))
            .orElse(0);
    Integer capacity =
        Optional.ofNullable(
                db.select(EVENTS.CAPACITY)
                    .from(EVENTS)
                    .where(EVENTS.ID.eq(eventId))
                    .fetchOneInto(Integer.class))
            .orElse(0);
    return capacity - sumRegistrations;
  }
}
