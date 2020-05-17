package com.codeforcommunity.dto.announcements;

import com.codeforcommunity.exceptions.MalformedParameterException;
import java.sql.Timestamp;
import java.util.Date;

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

  /**
   * Validates the request.
   *
   * @throws MalformedParameterException if any of the request parameters are invalid
   */
  public void validate() {
    if (count < 1) {
      throw new MalformedParameterException("count");
    }
    if (endDate.before(startDate) || endDate.after(new Date())) {
      throw new MalformedParameterException("end");
    }
  }
}
