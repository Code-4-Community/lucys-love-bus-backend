package com.codeforcommunity.rest;

import com.codeforcommunity.api.IAnnouncementsProcessor;
import com.codeforcommunity.api.IAuthProcessor;
import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.api.IPostsProcessor;
import com.codeforcommunity.api.IProtectedUserProcessor;
import com.codeforcommunity.api.IPublicAnnouncementsProcessor;
import com.codeforcommunity.api.IPublicEventsProcessor;
import com.codeforcommunity.api.IRequestsProcessor;
import com.codeforcommunity.auth.JWTAuthorizer;
import com.codeforcommunity.rest.subrouter.AnnouncementsRouter;
import com.codeforcommunity.rest.subrouter.AuthRouter;
import com.codeforcommunity.rest.subrouter.CheckoutRouter;
import com.codeforcommunity.rest.subrouter.CommonRouter;
import com.codeforcommunity.rest.subrouter.EventsRouter;
import com.codeforcommunity.rest.subrouter.PfRequestRouter;
import com.codeforcommunity.rest.subrouter.PostsRouter;
import com.codeforcommunity.rest.subrouter.ProtectedUserRouter;
import com.codeforcommunity.rest.subrouter.PublicAnnouncementsRouter;
import com.codeforcommunity.rest.subrouter.PublicEventsRouter;
import com.codeforcommunity.rest.subrouter.WebhooksRouter;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

public class ApiRouter implements IRouter {
  private final CommonRouter commonRouter;
  private final AuthRouter authRouter;
  private final ProtectedUserRouter protectedUserRouter;
  private final PfRequestRouter requestRouter;
  private final EventsRouter eventsRouter;
  private final PublicEventsRouter publicEventsRouter;
  private final AnnouncementsRouter announcementsRouter;
  private final PublicAnnouncementsRouter publicAnnouncementsRouter;
  private final CheckoutRouter checkoutRouter;
  private final WebhooksRouter webhooksRouter;
  private final PostsRouter postsRouter;

  public ApiRouter(
      IAuthProcessor authProcessor,
      IProtectedUserProcessor protectedUserProcessor,
      IRequestsProcessor requestsProcessor,
      IEventsProcessor eventsProcessor,
      IPublicEventsProcessor publicEventsProcessor,
      IAnnouncementsProcessor announcementEventsProcessor,
      IPublicAnnouncementsProcessor publicAnnouncementsProcessor,
      ICheckoutProcessor checkoutProcessor,
      IPostsProcessor postsProcessor,
      JWTAuthorizer jwtAuthorizer) {

    this.commonRouter = new CommonRouter(jwtAuthorizer);
    this.authRouter = new AuthRouter(authProcessor);
    this.protectedUserRouter = new ProtectedUserRouter(protectedUserProcessor);
    this.requestRouter = new PfRequestRouter(requestsProcessor);
    this.eventsRouter = new EventsRouter(eventsProcessor);
    this.publicEventsRouter = new PublicEventsRouter(publicEventsProcessor);
    this.announcementsRouter = new AnnouncementsRouter(announcementEventsProcessor);
    this.publicAnnouncementsRouter = new PublicAnnouncementsRouter(publicAnnouncementsProcessor);
    this.checkoutRouter = new CheckoutRouter(checkoutProcessor);
    this.webhooksRouter = new WebhooksRouter(checkoutProcessor);
    this.postsRouter = new PostsRouter(postsProcessor);
  }

  /** Initialize a router and register all route handlers on it. */
  public Router initializeRouter(Vertx vertx) {
    Router router = commonRouter.initializeRouter(vertx);

    router.mountSubRouter("/user", authRouter.initializeRouter(vertx));
    router.mountSubRouter("/webhooks", webhooksRouter.initializeRouter(vertx));
    router.mountSubRouter("/events", publicEventsRouter.initializeRouter(vertx));
    router.mountSubRouter("/announcements", publicAnnouncementsRouter.initializeRouter(vertx));
    router.mountSubRouter("/posts", postsRouter.initializeRouter(vertx));
    router.mountSubRouter("/protected", defineProtectedRoutes(vertx));

    return router;
  }

  /**
   * Mounts all routes that require a user to be logged in. All routes defined here require a user
   * to have a valid JWT access token in their header.
   */
  private Router defineProtectedRoutes(Vertx vertx) {
    Router router = Router.router(vertx);

    router.mountSubRouter("/user", protectedUserRouter.initializeRouter(vertx));
    router.mountSubRouter("/requests", requestRouter.initializeRouter(vertx));
    router.mountSubRouter("/events", eventsRouter.initializeRouter(vertx));
    router.mountSubRouter("/announcements", announcementsRouter.initializeRouter(vertx));
    router.mountSubRouter("/checkout", checkoutRouter.initializeRouter(vertx));

    return router;
  }

  public static void end(HttpServerResponse response, int statusCode) {
    end(response, statusCode, null);
  }

  public static void end(HttpServerResponse response, int statusCode, String jsonBody) {
    end(response, statusCode, jsonBody, "application/json");
  }

  public static void end(
      HttpServerResponse response, int statusCode, String jsonBody, String contentType) {
    response
        .setStatusCode(statusCode)
        .putHeader("Content-Type", contentType)
        .putHeader("Access-Control-Allow-Origin", "*")
        .putHeader("Access-Control-Allow-Methods", "DELETE, POST, GET, OPTIONS")
        .putHeader(
            "Access-Control-Allow-Headers",
            "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
    if (jsonBody == null || jsonBody.equals("")) {
      response.end();
    } else {
      response.end(jsonBody);
    }
  }
}
