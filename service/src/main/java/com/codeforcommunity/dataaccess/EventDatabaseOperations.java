package com.codeforcommunity.dataaccess;

import org.jooq.DSLContext;

import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.sum;

public class EventDatabaseOperations {

    private DSLContext db;

    public EventDatabaseOperations(DSLContext db) {
        this.db = db;
    }

    /**
     * Queries the database to find the number of spots left for a given event by id.
     * @param eventId the event id
     * @return int the number of remaining spots for this event
     */
    public int getSpotsLeft(int eventId) {

        int sumRegistrations = db.select(coalesce(sum(EVENT_REGISTRATIONS.TICKET_QUANTITY), 0))
                .from(EVENT_REGISTRATIONS)
                .where(EVENT_REGISTRATIONS.EVENT_ID.eq(eventId))
                .fetchOneInto(Integer.class);

        int capacity = db.select(EVENTS.CAPACITY)
                .from(EVENTS)
                .where(EVENTS.ID.eq(eventId))
                .fetchOneInto(Integer.class);

        return capacity - sumRegistrations;
    }

}
