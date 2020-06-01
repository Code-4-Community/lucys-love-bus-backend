package com.codeforcommunity.dto.announcements;

import java.sql.Timestamp;

public class GetAnnouncementsRequest {

  private Timestamp startDate; // optional
  private Timestamp endDate; // optional
  private Integer count; // optional

  private GetAnnouncementsRequest() {}

  public GetAnnouncementsRequest(Timestamp startDate, Timestamp endDate, Integer count) {
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

  public Integer getCount() {
    return count;
  }
}
