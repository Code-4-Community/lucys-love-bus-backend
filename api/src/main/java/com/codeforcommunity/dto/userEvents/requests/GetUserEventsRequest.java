package com.codeforcommunity.dto.userEvents.requests;

import java.sql.Timestamp;
import java.util.Optional;

public class GetUserEventsRequest {
  private Optional<Timestamp> endDate; //optional
  private Optional<Timestamp> startDate; //optional
  private Optional<Integer> count; //optional

  public GetUserEventsRequest(Optional<Timestamp> endDate, Optional<Timestamp> startDate, Optional<Integer> count) {
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

}
