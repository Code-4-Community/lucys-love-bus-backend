package com.codeforcommunity.dto.userEvents.requests;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import com.codeforcommunity.exceptions.MalformedParameterException;

public class CreateEventRequest implements ApiDto {
  private String title;
  private Integer spotsAvailable;
  private String thumbnail;
  private EventDetails details;

  public CreateEventRequest(
      String title, int spotsAvailable, String thumbnail, EventDetails details) {
    this.title = title;
    this.spotsAvailable = spotsAvailable;
    this.thumbnail = thumbnail;
    this.details = details;
  }

  private CreateEventRequest() {}

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

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

  @Override
  public void validate() {
    if (title == null) {
      throw new MalformedParameterException("Title");
    }
    if (spotsAvailable == null) {
      throw new MalformedParameterException("Spots available");
    }
    // TODO: can the thumbnail be null?
    if (details == null) {
      throw new MalformedParameterException("Details");
    }
    details.validate();
  }
}
