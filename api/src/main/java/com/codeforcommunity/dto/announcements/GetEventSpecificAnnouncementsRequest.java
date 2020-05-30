package com.codeforcommunity.dto.announcements;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.exceptions.MalformedParameterException;

public class GetEventSpecificAnnouncementsRequest implements ApiDto {

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
  @Override
  public void validate() {
    if (eventId < 1) {
      throw new MalformedParameterException("event_id");
    }
  }
}
