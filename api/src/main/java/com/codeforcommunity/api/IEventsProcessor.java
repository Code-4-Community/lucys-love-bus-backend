package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.events.CreateEventRequest;
import com.codeforcommunity.dto.events.SingleEventResponse;
import com.codeforcommunity.dto.userEvents.components.Event;
import com.codeforcommunity.dto.userEvents.requests.GetUserEventsRequest;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;

import java.util.List;

public interface IEventsProcessor {

  SingleEventResponse createEvent(CreateEventRequest request, JWTData userData);

  SingleEventResponse getSingleEvent(int eventId); // todo think about this =

  GetEventsResponse getEvents(List<Event> event);

  GetEventsResponse getEventsSignedUp(GetUserEventsRequest request, JWTData userData);

  GetEventsResponse getEventsQualified(JWTData userData);

}
