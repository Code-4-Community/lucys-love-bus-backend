package com.codeforcommunity.dto.announcements;

import com.codeforcommunity.api.ApiDto;
import com.codeforcommunity.exceptions.MalformedParameterException;

public class PostAnnouncementRequest implements ApiDto {

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
  public void validate() {
    if (title == null) {
      throw new MalformedParameterException("Title");
    }
    if (description == null) {
      throw new MalformedParameterException("Description");
    }
  }
}
