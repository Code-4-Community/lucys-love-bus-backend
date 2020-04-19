package com.codeforcommunity.dto.announcements;

import java.util.List;

public class GetEventSpecificAnnouncementsResponse {

  private int totalCount;
  private List<Announcement> announcements;

  private GetEventSpecificAnnouncementsResponse() {}

  public GetEventSpecificAnnouncementsResponse(int totalCount,
      List<Announcement> announcements) {
    this.totalCount = totalCount;
    this.announcements = announcements;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public List<Announcement> getAnnouncements() {
    return announcements;
  }
}
