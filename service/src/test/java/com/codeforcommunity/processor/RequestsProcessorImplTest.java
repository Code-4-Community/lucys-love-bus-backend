package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.PF_REQUESTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.JooqMock.OperationType;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.pfrequests.RequestData;
import com.codeforcommunity.dto.pfrequests.RequestStatusData;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.enums.RequestStatus;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import com.codeforcommunity.exceptions.OutstandingRequestException;
import com.codeforcommunity.exceptions.WrongPrivilegeException;
import com.codeforcommunity.requester.Emailer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.jooq.Record3;
import org.jooq.Record6;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.PfRequestsRecord;
import org.jooq.generated.tables.records.UsersRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Contains tests for RequestsProcessorImpl.java in main
public class RequestsProcessorImplTest {

  private JooqMock myJooqMock;
  private RequestsProcessorImpl myRequestsProcessorImpl;
  private Emailer mockEmailer;

  // set up all the mocks
  @BeforeEach
  public void setup() {
    this.myJooqMock = new JooqMock();
    this.mockEmailer = mock(Emailer.class);
    this.myRequestsProcessorImpl = new RequestsProcessorImpl(myJooqMock.getContext(), mockEmailer);
  }

  // test creating a request that fails because user isn't GP
  @Test
  public void testCreateRequest1() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

