package com.codeforcommunity.rest.subrouter;

import static com.codeforcommunity.rest.ApiRouter.end;

import com.codeforcommunity.auth.JWTAuthorizer;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.exceptions.AccessTokenInvalidException;
import com.codeforcommunity.dto.announcement_event.GetAnnouncementsRequest;
import com.codeforcommunity.exceptions.AuthException;
import com.codeforcommunity.exceptions.MissingHeaderException;
import com.codeforcommunity.rest.IRouter;
import com.codeforcommunity.rest.RestFunctions;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Optional;

public class CommonRouter implements IRouter {
  private final JWTAuthorizer jwtAuthorizer;
  private final FailureHandler failureHandler = new FailureHandler();

  public CommonRouter(JWTAuthorizer jwtAuthorizer) {
    this.jwtAuthorizer = jwtAuthorizer;
  }

  @Override
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create(false)); //Add body handling

    router.route().failureHandler(failureHandler::handleFailure); //Add failure handling

    router.routeWithRegex(".*/protected/.*").handler(this::handleAuthorizeUser); //Add auth checking

    registerGetAnnouncements(router);
    registerPostAnnouncement(router);

    return router;
  }

  /**
   * A handler to be called as the first handler for any request for a protected resource. If given user is
   * authorized this router will call the next router in which the desired response is handled.
   *
   * If user fails authorization this handler will end the handler with an unauthorized response to the user.
   *
   * @param ctx routing context to handle.
   */
  private void handleAuthorizeUser(RoutingContext ctx) {
    String accessToken = RestFunctions.getRequestHeader(ctx.request(), "X-Access-Token");
    Optional<JWTData> jwtData = jwtAuthorizer.checkTokenAndGetData(accessToken);
    if (jwtData.isPresent()) {
      ctx.put("jwt_data", jwtData.get());
      ctx.next();
    } else {
      throw new AccessTokenInvalidException();
    }
  }

  private void registerGetAnnouncements(Router router) {
    Route getAnnouncementsRoute = router.get("/protected/announcements");
    getAnnouncementsRoute.handler(this::handleGetAnnouncements);
  }

  private void registerPostAnnouncement(Router router) {
    Route postAnnouncementRoute = router.post("/protected/announcements");
    postAnnouncementRoute.handler(this::handlePostAnnouncement);
  }

  private void handleGetAllEvents(RoutingContext ctx) {
    GetAllEventsRequest request = new GetAllEventsRequest() {{
      setEndDate(RestFunctions.getNullableQueryParam(ctx, "end",
          RestFunctions.getDateParamMapper()));
      setStartDate(RestFunctions.getNullableQueryParam(ctx, "start",
          RestFunctions.getDateParamMapper()));
      setCount(RestFunctions.getNullableQueryParam(ctx, "count",
          RestFunctions.getCountParamMapper()));
    }};

    GetEventsResponse response = userEventsProcessor.getAllEvents(request);
    end(ctx.response(), 200, JsonObject.mapFrom(response).toString());
  }

  private void handleGetAnnouncements(RoutingContext ctx) {

    GetAnnouncementsRequest request = new GetAnnouncementsRequest(
        RestFunctions.getNullableQueryParam(ctx, "end",
            RestFunctions.getDateParamMapper()),
        RestFunctions.getNullableQueryParam(ctx, "end",
            RestFunctions.getDateParamMapper()),
        RestFunctions.getNullableQueryParam(ctx, "end",
            RestFunctions.getDateParamMapper()),
        RestFunctions.getNullableQueryParam(ctx, "end",
            RestFunctions.getDateParamMapper())
        );

    //todo implement
  }

  private void handlePostAnnouncement(RoutingContext ctx) {
    //todo implement
  }
}
