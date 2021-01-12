package com.codeforcommunity.api;

import com.codeforcommunity.dto.userEvents.responses.GetPublicEventsResponse;
import java.util.List;

public interface IPublicEventsProcessor {

  GetPublicEventsResponse getPublicEvents(List<Integer> eventIds);
}
