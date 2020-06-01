package com.codeforcommunity.dto.userEvents.requests;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import java.util.ArrayList;
import java.util.List;

public class CreateEventRequest extends ApiDto {
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
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "create_event_request.";
    List<String> fields = new ArrayList<>();
    if (isEmpty(title)) {
      fields.add(fieldName + "title");
    }
    if (spotsAvailable == null || spotsAvailable < 1) {
      fields.add(fieldName + "spots_available");
    }
    if (details == null) {
      fields.add(fieldName + "details");
    } else {
      fields.addAll(details.validateFields(fieldName));
    }
    return fields;
  }
}
