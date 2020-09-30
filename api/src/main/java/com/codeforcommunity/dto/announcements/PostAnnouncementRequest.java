package com.codeforcommunity.dto.announcements;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an object containing the information for an announcement post request, including the
 * title and description of the announcement.
 */
public class PostAnnouncementRequest extends ApiDto {

  private String title;
  private String description;

  private PostAnnouncementRequest() {}

  /**
   * Constructs a PostAnnouncementRequest object with the given title and description.
   *
   * @param title title of the post request object to be created
   * @param description description of the post request object to be created
   */
  public PostAnnouncementRequest(String title, String description) {
    this.title = title;
    this.description = description;
  }

  /**
   * Gets the title of the announcement in this post request object.
   *
   * @return title of the announcement in this post request object
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the description of the announcement in this post request object.
   *
   * @return description of the announcement in this post request object
   */
  public String getDescription() {
    return description;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "post_announcement_request.";
    List<String> fields = new ArrayList<>();
    if (isEmpty(title)) {
      fields.add(fieldName + "title");
    }
    if (description == null) {
      fields.add(fieldName + "description");
    }
    return fields;
  }
}
