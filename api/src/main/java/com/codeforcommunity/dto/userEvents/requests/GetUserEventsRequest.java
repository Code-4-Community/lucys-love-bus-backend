package com.codeforcommunity.dto.userEvents.requests;

import com.codeforcommunity.api.ApiDto;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GetUserEventsRequest extends ApiDto {
  private Optional<Timestamp> endDate; // optional
  private Optional<Timestamp> startDate; // optional
  private Optional<Integer> count; // optional

  public GetUserEventsRequest(
      Optional<Timestamp> endDate, Optional<Timestamp> startDate, Optional<Integer> count) {
    this.endDate = endDate;
    this.startDate = startDate;
    this.count = count;
  }

  public Optional<Timestamp> getEndDate() {
    return endDate;
  }

  public void setEndDate(Optional<Timestamp> endDate) {
    this.endDate = endDate;
  }

  public Optional<Timestamp> getStartDate() {
    return startDate;
  }

  public void setStartDate(Optional<Timestamp> startDate) {
    this.startDate = startDate;
  }

  public Optional<Integer> getCount() {
    return count;
  }

  public void setCount(Optional<Integer> count) {
    this.count = count;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "get_user_events_request.";
    List<String> fields = new ArrayList<>();
    if (count.isPresent() && count.get() < 0) {
      fields.add(fieldName + "count");
    }
    if (startDate.isPresent() && endDate.isPresent() && startDate.get().after(endDate.get())) {
      fields.add(fieldName + "start/end");
    }
    return fields;
  }
}
