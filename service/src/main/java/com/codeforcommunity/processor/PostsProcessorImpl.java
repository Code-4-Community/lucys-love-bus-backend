package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.POSTS;

import com.codeforcommunity.api.IPostsProcessor;
import com.codeforcommunity.dto.posts.GetPostsResponse;
import com.codeforcommunity.dto.posts.Post;
import com.codeforcommunity.dto.posts.PostPostRequest;
import com.codeforcommunity.dto.posts.PostPostResponse;
import com.codeforcommunity.requester.Emailer;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.Posts;
import org.jooq.generated.tables.records.PostsRecord;

public class PostsProcessorImpl implements IPostsProcessor {

  private final DSLContext db;
  private final Emailer emailer;

  public PostsProcessorImpl(DSLContext db, Emailer emailer) {
    this.db = db;
    this.emailer = emailer;
  }

  @Override
  public GetPostsResponse getPosts() {

    List<Posts> posts = db.selectFrom(POSTS).orderBy(POSTS.ID.desc()).fetchInto(Posts.class);

    return new GetPostsResponse(
        posts.stream().map(this::convertPostObject).collect(Collectors.toList()));
  }

  @Override
  public PostPostResponse postPost(PostPostRequest request) {
    PostsRecord newPostsRecord = postRequestToRecord(request);
    newPostsRecord.store();
    return postPojoToResponse(
        db.selectFrom(POSTS)
            .where(POSTS.ID.eq(newPostsRecord.getId()))
            .fetchInto(Posts.class)
            .get(0));
  }

  @Override
  public void deletePost(int postId) {
    db.delete(POSTS).where(POSTS.ID.eq(postId)).execute();
  }

  /**
   * Converts a jOOQ POJO post into the post class defined in the API package.
   *
   * @param post the jOOQ POJO post
   * @return an object of type Post
   */
  private Post convertPostObject(Posts post) {
    return new Post(post.getId(), post.getUserId(), post.getTitle(), post.getBody());
  }

  /**
   * Converts a jOOQ POJO post into the PostPostResponse class.
   *
   * @param posts jOOQ POJO post
   * @return response in the form of PostPostResponse
   */
  private PostPostResponse postPojoToResponse(Posts posts) {
    return new PostPostResponse(convertPostObject(posts));
  }

  private PostsRecord postRequestToRecord(PostPostRequest request) {
    PostsRecord newRecord = db.newRecord(POSTS);
    newRecord.setBody(request.getBody());
    newRecord.setUserId(request.getUserId());
    newRecord.setTitle(request.getTitle());
    return newRecord;
  }
}
