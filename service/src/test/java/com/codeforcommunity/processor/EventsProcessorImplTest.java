package com.codeforcommunity.processor;

import com.codeforcommunity.JooqMock;
import org.jooq.DSLContext;
import com.codeforcommunity.auth.JWTCreator;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.events.*;

import org.junit.Before;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.sql.Timestamp;

import com.codeforcommunity.dto.auth.*;
import com.codeforcommunity.exceptions.*;

// Contains tests for EventsProcessorImplTest.java in main
public class EventsProcessorImplTest {
  JooqMock myJooqMock;
  EventsProcessorImpl myEventsProcessorImpl;

  // set up all the mocks
  @Before
  public void setup() {
    this.myJooqMock = new JooqMock();
    this.myEventsProcessorImpl = new EventsProcessorImpl(myJooqMock.getContext());
  }

  // test exception thrown for not being an admin
  @Test(expected = AdminOnlyRouteException.class)
  public void testCreateEvent1() {
    CreateEventRequest myEventRequest = new CreateEventRequest("sample", 5, "sample thumbnail", null);
    JWTData badUser = Mockito.mock(JWTData.class);

    when(myEventsProcessorImpl.createEvent(myEventRequest, badUser))
        .thenThrow(new AdminOnlyRouteException());
  }

  // test proper event creation
  @Test
  public void testCreateEvent2() {
    CreateEventRequest myEventRequest = new CreateEventRequest("sample", 5, "sample thumbnail", null);
    JWTData goodUser = Mockito.mock(JWTData.class);

    SingleEventResponse res = myEventsProcessorImpl.createEvent(myEventRequest, goodUser);

    assertEquals(res.getTitle(), "sample");
    assertEquals(res.getSpotsAvailable(), 5);
    assertEquals(res.getThumbnail(), "sample thumbnail");
    assertEquals(res.getDetails(), null);
  }

  // test getting an event id that's not there
  @Test(expected = NullPointerException.class)
  public void testGetSingleEvent1() {
    when(myEventsProcessorImpl.getSingleEvent(5))
        .thenThrow(new NullPointerException());
  }

  // test getting an event id that's indeed there
  @Test
  public void testGetSingleEvent2() {
    SingleEventResponse res = myEventsProcessorImpl.getSingleEvent(1);
    
    int startTime = 100;
    int endTime = 200;

    assertEquals(res.getId(), 1);
    assertEquals(res.getDetails(), new EventDetails("relevant detail", "relevant location", new Timestamp(startTime), new Timestamp(endTime)));
    assertEquals(res.getSpotsAvailable(), 5);
    assertEquals(res.getThumbnail(), "relevant thumbnail");
    assertEquals(res.getTitle(), "relevant title");
  }
}
