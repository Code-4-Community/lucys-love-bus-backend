package com.codeforcommunity.dto.announcements;

import java.sql.Timestamp;

/**
 * Represents the information of a GetAnnouncements request, including start and end date to get
 * announcements between, and number of announcements to get.
 */
public class GetAnnouncementsRequest {

  private Timestamp startDate; // optional
  private Timestamp endDate; // optional
  private Integer count; // optional

  private GetAnnouncementsRequest() {}

  /**
   * Constructs a GetAnnouncementsRequest with the given start and end dates to get announcements
   * between, and the number of announcements to get.
   *
   * @param startDate start date of announcements to get
   * @param endDate end date of announcements to get
   * @param count number of announcements to get
   */
  public GetAnnouncementsRequest(Timestamp startDate, Timestamp endDate, Integer count) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.count = count;
  }

  /**
   * Gets the start date of announcements request stored in this object.
   *
   * @return start date of announcements request
   */
  public Timestamp getStartDate() {
    return startDate;
  }

  /**
   * Gets the end date of announcements request stored in this object.
   *
   * @return end date of announcements request
   */
  public Timestamp getEndDate() {
    return endDate;
  }

  /**
   * Gets the number of announcements requested stored in this object.
   *
   * @return number of announcements requested
   */
  public Integer getCount() {
    return count;
  }
}
