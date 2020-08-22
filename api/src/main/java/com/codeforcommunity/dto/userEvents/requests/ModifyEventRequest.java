package com.codeforcommunity.dto.userEvents.requests;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import java.util.ArrayList;
import java.util.List;

public class ModifyEventRequest extends ApiDto {
  private String title;
  private Integer spotsAvailable;
  private String thumbnail;
  private EventDetails details;
  private float price; // price in cents

  private ModifyEventRequest() {}

  public ModifyEventRequest(
      String title, Integer spotsAvailable, String thumbnail, EventDetails details, float price) {
    this.title = title;
    this.spotsAvailable = spotsAvailable;
    this.thumbnail = thumbnail;
    this.details = details;
    this.price = price;
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

  public Integer getPrice() {
    return Math.round(this.price * 100);
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "modify_event_request.";
    List<String> fields = new ArrayList<>();
    if (title != null && title.trim().isEmpty()) {
      fields.add(fieldName + "title");
    }
    if (spotsAvailable != null && spotsAvailable < 1) {
      fields.add(fieldName + "spots_available");
    }
    if (details != null) {
      fields.addAll(details.validateFields(fieldName, true));
    }
    return fields;
  }
}
