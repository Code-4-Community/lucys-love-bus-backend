package com.codeforcommunity.dto.userEvents.requests;

import com.codeforcommunity.dto.ApiDto;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import java.util.ArrayList;
import java.util.List;

/** A class to modify an event request */
public class ModifyEventRequest extends ApiDto {
  private String title;
  private Integer capacity;
  private String thumbnail;
  private EventDetails details;
  private Integer price; // price in cents
  private boolean forPFOnly;

  public ModifyEventRequest(
      String title, Integer capacity, String thumbnail, EventDetails details, Integer price, boolean forPFOnly) {
    this.title = title;
    this.capacity = capacity;
    this.thumbnail = thumbnail;
    this.details = details;
    this.price = price;
    this.forPFOnly = forPFOnly;
  }

  /**
   * Gets the title of the event.
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the number of spots available at the event.
   *
   * @return the number of available spots
   */
  public Integer getCapacity() {
    return capacity;
  }

  /**
   * Gets the thumbnail of the event.
   *
   * @return the thumbnail
   */
  public String getThumbnail() {
    return thumbnail;
  }

  /**
   * Gets the details of the event.
   *
   * @return the details of the event
   */
  public EventDetails getDetails() {
    return details;
  }

  /**
   * Gets the price of the event.
   *
   * @return the prices
   */
  public Integer getPrice() {
    return price;
  }

  /**
   * Gets whether the event is intended for participating families only
   *
   * @return whether the event is intended for participating families only
   */
  public boolean getForPFOnly() {
    return forPFOnly;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "modify_event_request.";
    List<String> fields = new ArrayList<>();
    if (title != null && title.trim().isEmpty()) {
      fields.add(fieldName + "title");
    }
    if (capacity != null && capacity < 1) {
      fields.add(fieldName + "spots_available");
    }
    if (details != null) {
      fields.addAll(details.validateFields(fieldName));
    }
    return fields;
  }
}
