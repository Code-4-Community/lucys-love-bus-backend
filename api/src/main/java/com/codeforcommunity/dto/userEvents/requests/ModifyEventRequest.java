package com.codeforcommunity.dto.userEvents.requests;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.dto.userEvents.components.EventDetails;

public class ModifyEventRequest implements ApiDto {
  private String title;
  private Integer spotsAvailable;
  private String thumbnail;
  private EventDetails details;

  private ModifyEventRequest() {}

  public ModifyEventRequest(
      String title, Integer spotsAvailable, String thumbnail, EventDetails details) {
    this.title = title;
    this.spotsAvailable = spotsAvailable;
    this.thumbnail = thumbnail;
    this.details = details;
  }

  public String getTitle() {
    return title;
  }

  public Integer getSpotsAvailable() {
    return spotsAvailable;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public EventDetails getDetails() {
    return details;
  }

  @Override
  public void validate() {}
}
