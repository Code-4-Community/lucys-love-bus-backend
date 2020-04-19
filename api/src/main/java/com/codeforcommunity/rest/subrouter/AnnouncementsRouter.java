package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IAnnouncementsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcements.GetEventSpecificAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementResponse;
import com.codeforcommunity.exceptions.RequestBodyMappingException;
import com.codeforcommunity.rest.IRouter;
import com.codeforcommunity.rest.RestFunctions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.sql.Timestamp;
import java.util.Optional;

import static com.codeforcommunity.rest.ApiRouter.end;

public class AnnouncementsRouter implements IRouter {

  private final IAnnouncementsProcessor processor;
  private static final long MILLIS_IN_WEEK = 1000 * 60 * 60 * 24 * 7;
  private static final int DEFAULT_COUNT = 50;

  public AnnouncementsRouter(IAnnouncementsProcessor processor) {
    this.processor = processor;
  }

  @Override
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

    registerGetAnnouncements(router);
    registerPostAnnouncement(router);
    registerGetEventSpecificAnnouncements(router);
    registerPostEventSpecificAnnouncement(router);

    return router;
  }

  private void registerGetAnnouncements(Router router) {
    Route getAnnouncementsRoute = router.get("/");
    getAnnouncementsRoute.handler(this::handleGetAnnouncements);
  }

  private void registerPostAnnouncement(Router router) {
    Route postAnnouncementRoute = router.post("/");
    postAnnouncementRoute.handler(this::handlePostAnnouncement);
  }

  private void registerGetEventSpecificAnnouncements(Router router) {
    Route getEventSpecificAnnouncementsRoute = router.get("/:event_id");
    getEventSpecificAnnouncementsRoute.handler(this::handleGetEventSpecificAnnouncements);
  }

  private void registerPostEventSpecificAnnouncement(Router router) {
    Route postEventSpecificAnnouncementRoute = router.post("/:event_id");
    postEventSpecificAnnouncementRoute.handler(this::handlePostEventSpecificAnnouncement);
  }

  private void handleGetAnnouncements(RoutingContext ctx) {
    Optional<Timestamp> start = RestFunctions.getOptionalQueryParam(ctx, "start",
        Timestamp::valueOf);
    Optional<Timestamp> end = RestFunctions.getOptionalQueryParam(ctx, "end",
        Timestamp::valueOf);
    Optional<Integer> count = RestFunctions.getOptionalQueryParam(ctx, "count",
        Integer::parseInt);

    Timestamp endParam = end.orElseGet(() -> new Timestamp(System.currentTimeMillis()));
    Timestamp startParam = start.orElseGet(() ->
        new Timestamp(endParam.getTime() - 3 * MILLIS_IN_WEEK));
    int countParam = count.orElse(DEFAULT_COUNT);

    GetAnnouncementsRequest request = new GetAnnouncementsRequest(startParam, endParam,
        countParam);
    GetAnnouncementsResponse response = processor.getAnnouncements(request);
    end(ctx.response(), 200, JsonObject.mapFrom(response).toString());
  }

  private void handlePostAnnouncement(RoutingContext ctx) {
    PostAnnouncementRequest requestData = RestFunctions.getJsonBodyAsClass(ctx,
        PostAnnouncementRequest.class);
    JWTData userData = ctx.get("jwt_data");

    PostAnnouncementResponse response = processor.postAnnouncement(requestData, userData);
    end(ctx.response(), 200, JsonObject.mapFrom(response).toString());
  }

  private void handleGetEventSpecificAnnouncements(RoutingContext ctx) {
    int eventId = RestFunctions.getRequestParameterAsInt(ctx.request(), "event_id");
    GetEventSpecificAnnouncementsRequest requestData = new GetEventSpecificAnnouncementsRequest(
        eventId);

    GetAnnouncementsResponse response = processor.getEventSpecificAnnouncements(
        requestData);
    end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
  }

  private void handlePostEventSpecificAnnouncement(RoutingContext ctx) {
    PostAnnouncementRequest requestData = RestFunctions.getJsonBodyAsClass(ctx,
        PostAnnouncementRequest.class);
    JWTData userData = ctx.get("jwt_data");

    PostAnnouncementResponse response = processor.postEventSpecificAnnouncement(
        requestData, userData, RestFunctions.getRequestParameterAsInt(ctx.request(),
            "event_id"));
    end(ctx.response(), 200, JsonObject.mapFrom(response).toString());
  }
}
