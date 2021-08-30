package com.codeforcommunity.api;

import com.codeforcommunity.dto.posts.GetPostsResponse;
import com.codeforcommunity.dto.posts.PostPostRequest;
import com.codeforcommunity.dto.posts.PostPostResponse;

public interface IPostsProcessor {

  /**
   * Gets all posts.
   *
   * @return an Posts response DTO
   */
  GetPostsResponse getPosts();

  /**
   * Creates a new Post.
   *
   * @param request DTO containing the data for the Post
   * @return the created Post
   */
  PostPostResponse postPost(PostPostRequest request);

  /**
   * Deletes an Post.
   *
   * @param postId the ID of the Post
   */
  void deletePost(int postId);
}
