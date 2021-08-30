package com.codeforcommunity.dto.posts;

import java.util.List;

/**
 * Represents an object containing the response for a get posts request, including the number of
 * posts returned and a list of the actual posts.
 */
public class GetPostsResponse {

  private int totalCount;
  private List<Post> posts;

  /**
   * Constructs a GetPostsResponse object containing the given data as the response.
   *
   * @param posts the list of posts contained in this response object
   */
  public GetPostsResponse(List<Post> posts) {
    this.posts = posts;
  }

  /**
   * Gets the list of posts contained in this response object.
   *
   * @return list of posts in this response object
   */
  public List<Post> getPosts() {
    return posts;
  }
}
