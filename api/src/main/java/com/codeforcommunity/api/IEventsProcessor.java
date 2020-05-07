package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.dto.userEvents.requests.GetUserEventsRequest;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.exceptions.BadRequestImageException;

import java.io.IOException;
import java.util.List;

public interface IEventsProcessor {

  SingleEventResponse createEvent(CreateEventRequest request, JWTData userData) throws BadRequestImageException;

  SingleEventResponse getSingleEvent(int eventId);

  GetEventsResponse getEvents(List<Integer> event);

  GetEventsResponse getEventsSignedUp(GetUserEventsRequest request, JWTData userData);

  GetEventsResponse getEventsQualified(JWTData userData);

}
