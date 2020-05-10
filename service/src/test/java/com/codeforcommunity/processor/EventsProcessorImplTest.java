package com.codeforcommunity.processor;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;

import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.EventsRecord;
import org.jooq.impl.UpdatableRecordImpl;

import org.mockito.Mockito;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// Contains tests for EventsProcessorImplTest.java in main
public class EventsProcessorImplTest {
  JooqMock myJooqMock;
  EventsProcessorImpl myEventsProcessorImpl;

  // use UNIX time for ease of testing
  // 04/16/2020 @ 1:20am (UTC)
  private final int START_TIMESTAMP_TEST = 1587000000;
  // 04/17/2020 @ 5:06am (UTC)
  private final int END_TIMESTAMP_TEST = 1587100000;

  // set up all the mocks
  @Before
  public void setup() {
    this.myJooqMock = new JooqMock();
    this.myEventsProcessorImpl = new EventsProcessorImpl(myJooqMock.getContext());
  }

  // test exception thrown for not being an admin
  @Test
  public void testCreateEvent1() {
    // make the event
    EventDetails myEventDetails = new EventDetails("my event", "boston",
            new Timestamp(START_TIMESTAMP_TEST), new Timestamp(END_TIMESTAMP_TEST));
    CreateEventRequest myEventRequest = new CreateEventRequest("sample", 5,
            "sample thumbnail", myEventDetails);

    // mock the DB
    JWTData badUser = Mockito.mock(JWTData.class);
    when(badUser.getPrivilegeLevel()).thenReturn(PrivilegeLevel.GP);

    try {
      myEventsProcessorImpl.createEvent(myEventRequest, badUser);
      fail();
    } catch (AdminOnlyRouteException e) {
      // we're good
    }
  }

  // test proper event creation
  @Test
  public void testCreateEvent2() {
    // make the event
    EventDetails myEventDetails = new EventDetails("my event", "boston",
            new Timestamp(START_TIMESTAMP_TEST), new Timestamp(END_TIMESTAMP_TEST));
    CreateEventRequest myEventRequest = new CreateEventRequest("sample", 5,
            "sample thumbnail", myEventDetails);

    // mock the DB
    JWTData goodUser = Mockito.mock(JWTData.class);
    when(goodUser.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

    EventsRecord record = myJooqMock.getContext().newRecord(Tables.EVENTS);
    record.setId(0);
    record.setCapacity(myEventRequest.getSpotsAvailable());
    myJooqMock.addReturn("INSERT", record);
    myJooqMock.addReturn("SELECT", record);

    SingleEventResponse res = myEventsProcessorImpl.createEvent(myEventRequest, goodUser);
    assertEquals(res.getId(), 0);
    assertEquals(res.getTitle(), "sample");
    fail("Reminder to fix this bug");
    // this is also probably a bug
    // assertEquals(res.getSpotsAvailable(), 5);
    assertEquals(res.getThumbnail(), "sample thumbnail");
    assertEquals(res.getDetails().getDescription(), myEventDetails.getDescription());
    assertEquals(res.getDetails().getLocation(), myEventDetails.getLocation());
    assertEquals(res.getDetails().getEnd(), myEventDetails.getEnd());
    assertEquals(res.getDetails().getStart(), myEventDetails.getStart());
  } 

  // test getting an event id that's not there
  @Test
  public void testGetSingleEvent1() {
    // mock the DB
    List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
    myJooqMock.addReturn("SELECT", emptySelectStatement);

    try {
      myEventsProcessorImpl.getSingleEvent(5);
    } catch (NullPointerException e) {
      // we're good
    }
  }

  // test getting an event id that's indeed there
  @Test
  public void testGetSingleEvent2() {
    // create the event
    EventDetails myEventDetails = new EventDetails("my event", "boston",
            new Timestamp(START_TIMESTAMP_TEST), new Timestamp(END_TIMESTAMP_TEST));
    CreateEventRequest myEventRequest = new CreateEventRequest("sample", 5,
            "sample thumbnail", myEventDetails);

    // mock the DB
    EventsRecord record = myJooqMock.getContext().newRecord(Tables.EVENTS);
    record.setId(1);
    record.setThumbnail(myEventRequest.getThumbnail());
    record.setTitle(myEventRequest.getTitle());
    record.setCapacity(myEventRequest.getSpotsAvailable());
    record.setLocation(myEventDetails.getLocation());
    record.setDescription(myEventDetails.getDescription());
    record.setStartTime(myEventDetails.getStart());
    record.setEndTime(myEventDetails.getEnd());

    myJooqMock.addReturn("SELECT", record);
    myJooqMock.addReturn("INSERT", record);

    SingleEventResponse res = myEventsProcessorImpl.getSingleEvent(1);

    assertEquals(res.getId(), 1);
    assertEquals(res.getDetails().getStart(), myEventDetails.getStart());
    assertEquals(res.getDetails().getEnd(), myEventDetails.getEnd());
    assertEquals(res.getDetails().getLocation(), myEventDetails.getLocation());
    assertEquals(res.getDetails().getDescription(), myEventDetails.getDescription());
    assertEquals(res.getSpotsAvailable(), myEventRequest.getSpotsAvailable());
    assertEquals(res.getThumbnail(), myEventRequest.getThumbnail());
    assertEquals(res.getTitle(), myEventRequest.getTitle());
  }

  // TODO
  @Test
  public void testGetEvents() {
    fail();
  }

  // TODO
  @Test
  public void testGetEventsSignedUp() {
    fail();
  }

  // TODO
  @Test
  public void testGetEventsQualified() {
    fail();
  }
}
