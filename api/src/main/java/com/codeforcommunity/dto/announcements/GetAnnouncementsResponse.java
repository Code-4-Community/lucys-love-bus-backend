package com.codeforcommunity.dto.announcements;

import java.util.List;

/**
 * Represents an object containing the response for a get announcements request, including the
 * number of announcements returned and a list of the actual announcements.
 */
public class GetAnnouncementsResponse {

  private int totalCount;
  private List<Announcement> announcements;

  /**
   * Constructs a GetAnnouncementsResponse object containing the given data as the response.
   *
   * @param totalCount the total number of announcements contained in this response object
   * @param announcements the list of announcements contained in this response object
   */
  public GetAnnouncementsResponse(int totalCount, List<Announcement> announcements) {
    this.totalCount = totalCount;
    this.announcements = announcements;
  }

  /**
   * Gets the total number of announcements in this response object.
   *
   * @return total number of announcements in this response object
   */
  public int getTotalCount() {
    return totalCount;
  }

  /**
   * Gets the list of announcements contained in this response object.
   *
   * @return list of announcements in this response object
   */
  public List<Announcement> getAnnouncements() {
    return announcements;
  }
}
