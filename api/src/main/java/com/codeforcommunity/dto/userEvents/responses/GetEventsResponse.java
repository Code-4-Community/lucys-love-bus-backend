package com.codeforcommunity.dto.userEvents.responses;

import com.codeforcommunity.dto.userEvents.components.Event;
import java.util.List;

public class GetEventsResponse {
  private List<Event> events;
  private int totalCount;

  public GetEventsResponse(List<Event> events, int totalCount) {
    this.events = events;
    this.totalCount = totalCount;
  }

  public List<Event> getEvents() {
    return events;
  }

  public void setEvents(List<Event> events) {
    this.events = events;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }
}
