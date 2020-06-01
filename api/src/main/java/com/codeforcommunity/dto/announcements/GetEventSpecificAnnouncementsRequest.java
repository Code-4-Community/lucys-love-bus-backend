package com.codeforcommunity.dto.announcements;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

public class GetEventSpecificAnnouncementsRequest extends ApiDto {

  private Integer eventId;

  private GetEventSpecificAnnouncementsRequest() {}

  public GetEventSpecificAnnouncementsRequest(Integer eventId) {
    this.eventId = eventId;
  }

  public Integer getEventId() {
    return eventId;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "get_event_specific_announcement_request.";
    List<String> fields = new ArrayList<>();
    if (eventId == null || eventId < 1) {
      fields.add(fieldName + "event_id");
    }
    return fields;
  }
}
