package com.codeforcommunity.rest.subrouter;

import static com.codeforcommunity.rest.ApiRouter.end;

import com.codeforcommunity.api.IPostsProcessor;
import com.codeforcommunity.dto.posts.GetPostsResponse;
import com.codeforcommunity.dto.posts.PostPostRequest;
import com.codeforcommunity.dto.posts.PostPostResponse;
import com.codeforcommunity.rest.IRouter;
import com.codeforcommunity.rest.RestFunctions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class PostsRouter implements IRouter {

  private final IPostsProcessor processor;
  private static final long MILLIS_IN_WEEK = 1000 * 60 * 60 * 24 * 7;
  private static final int DEFAULT_COUNT = 50;

  public PostsRouter(IPostsProcessor processor) {
    this.processor = processor;
  }

  @Override
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

    registerGetPosts(router);
    registerPostPosts(router);
    registerDeletePost(router);

    return router;
  }

  private void registerGetPosts(Router router) {
    Route getPostRoute = router.get("/");
    getPostRoute.handler(this::handleGetPosts);
  }

  private void registerPostPosts(Router router) {
    Route postPostRoute = router.post("/");
    postPostRoute.handler(this::handlePostPost);
  }

  private void registerDeletePost(Router router) {
    Route deletePostRoute = router.delete("/:post_id");
    deletePostRoute.handler(this::handleDeletePost);
  }

  private void handleGetPosts(RoutingContext ctx) {
    GetPostsResponse response = processor.getPosts();
    end(ctx.response(), 200, JsonObject.mapFrom(response).toString());
  }

  private void handlePostPost(RoutingContext ctx) {
    PostPostRequest requestData = RestFunctions.getJsonBodyAsClass(ctx, PostPostRequest.class);
    PostPostResponse response = processor.postPost(requestData);
    end(ctx.response(), 200, JsonObject.mapFrom(response).toString());
  }

  private void handleDeletePost(RoutingContext ctx) {
    int postId = RestFunctions.getRequestParameterAsInt(ctx.request(), "post_id");
    processor.deletePost(postId);
    end(ctx.response(), 200);
  }
}
