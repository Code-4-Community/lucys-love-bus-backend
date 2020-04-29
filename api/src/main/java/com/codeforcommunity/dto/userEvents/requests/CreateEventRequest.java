package com.codeforcommunity.dto.userEvents.requests;

import com.codeforcommunity.dto.userEvents.components.EventDetails;

public class CreateEventRequest {
  private String title;
  private int spotsAvailable;
  private String thumbnail;
  private EventDetails details;

  public CreateEventRequest(String title, int spotsAvailable, String thumbnail, EventDetails details) {
    this.title = title;
    this.spotsAvailable = spotsAvailable;
    this.thumbnail = thumbnail;
    this.details = details;
  }

  private CreateEventRequest() {}

  public String getTitle() {
    return title;
  }

  public int getSpotsAvailable() {
    return spotsAvailable;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public EventDetails getDetails() {
    return details;
  }
}
