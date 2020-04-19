package com.codeforcommunity.dto.announcements;

import com.codeforcommunity.exceptions.MalformedParameterException;

public class GetEventSpecificAnnouncementsRequest {

  private int eventId;

  private GetEventSpecificAnnouncementsRequest() {}

  public GetEventSpecificAnnouncementsRequest(int eventId) {
    this.eventId = eventId;
  }

  public int getEventId() {
    return eventId;
  }

  /**
   * Validates the request.
   *
   * @throws MalformedParameterException if any of the request parameters are invalid
   */
  public void validate() {
    if (eventId < 1) {
      throw new MalformedParameterException("event_id");
    }
  }
}