    try {
      myRequestsProcessorImpl.createRequest(myUserData);
      fail();
    } catch (WrongPrivilegeException e) {
      assertEquals(e.getRequiredPrivilegeLevel(), PrivilegeLevel.STANDARD);
    }
  }

  // test creating a request that fails because user has outstanding requests
  @Test
  public void testCreateRequest2() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.STANDARD);
    when(myUserData.getUserId()).thenReturn(0);

    // mock the DB
    PfRequestsRecord myPFReqRecord = myJooqMock.getContext().newRecord(Tables.PF_REQUESTS);
    myPFReqRecord.setUserId(0);
    myPFReqRecord.setStatus(RequestStatus.PENDING);
    myJooqMock.addReturn(OperationType.SELECT, myPFReqRecord);

    try {
      myRequestsProcessorImpl.createRequest(myUserData);
      fail();
    } catch (OutstandingRequestException e) {
      // we're good
    }
  }

  // test properly creating a request
  @Test
  public void testCreateRequest3() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.STANDARD);
    when(myUserData.getUserId()).thenReturn(0);

    // mock the DB
    PfRequestsRecord myPFReqRecord = myJooqMock.getContext().newRecord(Tables.PF_REQUESTS);
    myPFReqRecord.setUserId(0);
    myPFReqRecord.setStatus(RequestStatus.APPROVED);
    myJooqMock.addReturn(OperationType.SELECT, myPFReqRecord);
    myJooqMock.addReturn(OperationType.INSERT, myPFReqRecord);

    myRequestsProcessorImpl.createRequest(myUserData);
  }

  // getting the requests fails if the user isn't an admin
  @Test
  public void testGetRequests1() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.STANDARD);
    when(myUserData.getUserId()).thenReturn(0);

    try {
      myRequestsProcessorImpl.getRequests(myUserData);
      fail();
    } catch (AdminOnlyRouteException e) {
      // we're good
    }
  }

  // test for empty list of requests
  @Test
  public void testGetRequests2() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);
    when(myUserData.getUserId()).thenReturn(0);

    // mock the DB
    myJooqMock.addEmptyReturn(OperationType.SELECT);

    assertEquals(myRequestsProcessorImpl.getRequests(myUserData).size(), 0);
  }

  // test for singleton list of requests
  @Test
  public void testGetRequests3() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

    // mock the DB for PF requests
    Record6<Integer, Integer, String, String, String, String> record =
        myJooqMock
            .getContext()
            .newRecord(
                Tables.PF_REQUESTS.ID,
                Tables.PF_REQUESTS.USER_ID,
                Tables.CONTACTS.EMAIL,
                Tables.CONTACTS.FIRST_NAME,
                Tables.CONTACTS.LAST_NAME,
                Tables.CONTACTS.PHONE_NUMBER);
    record.values(0, 0, "brandon@example.com", "Brandon", "Liang", "555-555-5555");
    myJooqMock.addReturn(OperationType.SELECT, record);

    List<RequestData> reqs = myRequestsProcessorImpl.getRequests(myUserData);

    assertEquals(reqs.size(), 1);
    assertEquals(reqs.get(0).getId(), 0);
    assertEquals(reqs.get(0).getUser().getId(), 0);
    assertEquals(reqs.get(0).getUser().getEmail(), "brandon@example.com");
    assertEquals(reqs.get(0).getUser().getFirstName(), "Brandon");
    assertEquals(reqs.get(0).getUser().getLastName(), "Liang");
    assertEquals(reqs.get(0).getUser().getPhoneNumber(), "555-555-5555");
  }

  // test for list of requests with multiple elements
  @Test
  public void testGetRequests4() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

    // mock the DB for PF requests
    Record6<Integer, Integer, String, String, String, String> record1 =
        myJooqMock
            .getContext()
            .newRecord(
                Tables.PF_REQUESTS.ID,
                Tables.PF_REQUESTS.USER_ID,
                Tables.CONTACTS.EMAIL,
                Tables.CONTACTS.FIRST_NAME,
                Tables.CONTACTS.LAST_NAME,
                Tables.CONTACTS.PHONE_NUMBER);
    record1.values(0, 0, "brandon@example.com", "Brandon", "Liang", "555-555-5555");

    Record6<Integer, Integer, String, String, String, String> record2 =
        myJooqMock
            .getContext()
            .newRecord(
                Tables.PF_REQUESTS.ID,
                Tables.PF_REQUESTS.USER_ID,
                Tables.CONTACTS.EMAIL,
                Tables.CONTACTS.FIRST_NAME,
                Tables.CONTACTS.LAST_NAME,
                Tables.CONTACTS.PHONE_NUMBER);
    record2.values(1, 2, "conner@example.com", "Conner", "Nilsen", "222-323-9090");

    List<Record6> records = new ArrayList<>();
    records.add(record1);
    records.add(record2);
    myJooqMock.addReturn(OperationType.SELECT, records);

    List<RequestData> reqs = myRequestsProcessorImpl.getRequests(myUserData);

    assertEquals(reqs.size(), 2);

    assertEquals(reqs.get(0).getId(), 0);
    assertEquals(reqs.get(0).getUser().getId(), 0);
    assertEquals(reqs.get(0).getUser().getEmail(), "brandon@example.com");
    assertEquals(reqs.get(0).getUser().getFirstName(), "Brandon");
    assertEquals(reqs.get(0).getUser().getLastName(), "Liang");
    assertEquals(reqs.get(0).getUser().getPhoneNumber(), "555-555-5555");

    assertEquals(reqs.get(1).getId(), 1);
    assertEquals(reqs.get(1).getUser().getId(), 2);
    assertEquals(reqs.get(1).getUser().getEmail(), "conner@example.com");
    assertEquals(reqs.get(1).getUser().getFirstName(), "Conner");
    assertEquals(reqs.get(1).getUser().getLastName(), "Nilsen");
    assertEquals(reqs.get(1).getUser().getPhoneNumber(), "222-323-9090");
  }

  // general users can't approve requests
  @Test
  public void testApproveRequest1() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.STANDARD);

    try {
      myRequestsProcessorImpl.approveRequest(0, myUserData);
      fail();
    } catch (AdminOnlyRouteException e) {
      // we're good
    }
  }

  // test approving a pending request
  @Test
  public void testApproveRequest2() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

    // mock the DB for PF requests
    PfRequestsRecord myPFReqRecord = myJooqMock.getContext().newRecord(Tables.PF_REQUESTS);
    myPFReqRecord.setUserId(0);
    myPFReqRecord.setId(0);
    myPFReqRecord.setStatus(RequestStatus.PENDING);
    myJooqMock.addReturn(OperationType.SELECT, myPFReqRecord);
    myJooqMock.addReturn(OperationType.UPDATE, myPFReqRecord);

    // mock the DB for a user
    UsersRecord myUserRecord = myJooqMock.getContext().newRecord(Tables.USERS);
    myUserRecord.setId(0);
    myUserRecord.setPrivilegeLevel(PrivilegeLevel.ADMIN);
    myJooqMock.addReturn(OperationType.UPDATE, myUserRecord);

    myRequestsProcessorImpl.approveRequest(0, myUserData);

    assertEquals(
        myJooqMock.getSqlOperationBindings().get(OperationType.UPDATE).get(0)[0],
        RequestStatus.APPROVED.getVal());
    assertEquals(
        myJooqMock.getSqlOperationBindings().get(OperationType.UPDATE).get(0)[1],
        myUserRecord.getId());
    assertEquals(
        myJooqMock.getSqlOperationBindings().get(OperationType.UPDATE).get(1)[0],
        PrivilegeLevel.PF.ordinal());
    assertEquals(
        myJooqMock.getSqlOperationBindings().get(OperationType.UPDATE).get(1)[1],
        myUserRecord.getId());
  }

  // general users can't reject requests
  @Test
  public void testRejectRequest1() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.STANDARD);

    try {
      myRequestsProcessorImpl.rejectRequest(0, myUserData);
      fail();
    } catch (AdminOnlyRouteException e) {
      // we're good
    }
  }

  // test rejecting a pending request
  @Test
  public void testRejectRequest2() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

    // mock the DB for PF requests
    PfRequestsRecord myPFReqRecord = myJooqMock.getContext().newRecord(Tables.PF_REQUESTS);
    myPFReqRecord.setUserId(0);
    myPFReqRecord.setId(0);
    myPFReqRecord.setStatus(RequestStatus.PENDING);
    myJooqMock.addReturn(OperationType.SELECT, myPFReqRecord);
    myJooqMock.addReturn(OperationType.UPDATE, myPFReqRecord);

    myRequestsProcessorImpl.rejectRequest(0, myUserData);

    assertEquals(
        myJooqMock.getSqlOperationBindings().get(OperationType.UPDATE).get(0)[0],
        RequestStatus.REJECTED.getVal());
    assertEquals(
        myJooqMock.getSqlOperationBindings().get(OperationType.UPDATE).get(0)[1],
        myPFReqRecord.getUserId());
  }

  // test getting request statuses when there are none
  @Test
  public void testGetRequestStatus1() {
    JWTData myUserData = new JWTData(1, PrivilegeLevel.STANDARD);

    myJooqMock.addEmptyReturn(OperationType.SELECT);

    List<RequestStatusData> statuses = myRequestsProcessorImpl.getRequestStatuses(myUserData);
    assertTrue(statuses.isEmpty());
  }

  // test getting request statuses when there is one
  @Test
  public void testGetRequestStatus2() {
    JWTData myUserData = new JWTData(1, PrivilegeLevel.STANDARD);

    // mock the DB for PF requests
    Record3<Integer, RequestStatus, Timestamp> myPFReqRecord =
        myJooqMock.getContext().newRecord(PF_REQUESTS.ID, PF_REQUESTS.STATUS, PF_REQUESTS.CREATED);
    myPFReqRecord.values(1, RequestStatus.PENDING, new Timestamp(0));
    myJooqMock.addReturn(OperationType.SELECT, myPFReqRecord);

    List<RequestStatusData> statuses = myRequestsProcessorImpl.getRequestStatuses(myUserData);
    assertEquals(1, statuses.size());
    assertEquals(1, statuses.get(0).getId());
    assertEquals(RequestStatus.PENDING, statuses.get(0).getStatus());
    assertEquals(new Timestamp(0), statuses.get(0).getCreated());
  }

  // test getting request statuses when there are multiple
  @Test
  public void testGetRequestStatus3() {
    JWTData myUserData = new JWTData(1, PrivilegeLevel.STANDARD);

    // mock the DB for PF requests
    Record3<Integer, RequestStatus, Timestamp> myPFReqRecord1 =
        myJooqMock.getContext().newRecord(PF_REQUESTS.ID, PF_REQUESTS.STATUS, PF_REQUESTS.CREATED);
    myPFReqRecord1.values(1, RequestStatus.APPROVED, new Timestamp(0));

    Record3<Integer, RequestStatus, Timestamp> myPFReqRecord2 =
        myJooqMock.getContext().newRecord(PF_REQUESTS.ID, PF_REQUESTS.STATUS, PF_REQUESTS.CREATED);
    myPFReqRecord2.values(2, RequestStatus.REJECTED, new Timestamp(1000));

    List<Record3<Integer, RequestStatus, Timestamp>> myPFReqRecords = new ArrayList<>();
    myPFReqRecords.add(myPFReqRecord1);
    myPFReqRecords.add(myPFReqRecord2);
    myJooqMock.addReturn(OperationType.SELECT, myPFReqRecords);

    List<RequestStatusData> statuses = myRequestsProcessorImpl.getRequestStatuses(myUserData);
    assertEquals(2, statuses.size());
    assertEquals(1, statuses.get(0).getId());
    assertEquals(RequestStatus.APPROVED, statuses.get(0).getStatus());
    assertEquals(new Timestamp(0), statuses.get(0).getCreated());
    assertEquals(2, statuses.get(1).getId());
    assertEquals(RequestStatus.REJECTED, statuses.get(1).getStatus());
    assertEquals(new Timestamp(1000), statuses.get(1).getCreated());
  }
}
