package com.codeforcommunity.dto.userEvents.responses;

import java.util.List;

public class GetPublicEventsResponse {
  private final List<PublicSingleEventResponse> events;
  private final int totalCount;

  public GetPublicEventsResponse(List<PublicSingleEventResponse> events, int totalCount) {
    this.events = events;
    this.totalCount = totalCount;
  }

  public List<PublicSingleEventResponse> getEvents() {
    return events;
  }

  public int getTotalCount() {
    return totalCount;
  }
}
