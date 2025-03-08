package com.codeforcommunity.dto.userEvents.components;

import com.codeforcommunity.dto.ApiDto;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** A class to represent the details of an event. */
public class EventDetails extends ApiDto {
  private String description;
  private String privateDescription;
  private String location;
  private Timestamp start;
  private Timestamp end;

  public EventDetails(
      String description,
      String privateDescription,
      String location,
      Timestamp start,
      Timestamp end) {
    this.description = description;
    this.privateDescription = privateDescription;
    this.location = location;
    this.start = start;
    this.end = end;
  }

  private EventDetails() {}

  /**
   * Gets the event description.
   *
   * @return the event description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the given description as the description of the event
   *
   * @param description the description to be set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the event's private description.
   *
   * @return the event's private description
   */
  public String getPrivateDescription() {
    return privateDescription;
  }

  /**
   * Sets the given private description as the description of the event
   *
   * @param privateDescription the private description to be set
   */
  public void setPrivateDescription(String privateDescription) {
    this.privateDescription = privateDescription;
  }

  /**
   * Gets the location of the event.
   *
   * @return the location of the event
   */
  public String getLocation() {
    return location;
  }

  /**
   * Sets the given location as the location of the event.
   *
   * @param location the location to be set
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Gets the start time of the event.
   *
   * @return the start time
   */
  public Timestamp getStart() {
    return start;
  }

  /**
   * Sets the start time of the event.
   *
   * @param start the start time
   */
  public void setStart(Timestamp start) {
    this.start = start;
  }

  /**
   * Gets the end time of the event.
   *
   * @return the end time
   */
  public Timestamp getEnd() {
    return end;
  }

  /**
   * Sets the end time of the event.
   *
   * @param end the end time
   */
  public void setEnd(Timestamp end) {
    this.end = end;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "event_details.";
    List<String> fields = new ArrayList<>();
    if (description == null) {
      fields.add(fieldName + "description");
    }
    if (isEmpty(location) || location.length() > 120) {
      fields.add(fieldName + "location");
    }
    if (start == null || isStartInvalid(start)) {
      fields.add(fieldName + "start");
    }
    if (end == null) {
      fields.add(fieldName + "end");
    }
    if (start != null && end != null && start.after(end)) {
      fields.add(fieldName + "start/end");
    }
    return fields;
  }

  private boolean isStartInvalid(Timestamp start) {
    return start.before(Date.from(Instant.now()));
  }
}
