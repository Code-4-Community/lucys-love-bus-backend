package com.codeforcommunity.dto.posts;

import com.codeforcommunity.dto.ApiDto;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an object containing the information for an post post request, including the title,
 * description, and possibly an image src of the post.
 */
public class PostPostRequest extends ApiDto {

  private int userId;
  private String title;
  private String body;

  private PostPostRequest() {}

  /**
   * Constructs a PostpostRequest object with the given userId, title, and body.
   *
   * @param userId userId of the post request object to be created
   * @param title title of the post request object to be created
   * @param body description of the post request object to be created
   */
  public PostPostRequest(int userId, String title, String body) {
    this.userId = userId;
    this.title = title;
    this.body = body;
  }

  /**
   * Gets the userId of the post in this post request object.
   *
   * @return userId of the post in this post request object
   */
  public int getUserId() {
    return userId;
  }

  /**
   * Gets the title of the post in this post request object.
   *
   * @return title of the post in this post request object
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the body of the post in this post request object.
   *
   * @return body of the post in this post request object
   */
  public String getBody() {
    return body;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "post_post_request.";
    List<String> fields = new ArrayList<>();
    if (isEmpty(title)) {
      fields.add(fieldName + "title");
    }
    if (isEmpty(body)) {
      fields.add(fieldName + "body");
    }
    return fields;
  }
}
