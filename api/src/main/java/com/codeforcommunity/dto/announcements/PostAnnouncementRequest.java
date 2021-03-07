package com.codeforcommunity.dto.announcements;

import com.codeforcommunity.api.ApiDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents an object containing the information for an announcement post request, including the
 * title, description, and possibly an image src of the announcement.
 */
public class PostAnnouncementRequest extends ApiDto {

  private String title;
  private String description;
  private String imageSrc;

  private PostAnnouncementRequest() {}

  /**
   * Constructs a PostAnnouncementRequest object with the given title and description. This will
   * create an announcement with no image.
   *
   * @param title title of the post request object to be created
   * @param description description of the post request object to be created
   */
  public PostAnnouncementRequest(String title, String description) {
    this(title, description, null);
  }

  /**
   * Constructs a PostAnnouncementRequest object with the given title, description, and image src.
   *
   * @param title title of the post request object to be created
   * @param description description of the post request object to be created
   * @param imageSrc image src url of the post request object to be created
   */
  public PostAnnouncementRequest(String title, String description, String imageSrc) {
    this.title = title;
    this.description = description;
    this.imageSrc = imageSrc;
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

  /**
   * Gets the image src of the announcement in this post request object, if it exists.
   *
   * @return an optional image src of the announcement in this post request object.
   */
  public Optional<String> getImageSrc() {
    return isEmpty(imageSrc) ? Optional.empty() : Optional.of(imageSrc);
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "post_announcement_request.";
    List<String> fields = new ArrayList<>();
    if (isEmpty(title)) {
      fields.add(fieldName + "title");
    }
    if (isEmpty(description)) {
      fields.add(fieldName + "description");
    }
    if (isEmpty(imageSrc)) {
      fields.add(fieldName + "imageSrc");
    }
    return fields;
  }
}
