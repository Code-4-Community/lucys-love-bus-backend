package com.codeforcommunity.dto.announcements;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

public class PostAnnouncementRequest extends ApiDto {

  private String title;
  private String description;

  private PostAnnouncementRequest() {}

  public PostAnnouncementRequest(String title, String description) {
    this.title = title;
    this.description = description;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    List<String> fields = new ArrayList<>();
    if (isEmpty(title)) {
      fields.add(fieldPrefix + "title");
    }
    if (isEmpty(description)) {
      fields.add(fieldPrefix + "description");
    }
    return fields;
  }

  @Override
  public String fieldName() {
    return "post_announcement_request.";
  }
}
