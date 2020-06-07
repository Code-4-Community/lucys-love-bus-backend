package com.codeforcommunity.dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.codeforcommunity.JooqMock;
import org.jooq.Record1;
import org.jooq.generated.Tables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Contains tests for EventDatabaseOperations.java
public class EventDatabaseOperationsTest {
  private JooqMock myJooqMock;
  private EventDatabaseOperations myEventDatabaseOperations;

  // set up all the mocks
  @BeforeEach
  public void setup() {
    this.myJooqMock = new JooqMock();
    this.myEventDatabaseOperations = new EventDatabaseOperations(myJooqMock.getContext());
  }

  // test getting how many spots left where capacity > registration
  @Test
  public void testGetSpotsLeft1() {
    Record1<Integer> myEventRegistration =
        myJooqMock.getContext().newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(4);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(1);
    myJooqMock.addReturn("SELECT", myTicketsRecord);

    // pending
    myJooqMock.addEmptyReturn("SELECT");

    assertEquals(3, myEventDatabaseOperations.getSpotsLeft(0));
  }

  // test getting how many spots left where capacity = registration
  @Test
  public void testGetSpotsLeft2() {
    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(4);

    Record1<Integer> myEventRegistration =
        myJooqMock.getContext().newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(4);

    myJooqMock.addReturn("SELECT", myTicketsRecord);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    assertEquals(0, myEventDatabaseOperations.getSpotsLeft(0));
  }

  // test getting how many spots left where capacity < registration
  @Test
  public void testGetSpotsLeft3() {
    Record1<Integer> myEventRegistration =
        myJooqMock.getContext().newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(4);

    Record1<Integer> myTicketsRecord =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(10);

    myJooqMock.addReturn("SELECT", myEventRegistration);
    myJooqMock.addReturn("SELECT", myTicketsRecord);

    assertEquals(0, myEventDatabaseOperations.getSpotsLeft(0));
  }
}
