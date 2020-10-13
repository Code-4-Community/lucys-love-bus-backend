package com.codeforcommunity.dto.userEvents.responses;

import java.util.List;

/** A class to get the responses to events. */
public class GetEventsResponse {
  private List<SingleEventResponse> events;
  private int totalCount;

  public GetEventsResponse(List<SingleEventResponse> events, int totalCount) {
    this.events = events;
    this.totalCount = totalCount;
  }

  /**
   * Gets the events.
   *
   * @return a list of events
   */
  public List<SingleEventResponse> getEvents() {
    return events;
  }

  /**
   * Sets the events.
   *
   * @param events a list of events
   */
  public void setEvents(List<SingleEventResponse> events) {
    this.events = events;
  }

  /**
   * Gets the total count at the events
   *
   * @return the total count
   */
  public int getTotalCount() {
    return totalCount;
  }

  /**
   * Sets the total count.
   *
   * @param totalCount the total count
   */
  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }
}
