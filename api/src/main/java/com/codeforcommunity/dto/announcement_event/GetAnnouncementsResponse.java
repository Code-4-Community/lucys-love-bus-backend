package com.codeforcommunity.dto.announcement_event;

import java.util.List;

public class GetAnnouncementsResponse {

  private int totalCount;
  private List<Announcement> announcements;

  public GetAnnouncementsResponse(int totalCount,
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
