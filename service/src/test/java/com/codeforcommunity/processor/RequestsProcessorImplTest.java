package com.codeforcommunity.processor;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.auth.JWTCreator;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.pfrequests.CreateRequest;
import com.codeforcommunity.dto.pfrequests.RequestData;
import com.codeforcommunity.enums.RequestStatus;

import java.util.*;

import org.jooq.Record3;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.PfRequestsRecord;
import org.jooq.generated.tables.records.UsersRecord;
import org.jooq.impl.UpdatableRecordImpl;
import org.junit.Before;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

import com.codeforcommunity.dto.auth.*;
import com.codeforcommunity.exceptions.*;

// Contains tests for RequestsProcessorImpl.java in main
public class RequestsProcessorImplTest {
  JooqMock myJooqMock;
  RequestsProcessorImpl myRequestsProcessorImpl;

  // set up all the mocks
  @Before
  public void setup() {
    this.myJooqMock = new JooqMock();
    this.myRequestsProcessorImpl = new RequestsProcessorImpl(myJooqMock.getContext());
  }

  // test creating a request that fails because user isn't GP
  @Test
  public void testCreateRequest1() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

    CreateRequest myRequest = new CreateRequest("sample description");

    try {
      myRequestsProcessorImpl.createRequest(myRequest, myUserData);
      fail();
    } catch (WrongPrivilegeException e) {
      assertEquals(e.getRequiredPrivilegeLevel(), PrivilegeLevel.GP);
    }
  }

  // test creating a request that fails because user has outstanding requests
  @Test
  public void testCreateRequest2() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.GP);
    when(myUserData.getUserId()).thenReturn(0);

    // mock the DB
    PfRequestsRecord myPFReqRecord = myJooqMock.getContext().newRecord(Tables.PF_REQUESTS);
    myPFReqRecord.setUserId(0);
    myPFReqRecord.setStatus(RequestStatus.PENDING);
    myJooqMock.addReturn("SELECT", myPFReqRecord);

    CreateRequest myRequest = new CreateRequest("sample description");

    try {
      myRequestsProcessorImpl.createRequest(myRequest, myUserData);
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
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.GP);
    when(myUserData.getUserId()).thenReturn(0);

    // mock the DB
    PfRequestsRecord myPFReqRecord = myJooqMock.getContext().newRecord(Tables.PF_REQUESTS);
    myPFReqRecord.setUserId(0);
    myPFReqRecord.setStatus(RequestStatus.APPROVED);
    myJooqMock.addReturn("SELECT", myPFReqRecord);
    myJooqMock.addReturn("INSERT", myPFReqRecord);

    CreateRequest myRequest = new CreateRequest("sample description");
    myRequestsProcessorImpl.createRequest(myRequest, myUserData);
  }

  // getting the requests fails if the user isn't an admin
  @Test
  public void testGetRequests1() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.GP);
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
    List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
    myJooqMock.addReturn("SELECT", emptySelectStatement);

    assertEquals(myRequestsProcessorImpl.getRequests(myUserData).size(), 0);
  }

  // test for singleton list of requests
  @Test
  public void testGetRequests3() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

    // mock the DB for PF requests
    PfRequestsRecord myPFReqRecord = myJooqMock.getContext().newRecord(Tables.PF_REQUESTS);
    myPFReqRecord.setId(0);
    // getDescription() corresponds to user id, for some awkward reason
    myPFReqRecord.setUserId(2);
    // and getUserEmail() corresponds to this description
    myPFReqRecord.setDescription("sample description");
    myPFReqRecord.setStatus(RequestStatus.PENDING);
    myJooqMock.addReturn("SELECT", myPFReqRecord);

    Record3<Integer, String, String> recordImpl = mock(Record3.class);
    when(recordImpl.component1()).thenReturn(myPFReqRecord.getId());
    when(recordImpl.component2()).thenReturn(myPFReqRecord.getDescription());
    when(recordImpl.component3()).thenReturn("brandon@example.com");
    myJooqMock.addReturn("SELECT", recordImpl);

    List<RequestData> reqs = myRequestsProcessorImpl.getRequests(myUserData);

    // TODO: make these work
    assertEquals(reqs.size(), 1);
    assertEquals(reqs.get(0).getId(), 0);
    fail("Reminder to fix this test!");
    // assertEquals(reqs.get(0).getDescription(), "sample description");
    // returns "2"
    // assertEquals(reqs.get(0).getUserEmail(), "brandon@example.com");
    // returns "sample description"
  }

  // test for list of requests with multiple elements
  @Test
  public void testGetRequests4() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

    // mock the DB for PF requests
    PfRequestsRecord myPFReqRecord = myJooqMock.getContext().newRecord(Tables.PF_REQUESTS);
    myPFReqRecord.setId(0);
    myPFReqRecord.setUserId(0);
    myPFReqRecord.setDescription("sample description 1");
    myPFReqRecord.setStatus(RequestStatus.PENDING);

    PfRequestsRecord myPFReqRecord2 = myJooqMock.getContext().newRecord(Tables.PF_REQUESTS);
    myPFReqRecord2.setId(1);
    myPFReqRecord2.setUserId(1);
    myPFReqRecord2.setDescription("sample description 2");
    myPFReqRecord2.setStatus(RequestStatus.PENDING);

    List<UpdatableRecordImpl> records = new ArrayList<UpdatableRecordImpl>();
    records.add(myPFReqRecord);
    records.add(myPFReqRecord2);
    myJooqMock.addReturn("SELECT", records);
    myJooqMock.addReturn("INSERT", records);

    List<RequestData> reqs = myRequestsProcessorImpl.getRequests(myUserData);

    assertEquals(reqs.size(), 2);
    fail("Reminder to fix this test!");
    // TODO: add assertEquals for more of the properties like the method above
  }

  // general users can't approve requests
  @Test
  public void testApproveRequest1() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.GP);

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
    myPFReqRecord.setDescription("sample description");
    myPFReqRecord.setStatus(RequestStatus.PENDING);
    myJooqMock.addReturn("SELECT", myPFReqRecord);
    myJooqMock.addReturn("UPDATE", myPFReqRecord);

    // mock the DB for a user
    UsersRecord myUserRecord = myJooqMock.getContext().newRecord(Tables.USERS);
    myUserRecord.setId(0);
    myUserRecord.setPrivilegeLevel(PrivilegeLevel.ADMIN);
    myJooqMock.addReturn("UPDATE", myUserRecord);

    myRequestsProcessorImpl.approveRequest(0, myUserData);

    assertEquals(myJooqMock.getSqlBindings().get("UPDATE").get(0)[0], RequestStatus.APPROVED.getVal());
    assertEquals(myJooqMock.getSqlBindings().get("UPDATE").get(0)[1], myUserRecord.getId());
    assertEquals(myJooqMock.getSqlBindings().get("UPDATE").get(1)[0], PrivilegeLevel.PF.getVal());
    assertEquals(myJooqMock.getSqlBindings().get("UPDATE").get(1)[1], myUserRecord.getId());
  }

  // general users can't reject requests
  @Test
  public void testRejectRequest1() {
    // mock the user data
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.GP);

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
    myPFReqRecord.setDescription("sample description");
    myPFReqRecord.setStatus(RequestStatus.PENDING);
    myJooqMock.addReturn("SELECT", myPFReqRecord);
    myJooqMock.addReturn("UPDATE", myPFReqRecord);

    myRequestsProcessorImpl.rejectRequest(0, myUserData);

    assertEquals(myJooqMock.getSqlBindings().get("UPDATE").get(0)[0], RequestStatus.REJECTED.getVal());
    assertEquals(myJooqMock.getSqlBindings().get("UPDATE").get(0)[1], myPFReqRecord.getUserId());
  }

  // test for exception when getting request status from an unauthorized user
  @Test
  public void testGeRequestStatus1() {
    // mock a general user
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.GP);
    when(myUserData.getUserId()).thenReturn(1);

    // mock the DB for PF requests
    PfRequestsRecord myPFReqRecord = myJooqMock.getContext().newRecord(Tables.PF_REQUESTS);
    myPFReqRecord.setId(0);
    myPFReqRecord.setUserId(0);
    myJooqMock.addReturn("SELECT", myPFReqRecord);

    // seed the db with a user
    UsersRecord record = myJooqMock.getContext().newRecord(Tables.USERS);
    record.setId(0);
    myJooqMock.addReturn("SELECT", record);

    int testRequestID = 0;

    try {
      myRequestsProcessorImpl.geRequestStatus(testRequestID, myUserData);
      fail();
    } catch (ResourceNotOwnedException e) {
      assertEquals(e.getResource(), "request " + testRequestID);
    }
  }

  // test for pending request status
  @Test
  public void testGeRequestStatus2() {
    // mock a general user
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.GP);
    when(myUserData.getUserId()).thenReturn(0);

    // mock the DB for PF requests
    PfRequestsRecord myPFReqRecord = myJooqMock.getContext().newRecord(Tables.PF_REQUESTS);
    myPFReqRecord.setId(0);
    myPFReqRecord.setUserId(0);
    myPFReqRecord.setStatus(RequestStatus.PENDING);
    myJooqMock.addReturn("SELECT", myPFReqRecord);

    // seed the db with a user
    UsersRecord record = myJooqMock.getContext().newRecord(Tables.USERS);
    record.setId(0);
    myJooqMock.addReturn("SELECT", record);

    int testRequestID = 0;

    assertEquals(myRequestsProcessorImpl.geRequestStatus(testRequestID, myUserData), RequestStatus.PENDING);
  }

  // test for approved request status
  @Test
  public void testGeRequestStatus3() {
    // mock an admin user
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

    // mock the DB for PF requests
    PfRequestsRecord myPFReqRecord = myJooqMock.getContext().newRecord(Tables.PF_REQUESTS);
    myPFReqRecord.setId(0);
    myPFReqRecord.setStatus(RequestStatus.APPROVED);
    myJooqMock.addReturn("SELECT", myPFReqRecord);

    int testRequestID = 0;

    assertEquals(myRequestsProcessorImpl.geRequestStatus(testRequestID, myUserData), RequestStatus.APPROVED);
  }

  // test for rejected request status
  @Test
  public void testGeRequestStatus4() {
    // mock an admin user
    JWTData myUserData = mock(JWTData.class);
    when(myUserData.getPrivilegeLevel()).thenReturn(PrivilegeLevel.ADMIN);

    // mock the DB for PF requests
    PfRequestsRecord myPFReqRecord = myJooqMock.getContext().newRecord(Tables.PF_REQUESTS);
    myPFReqRecord.setId(0);
    myPFReqRecord.setStatus(RequestStatus.REJECTED);
    myJooqMock.addReturn("SELECT", myPFReqRecord);

    int testRequestID = 0;

    assertEquals(myRequestsProcessorImpl.geRequestStatus(testRequestID, myUserData), RequestStatus.REJECTED);
  }
}
