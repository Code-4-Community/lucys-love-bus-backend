package com.codeforcommunity.dto.announcements;

public class GetEventSpecificAnnouncementsRequest {

  private Integer eventId;

  private GetEventSpecificAnnouncementsRequest() {}

  public GetEventSpecificAnnouncementsRequest(Integer eventId) {
    this.eventId = eventId;
  }

  public Integer getEventId() {
    return eventId;
  }
}
