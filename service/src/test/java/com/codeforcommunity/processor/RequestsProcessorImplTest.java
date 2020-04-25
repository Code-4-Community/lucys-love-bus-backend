package com.codeforcommunity.processor;

import com.codeforcommunity.JooqMock;
import org.jooq.DSLContext;
import com.codeforcommunity.auth.JWTCreator;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.events.*;
import com.codeforcommunity.dto.pfrequests.CreateRequest;
import com.codeforcommunity.dto.pfrequests.RequestData;
import com.codeforcommunity.enums.RequestStatus;

import java.util.*;

import org.junit.Before;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import com.codeforcommunity.dto.auth.*;
import com.codeforcommunity.exceptions.*;

// Contains tests for RequestsProcessorImpl.java in main
public class RequestsProcessorImplTest {
  JooqMock myJooqMock;
  RequestsProcessorImpl myRequestsProcessorImpl = Mockito.mock(RequestsProcessorImpl.class);

  // set up all the mocks
  @Before
  public void setup() {
    this.myJooqMock = new JooqMock();
    this.myRequestsProcessorImpl = new RequestsProcessorImpl(myJooqMock.getContext());
  }
  
  // consider using the void methods in RequestsProcessorImpl.java to mutate the DB before running further tests

  /*
  // test for empty list of requests
  @Test 
  public void testGetRequests1() {
    List<RequestData> noRequest = new ArrayList<RequestData>();
    JWTData userDataWithNothing = Mockito.mock(JWTData.class);

    assertEquals(myRequestsProcessorImpl.getRequests(userDataWithNothing), noRequest);
  }

  // test for non-empty list of requests
  @Test 
  public void testGetRequests2() {
    List<RequestData> sampleRequests = new ArrayList<RequestData>();
    JWTData userDataWithSomething = Mockito.mock(JWTData.class);

    sampleRequests.add(new RequestData(5, "sample description", "sample@email.com"));

    assertEquals(myRequestsProcessorImpl.getRequests(userDataWithSomething), sampleRequests);
  }

  // test for exception when getting request status from an incorrect user
  @Test(expected = ResourceNotOwnedException.class)
  public void testGeRequestStatus1() {
    JWTData mismatchUserData = Mockito.mock(JWTData.class);

    int testRequestID = 1;

    when(myRequestsProcessorImpl.geRequestStatus(testRequestID, mismatchUserData))
        .thenThrow(new ResourceNotOwnedException("request " + testRequestID));
  }

  // test for pending request status
  @Test
  public void testGeRequestStatus2() {
    JWTData pendingUserData = Mockito.mock(JWTData.class);
    int testRequestID = 2;

    assertEquals(myRequestsProcessorImpl.geRequestStatus(testRequestID, pendingUserData), RequestStatus.PENDING);
  }

  // test for approved request status
  @Test
  public void testGeRequestStatus3() {
    JWTData approvedUserData = Mockito.mock(JWTData.class);
    int testRequestID = 3;

    assertEquals(myRequestsProcessorImpl.geRequestStatus(testRequestID, approvedUserData), RequestStatus.APPROVED);
  }

  // test for exception when getting request status from an incorrect user
  @Test(expected = ResourceNotOwnedException.class)
  public void testGeRequestStatus4() {
    JWTData rejectedUserData = Mockito.mock(JWTData.class);
    int testRequestID = 4;

    assertEquals(myRequestsProcessorImpl.geRequestStatus(testRequestID, rejectedUserData), RequestStatus.REJECTED);
  }
  */
}
