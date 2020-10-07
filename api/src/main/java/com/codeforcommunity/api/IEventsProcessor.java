package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.userEvents.requests.GetUserEventsRequest;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import java.util.List;

public interface IEventsProcessor {

  SingleEventResponse getSingleEvent(int eventId, JWTData userData);

  GetEventsResponse getEvents(List<Integer> event, JWTData userData);

  GetEventsResponse getEventsSignedUp(GetUserEventsRequest request, JWTData userData);

  GetEventsResponse getEventsQualified(JWTData userData);
}
