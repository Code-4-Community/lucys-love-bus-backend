package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.CONTACTS;
import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.userEvents.components.Registration;
import com.codeforcommunity.dto.userEvents.requests.GetUserEventsRequest;
import com.codeforcommunity.dto.userEvents.responses.EventRegistrations;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.EventDoesNotExistException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.jooq.Record2;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.generated.tables.records.EventsRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class EventsProcessorImplTest {
  private EventsProcessorImpl processor;
  private JooqMock mock;

  @BeforeEach
  private void setup() {
    this.mock = new JooqMock();
    this.processor = new EventsProcessorImpl(mock.getContext());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  public void testGetEventRegisteredUsersIncorrectPrivilegeLevel(int privLevel) {
    PrivilegeLevel level = PrivilegeLevel.from(privLevel);
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(level);

    try {
      processor.getEventRegisteredUsers(1, jwtData);
    } catch (AdminOnlyRouteException ignored) {
    }
  }

  @Test
  public void testGetEventsRegisteredUsersNoEvent() {
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);
    mock.addEmptyReturn("SELECT");

    try {
      processor.getEventRegisteredUsers(125, jwtData);
    } catch (EventDoesNotExistException e) {
      assertEquals(125, e.getEventId());
    }
  }

  @Test
  public void testGetEventsRegisteredUsersEmptyReturn() {
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);
    mock.addReturn("SELECT", new EventsRecord());

    mock.addEmptyReturn("SELECT");

    EventRegistrations regs1 = processor.getEventRegisteredUsers(1, jwtData);
    assertEquals(0, regs1.getRegistrations().size());
  }

  @Test
  public void testGetEventsRegisteredUsersSingleReturn() {
    int ticketCount = 5;
    JWTData jwtData = mock(JWTData.class);
    when(jwtData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);
    mock.addReturn("SELECT", new EventsRecord());

    Record4<String, String, String, Integer> result =
        mock.getContext()
            .newRecord(
                CONTACTS.FIRST_NAME,
                CONTACTS.LAST_NAME,
                CONTACTS.EMAIL,
                EVENT_REGISTRATIONS.TICKET_QUANTITY);
    result.values("Conner", "Nilsen", "connernilsen@gmail.com", ticketCount);
    mock.addReturn("SELECT", result);

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
    mock.addReturn("SELECT", new EventsRecord());

    Result<Record4<String, String, String, Integer>> result =
        mock.getContext()
            .newResult(
                CONTACTS.FIRST_NAME,
                CONTACTS.LAST_NAME,
                CONTACTS.EMAIL,
                EVENT_REGISTRATIONS.TICKET_QUANTITY);

    for (int i = 1; i < 6; i++) {
      Record4<String, String, String, Integer> tempRes =
          mock.getContext()
              .newRecord(
                  CONTACTS.FIRST_NAME,
                  CONTACTS.LAST_NAME,
                  CONTACTS.EMAIL,
                  EVENT_REGISTRATIONS.TICKET_QUANTITY);
      tempRes.values("Conner" + i, "Nilsen" + i, "connernilsen@gmail.com" + i, i);
      result.add(tempRes);
    }
    mock.addReturn("SELECT", result);

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

  private void prepSignedUp(Boolean signedUp) {
    EventsRecord eventResult = mock.getContext().newRecord(EVENTS);
    eventResult.setStartTime(Timestamp.from(Instant.now()));
    eventResult.setEndTime(Timestamp.from(Instant.now()));
    eventResult.setLocation("HERE");
    eventResult.setCapacity(5);
    eventResult.setDescription("DESC");
    eventResult.setTitle("TITLE");
    eventResult.setId(1);
    mock.addReturn("SELECT", eventResult);
    Result<Record2<Integer, Integer>> res =
        mock.getContext().newResult(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    Record2<Integer, Integer> record =
        mock.getContext().newRecord(EVENTS.ID, EVENT_REGISTRATIONS.TICKET_QUANTITY);
    record.value1(1);
    record.value2(signedUp ? 1 : 0);
    mock.addReturn("SELECT", record);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void testGetEventsSignedUpUserSignedUp(boolean signedUp) {
    prepSignedUp(signedUp);

    GetUserEventsRequest req =
        new GetUserEventsRequest(Optional.empty(), Optional.empty(), Optional.empty());
    JWTData data = new JWTData(1, PrivilegeLevel.GP);
    GetEventsResponse resp = processor.getEventsSignedUp(req, data);

    assertEquals(signedUp, resp.getEvents().get(0).getSignedUp());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void testGetEventsQualifiedUserSignedUp(boolean signedUp) {
    JWTData data = new JWTData(1, PrivilegeLevel.GP);

    prepSignedUp(signedUp);

    GetEventsResponse resp = processor.getEventsQualified(data);
    assertEquals(signedUp, resp.getEvents().get(0).getSignedUp());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void testGetSingleEventUserSignedUp(boolean signedUp) {
    JWTData data = new JWTData(1, PrivilegeLevel.GP);

    prepSignedUp(signedUp);
    GetEventsResponse resp = processor.getEventsQualified(data);
    assertEquals(signedUp, resp.getEvents().get(0).getSignedUp());
  }
}
