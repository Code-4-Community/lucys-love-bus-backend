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
    Record1<Integer> myTicketsRecord = myJooqMock
        .getContext()
        .newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(1);

    Record1<Integer> myEventRegistration = myJooqMock
        .getContext()
        .newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(4);

    myJooqMock.addReturn("SELECT", myTicketsRecord);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    assertEquals(3, myEventDatabaseOperations.getSpotsLeft(0));
  }

  // test getting how many spots left where capacity = registration
  @Test
  public void testGetSpotsLeft2() {
    Record1<Integer> myTicketsRecord = myJooqMock
        .getContext()
        .newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(4);

    Record1<Integer> myEventRegistration = myJooqMock
        .getContext()
        .newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(4);

    myJooqMock.addReturn("SELECT", myTicketsRecord);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    assertEquals(0, myEventDatabaseOperations.getSpotsLeft(0));
  }

  // test getting how many spots left where capacity < registration
  @Test
  public void testGetSpotsLeft3() {
    Record1<Integer> myTicketsRecord = myJooqMock
        .getContext()
        .newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(10);

    Record1<Integer> myEventRegistration = myJooqMock
        .getContext()
        .newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(4);

    myJooqMock.addReturn("SELECT", myTicketsRecord);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    // TODO: @ dev team, please address this
    // it actually returns -6, though it should realistically just be 0
    assertEquals(0, myEventDatabaseOperations.getSpotsLeft(0));
  }

  // test getting how many spots left where you have negative capacity
  @Test
  public void testGetSpotsLeft4() {
    Record1<Integer> myTicketsRecord = myJooqMock
        .getContext()
        .newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(2);

    Record1<Integer> myEventRegistration = myJooqMock
        .getContext()
        .newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(-4);

    myJooqMock.addReturn("SELECT", myTicketsRecord);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    // TODO: @ dev team, please address this
    // it actually returns -6, are you sure you want this behavior?
    assertEquals(0, myEventDatabaseOperations.getSpotsLeft(0));
  }

  // test getting how many spots left where you have negative registration
  @Test
  public void testGetSpotsLeft5() {
    Record1<Integer> myTicketsRecord = myJooqMock
        .getContext()
        .newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(-2);

    Record1<Integer> myEventRegistration = myJooqMock
        .getContext()
        .newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(4);

    myJooqMock.addReturn("SELECT", myTicketsRecord);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    // TODO: @ dev team, please address this
    // it actually returns 6, though it should realistically just be 4
    assertEquals(4, myEventDatabaseOperations.getSpotsLeft(0));
  }

  // test getting how many spots left where you have both negative registration/capacity
  @Test
  public void testGetSpotsLeft6() {
    Record1<Integer> myTicketsRecord = myJooqMock
        .getContext()
        .newRecord(Tables.EVENT_REGISTRATIONS.TICKET_QUANTITY);
    myTicketsRecord.values(-6);

    Record1<Integer> myEventRegistration = myJooqMock
        .getContext()
        .newRecord(Tables.EVENTS.CAPACITY);
    myEventRegistration.values(-4);

    myJooqMock.addReturn("SELECT", myTicketsRecord);
    myJooqMock.addReturn("SELECT", myEventRegistration);

    // TODO: @ dev team, please address this
    // it actually returns 2, though it should realistically just be 0 (though idk
    // what exactly in this case)
    assertEquals(0, myEventDatabaseOperations.getSpotsLeft(0));
  }
}