package com.codeforcommunity.dto.userEvents.responses;

import java.util.List;

public class GetEventsResponse {
  private List<SingleEventResponse> events;
  private int totalCount;

  public GetEventsResponse(List<SingleEventResponse> events, int totalCount) {
    this.events = events;
    this.totalCount = totalCount;
  }

  public List<SingleEventResponse> getEvents() {
    return events;
  }

  public void setEvents(List<SingleEventResponse> events) {
    this.events = events;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }
}
