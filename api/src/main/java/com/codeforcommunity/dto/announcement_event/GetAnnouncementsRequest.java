package com.codeforcommunity.dto.announcement_event;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

public class GetAnnouncementsRequest {

  private Optional<Timestamp> startDate; // optional
  private Optional<Timestamp> endDate; // optional
  private Optional<Integer> count; // optional
  private UUID id; // required

  private GetAnnouncementsRequest() {}

  public GetAnnouncementsRequest(Optional<Timestamp> endDate,
      Optional<Timestamp> startDate, Optional<Integer> count, UUID id) {
    this.endDate = endDate;
    this.startDate = startDate;
    this.count = count;
    this.id = id;
  }

  public Optional<Timestamp> getEndDate() {
    return endDate;
  }

  public Optional<Timestamp> getStartDate() {
    return startDate;
  }

  public Optional<Integer> getCount() {
    return count;
  }

  public UUID getId() {
    return id;
  }
}
