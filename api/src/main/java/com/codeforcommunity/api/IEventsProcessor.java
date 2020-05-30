package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.requests.GetUserEventsRequest;
import com.codeforcommunity.dto.userEvents.requests.ModifyEventRequest;
import com.codeforcommunity.dto.userEvents.responses.EventRegistrations;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.exceptions.BadRequestImageException;
import com.codeforcommunity.exceptions.S3FailedUploadException;
import java.util.List;

public interface IEventsProcessor {

  SingleEventResponse createEvent(CreateEventRequest request, JWTData userData)
      throws BadRequestImageException, S3FailedUploadException;

  SingleEventResponse getSingleEvent(int eventId);

  GetEventsResponse getEvents(List<Integer> event);

  GetEventsResponse getEventsSignedUp(GetUserEventsRequest request, JWTData userData);

  GetEventsResponse getEventsQualified(JWTData userData);

  SingleEventResponse modifyEvent(int eventId, ModifyEventRequest request, JWTData userData);

  void deleteEvent(int eventId, JWTData userData);

  EventRegistrations getEventRegisteredUsers(int eventId, JWTData userData);

  String getEventRSVPs(int eventId, JWTData userData);
}
