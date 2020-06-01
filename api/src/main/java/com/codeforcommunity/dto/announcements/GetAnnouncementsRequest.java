package com.codeforcommunity.dto.announcements;

import com.codeforcommunity.api.ApiDto;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GetAnnouncementsRequest extends ApiDto {

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

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "get_announcements_request.";
    List<String> fields = new ArrayList<>();
    if (count == null || count < 1) {
      fields.add(fieldName + "count");
    }
    if (startDate == null) {
      fields.add(fieldName + "start");
    }
    if (endDate == null) {
      fields.add(fieldName + "end");
    }
    if (endDate.before(startDate) || endDate.after(new Date())) {
      fields.add(fieldName + "start/end");
    }

    return fields;
  }
}
