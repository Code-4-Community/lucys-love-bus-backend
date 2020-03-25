package com.codeforcommunity.dto.announcement_event;

import java.sql.Timestamp;
import java.util.UUID;

public class GetAnnouncementsRequest {

  private Timestamp startDate; // optional
  private Timestamp endDate; // optional
  private int count; // optional

  private GetAnnouncementsRequest() {}

  public GetAnnouncementsRequest(Timestamp startDate, Timestamp endDate, int count) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.count = count;
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
}
