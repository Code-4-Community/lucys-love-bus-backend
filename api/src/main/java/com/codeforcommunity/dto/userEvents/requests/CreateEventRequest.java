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
  private float price; // price in cents

  public CreateEventRequest(
      String title, int spotsAvailable, String thumbnail, EventDetails details, float price) {
    this.title = title;
    this.spotsAvailable = spotsAvailable;
    this.thumbnail = thumbnail;
    this.details = details;
    this.price = price;
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

  public int getPrice() {
    return Math.round(this.price * 100);
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "create_event_request.";
    List<String> fields = new ArrayList<>();
    if (isEmpty(title) || title.length() > 36) {
      fields.add(fieldName + "title");
    }
    if (spotsAvailable == null || spotsAvailable < 0) {
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
