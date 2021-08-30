package com.codeforcommunity.dto.posts;

/**
 * Represents an object containing the information for a response to a post post, which is just the
 * post.
 */
public class PostPostResponse {

  private Post post;

  private PostPostResponse() {}

  /**
   * Constructs a PostPostResponse object containing the given post.
   *
   * @param post the post to be contained in this response object
   */
  public PostPostResponse(Post post) {
    this.post = post;
  }

  /**
   * Gets the post stored in this response object.
   *
   * @return the post stored in this response object
   */
  public Post getPost() {
    return post;
  }
}
