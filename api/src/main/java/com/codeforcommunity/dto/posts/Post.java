package com.codeforcommunity.dto.posts;

/** Represents the data about an post, including post's id, title, body, and userId of post. */
public class Post {

  private int id;
  private int userId;
  private String title;
  private String body;

  private Post() {}

  /**
   * Constructs a post with the given data.
   *
   * @param id id of the post being created
   * @param userId user ID of the post
   * @param title title of the post
   * @param body body of the post
   */
  public Post(int id, int userId, String title, String body) {
    this.id = id;
    this.userId = userId;
    this.title = title;
    this.body = body;
  }

  /**
   * Gets the id of this post.
   *
   * @return id of this post
   */
  public int getId() {
    return id;
  }

  /**
   * Gets the userId of this post.
   *
   * @return id of this post
   */
  public int getUserId() {
    return userId;
  }

  /**
   * Gets the title of this post.
   *
   * @return title of this post
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the body of this post.
   *
   * @return body of this post
   */
  public String getBody() {
    return body;
  }

  @Override
  public String toString() {
    return "post={"
        + "id="
        + id
        + "userId="
        + userId
        + ", title='"
        + title
        + '\''
        + ", body='"
        + body
        + '}';
  }
}
