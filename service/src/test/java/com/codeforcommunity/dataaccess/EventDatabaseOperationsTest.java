package com.codeforcommunity.dataaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.codeforcommunity.JooqMock;
import java.util.ArrayList;
import java.util.List;
import org.jooq.generated.tables.records.EventRegistrationsRecord;
import org.jooq.generated.tables.records.EventsRecord;
import org.junit.Before;
import org.junit.Test;

// Contains tests for EventDatabaseOperations.java
public class EventDatabaseOperationsTest {
  JooqMock myJooqMock;
  EventDatabaseOperations myEventDatabaseOperations;

  // set up all the mocks
  @Before
  public void setup() {
    this.myJooqMock = new JooqMock();
    this.myEventDatabaseOperations = new EventDatabaseOperations(myJooqMock.getContext());
  }

  // test getting how many spots left there are for an event with more capacity than registration
  @Test
  public void testGetSpotsLeft1() {
    EventRegistrationsRecord myEventRegistration = new EventRegistrationsRecord();
    myEventRegistration.setEventId(0);
    myEventRegistration.setTicketQuantity(1);
    List<EventRegistrationsRecord> myRegistrations = new ArrayList<>();
    myRegistrations.add(myEventRegistration);
    myJooqMock.addReturn("SELECT", myRegistrations);

    EventsRecord myEvent = new EventsRecord();
    myEvent.setId(0);
    myEvent.setCapacity(4);
    myJooqMock.addReturn("SELECT", myEvent);

    assertEquals(3, myEventDatabaseOperations.getSpotsLeft(0));
  }
}