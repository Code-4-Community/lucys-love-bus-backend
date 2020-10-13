package com.codeforcommunity.api;

import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import java.util.List;

public interface IPublicEventsProcessor {

  GetEventsResponse getEvents(List<Integer> event);
}
