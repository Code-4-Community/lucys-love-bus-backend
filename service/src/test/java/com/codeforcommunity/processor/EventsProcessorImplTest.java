package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;
import static org.jooq.generated.Tables.USERS;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codeforcommunity.Base64TestStrings;
import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dataaccess.EventDatabaseOperations;
import com.codeforcommunity.dto.userEvents.components.Event;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import com.codeforcommunity.dto.userEvents.components.Registration;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.responses.EventRegistrations;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.EventDoesNotExistException;
import com.codeforcommunity.requester.S3Requester;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.EventsRecord;
import org.jooq.impl.UpdatableRecordImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// Contains tests for EventsProcessorImpl.java in main
public class EventsProcessorImplTest {
  private EventsProcessorImpl processor;
  private EventDatabaseOperations myEventDatabaseOperations;
  private JooqMock myJooqMock;

  // use UNIX time for ease of testing
  // 04/16/2020 @ 1:20am (UTC)
  private final int START_TIMESTAMP_TEST = 1587000000;
  // 04/17/2020 @ 5:06am (UTC)
  private final int END_TIMESTAMP_TEST = 1587100000;

  @BeforeEach
  private void setup() {
    this.myJooqMock = new JooqMock();
    this.myEventDatabaseOperations = new EventDatabaseOperations(myJooqMock.getContext());
    this.processor = new EventsProcessorImpl(myJooqMock.getContext());
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
    JWTData badUser = mock(JWTData.class);
    when(badUser.getPrivilegeLevel()).thenReturn(PrivilegeLevel.GP);

    try {
      processor.createEvent(myEventRequest, badUser);
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
        Base64TestStrings.TEST_STRING_1, myEventDetails);

    when(S3Requester.validateUploadImageToS3LucyEvents("sample", Base64TestStrings.TEST_STRING_1))
        .thenReturn("https://sampleurl.com");

    // mock the DB
    JWTData goodUser = mock(JWTData.class);
    when(goodUser.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

    EventsRecord record = myJooqMock.getContext().newRecord(Tables.EVENTS);
    record.setId(0);
    record.setCapacity(myEventRequest.getSpotsAvailable());
    myJooqMock.addReturn("INSERT", record);
    myJooqMock.addReturn("SELECT", record);

    SingleEventResponse res = processor.createEvent(myEventRequest, goodUser);

    assertEquals(res.getId(), 0);
    assertEquals(res.getTitle(), "sample");
    assertEquals(myEventDatabaseOperations.getSpotsLeft(0), 5);
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
      processor.getSingleEvent(5);
      fail();
    } catch (EventDoesNotExistException e) {
      assertEquals(e.getEventId(), 5);
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

    SingleEventResponse res = processor.getSingleEvent(1);

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

  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  public void testGetEventRegisteredUsersIncorrectPrivilegeLevel(int privLevel) {
    PrivilegeLevel level = PrivilegeLevel.from(privLevel);
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(level);

    try {
      processor.getEventRegisteredUsers(1, jwtData);
    }
    catch(AdminOnlyRouteException ignored) {}
  }

  @Test
  public void testGetEventsRegisteredUsersNoEvent() {
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);
    myJooqMock.addEmptyReturn("SELECT");

    try {
      processor.getEventRegisteredUsers(125, jwtData);
    }
    catch (EventDoesNotExistException e) {
      assertEquals(125, e.getEventId());
    }
  }

  @Test
  public void testGetEventsRegisteredUsersEmptyReturn() {
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);
    myJooqMock.addReturn("SELECT", new EventsRecord());

    myJooqMock.addEmptyReturn("SELECT");

    EventRegistrations regs1 = processor.getEventRegisteredUsers(1, jwtData);
    assertEquals(0, regs1.getRegistrations().size());
  }

  @Test
  public void testGetEventsRegisteredUsersSingleReturn() {
    int ticketCount = 5;
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);
    myJooqMock.addReturn("SELECT", new EventsRecord());

    Record4<String, String, String, Integer> result = myJooqMock.getContext().newRecord(
        USERS.FIRST_NAME, USERS.LAST_NAME, USERS.EMAIL, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    result.values("Conner", "Nilsen", "connernilsen@gmail.com", ticketCount);
    myJooqMock.addReturn("SELECT", result);

    EventRegistrations regs = processor.getEventRegisteredUsers(1, jwtData);

    assertEquals(1, regs.getRegistrations().size());
    Registration reg0 = regs.getRegistrations().get(0);
    assertEquals("Conner", reg0.getFirstName());
    assertEquals("Nilsen", reg0.getLastName());
    assertEquals("connernilsen@gmail.com", reg0.getEmail());
    assertEquals(ticketCount, reg0.getTicketCount());
  }

  @Test
  public void testGetEventsRegisteredUsersMultiReturn() {
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);
    myJooqMock.addReturn("SELECT", new EventsRecord());

    Result<Record4<String, String, String, Integer>> result = myJooqMock.getContext().newResult(
        USERS.FIRST_NAME, USERS.LAST_NAME, USERS.EMAIL, EVENT_REGISTRATIONS.TICKET_QUANTITY);

    for (int i = 1; i < 6; i++) {
      Record4<String, String, String, Integer> tempRes =
          myJooqMock.getContext().newRecord(
              USERS.FIRST_NAME, USERS.LAST_NAME, USERS.EMAIL, EVENT_REGISTRATIONS.TICKET_QUANTITY);
      tempRes.values("Conner" + i, "Nilsen" + i, "connernilsen@gmail.com" + i, i);
      result.add(tempRes);
    }
    myJooqMock.addReturn("SELECT", result);

    EventRegistrations regs = processor.getEventRegisteredUsers(1, jwtData);

    assertEquals(5, regs.getRegistrations().size());
    for (int i = 1; i < 6; i++) {
      Registration reg = regs.getRegistrations().get(i - 1);
      assertEquals("Conner" + i, reg.getFirstName());
      assertEquals("Nilsen" + i, reg.getLastName());
      assertEquals("connernilsen@gmail.com" + i, reg.getEmail());
      assertEquals(i, reg.getTicketCount());
    }
  }
}
