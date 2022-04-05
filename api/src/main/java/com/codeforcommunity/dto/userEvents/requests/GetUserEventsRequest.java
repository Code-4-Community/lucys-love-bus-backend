package com.codeforcommunity.dto.userEvents.requests;

import java.sql.Timestamp;
import java.util.Optional;

/** A class to get user events. */
public class GetUserEventsRequest {
  private Optional<Timestamp> endDate; // optional
  private Optional<Timestamp> startDate; // optional
  private Optional<Integer> count; // optional

  public GetUserEventsRequest(
      Optional<Timestamp> endDate, Optional<Timestamp> startDate, Optional<Integer> count) {
    this.endDate = endDate;
    this.startDate = startDate;
    this.count = count;
  }

  public GetUserEventsRequest() {
    this.endDate = Optional.empty();
    this.startDate = Optional.empty();
    this.count = Optional.empty();
  }

  /**
   * Gets the end date of the event.
   *
   * @return
   */
  public Optional<Timestamp> getEndDate() {
    return endDate;
  }

  /**
   * Sets the end date of the event.
   *
   * @param endDate the end date, is optionals
   */
  public void setEndDate(Optional<Timestamp> endDate) {
    this.endDate = endDate;
  }

  /**
   * Gets the start date of the event.
   *
   * @return the start date
   */
  public Optional<Timestamp> getStartDate() {
    return startDate;
  }

  /**
   * Sets the start date of the event.
   *
   * @param startDate the start date, is optional
   */
  public void setStartDate(Optional<Timestamp> startDate) {
    this.startDate = startDate;
  }

  /**
   * Gets the count of the event.
   *
   * @return the count
   */
  public Optional<Integer> getCount() {
    return count;
  }

  /**
   * Sets the count of the event.
   *
   * @param count the count, is optional
   */
  public void setCount(Optional<Integer> count) {
    this.count = count;
  }
}
