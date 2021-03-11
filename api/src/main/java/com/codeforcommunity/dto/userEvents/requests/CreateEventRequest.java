package com.codeforcommunity.dto.userEvents.requests;

import com.codeforcommunity.dto.ApiDto;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import java.util.ArrayList;
import java.util.List;

/** A class to create an event request */
public class CreateEventRequest extends ApiDto {
  private String title;
  private Integer capacity;
  private String thumbnail;
  private EventDetails details;
  private int price; // price in cents

  public CreateEventRequest(
      String title, int capacity, String thumbnail, EventDetails details, int price) {
    this.title = title;
    this.capacity = capacity;
    this.thumbnail = thumbnail;
    this.details = details;
    this.price = price;
  }

  private CreateEventRequest() {}

  /**
   * Sets the thumbnail to be the given thumbnail
   *
   * @param thumbnail the thumbnail
   */
  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

  /**
   * Gets the title of the event request
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the number of spots available at the event
   *
   * @return the number of spots available
   */
  public int getCapacity() {
    return capacity;
  }

  /**
   * Gets the current thumbnail for the event request
   *
   * @return the thumbnail
   */
  public String getThumbnail() {
    return thumbnail;
  }

  /**
   * Gets the details of the event
   *
   * @return the details
   */
  public EventDetails getDetails() {
    return details;
  }

  /**
   * Gets the price of the event
   *
   * @return the prices
   */
  public int getPrice() {
    return price;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "create_event_request.";
    List<String> fields = new ArrayList<>();
    if (isEmpty(title) || title.length() > 36) {
      fields.add(fieldName + "title");
    }
    if (capacity == null || capacity < 0) {
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
