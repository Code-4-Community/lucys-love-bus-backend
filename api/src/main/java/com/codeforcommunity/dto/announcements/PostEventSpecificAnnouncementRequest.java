package com.codeforcommunity.dto.announcements;

import com.codeforcommunity.exceptions.MalformedParameterException;

public class PostEventSpecificAnnouncementRequest {

  private int eventId;
  private String title;
  private String description;

  private PostEventSpecificAnnouncementRequest() {}

  public PostEventSpecificAnnouncementRequest(int eventId, String title, String description) {
    this.eventId = eventId;
    this.title = title;
    this.description = description;
  }

  public int getEventId() {
    return eventId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
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
