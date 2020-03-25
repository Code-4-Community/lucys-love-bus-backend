package com.codeforcommunity.dto.announcement_event;

import java.sql.Timestamp;
import java.util.UUID;

public class GetAnnouncementsRequest {

  private Timestamp startDate; // optional
  private Timestamp endDate; // optional
  private int count; // optional
  private UUID id; // required

  private GetAnnouncementsRequest() {}

  public GetAnnouncementsRequest(Timestamp startDate, Timestamp endDate, int count, UUID id) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.count = count;
    this.id = id;
  }

  public Timestamp getStartDate() {
    return startDate;
  }

  public Timestamp getEndDate() {
    return endDate;
  }

  public int getCount() {
    return count;
  }

  public UUID getId() {
    return id;
  }
}
