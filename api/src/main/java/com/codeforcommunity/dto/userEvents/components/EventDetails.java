package com.codeforcommunity.dto.userEvents.components;

import com.codeforcommunity.api.ApiDto;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventDetails extends ApiDto {
  private String description;
  private String location;
  private Timestamp start;
  private Timestamp end;

  public EventDetails(String description, String location, Timestamp start, Timestamp end) {
    this.description = description;
    this.location = location;
    this.start = start;
    this.end = end;
  }

  private EventDetails() {}

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Timestamp getStart() {
    return start;
  }

  public void setStart(Timestamp start) {
    this.start = start;
  }

  public Timestamp getEnd() {
    return end;
  }

  public void setEnd(Timestamp end) {
    this.end = end;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "event_details.";
    List<String> fields = new ArrayList<>();
    if (isEmpty(description)) {
      fields.add(fieldPrefix + "description");
    }
    if (isEmpty(location)) {
      fields.add(fieldPrefix + "location");
    }
    if (start == null
        || start.before(Date.from(Instant.now().minusSeconds(ApiDto.secondsLateEventCreation)))) {
      fields.add(fieldPrefix + "start");
    }
    if (end == null) {
      fields.add(fieldPrefix + "end");
    }
    if (start != null && end != null && start.after(end)) {
      fields.add(fieldPrefix + "start/end");
    }
    return fields;
  }

  @Override
  public List<String> validateFields(String fieldPrefix, boolean isNullable) {
    if (!isNullable) {
      return this.validateFields(fieldPrefix);
    }
    String fieldName = fieldPrefix + "event_details.";

    List<String> fields = new ArrayList<>();
    if (description != null && description.trim().isEmpty()) {
      fields.add(fieldName + "description");
    }
    if (location != null && location.trim().isEmpty()) {
      fields.add(fieldName + "location");
    }
    if (start != null && end != null && start.after(end)) {
      fields.add(fieldName + "start/end");
    }

    return fields;
  }
}
