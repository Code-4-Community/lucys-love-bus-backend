package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IAnnouncementEventsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcement_event.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcement_event.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcement_event.PostAnnouncementsRequest;
import com.codeforcommunity.dto.announcement_event.PostAnnouncementsResponse;
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

  private final IAnnouncementEventsProcessor announcementEventsProcessor;
  private static final long MILLIS_IN_WEEK = 1000 * 60 * 60 * 24 * 7;
  private static final int DEFAULT_COUNT = 50;

  public AnnouncementsRouter(
      IAnnouncementEventsProcessor announcementEventsProcessor) {
    this.announcementEventsProcessor = announcementEventsProcessor;
  }

  @Override
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

    registerGetAnnouncements(router);
    registerPostAnnouncement(router);

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

  private void handleGetAnnouncements(RoutingContext ctx) {
    try {
      Optional<Timestamp> start = RestFunctions.getNullableQueryParam(ctx, "start",
          RestFunctions.getDateParamMapper());
      Optional<Timestamp> end = RestFunctions.getNullableQueryParam(ctx, "end",
          RestFunctions.getDateParamMapper());
      Optional<Integer> count = RestFunctions.getNullableQueryParam(ctx, "count",
          RestFunctions.getCountParamMapper());

      Timestamp endParam = end.orElseGet(() -> new Timestamp(System.currentTimeMillis()));
      Timestamp startParam = start.orElseGet(() ->
          new Timestamp(endParam.getTime() - 3 * MILLIS_IN_WEEK));
      int countParam = count.orElseGet(() -> DEFAULT_COUNT);

      GetAnnouncementsRequest request = new GetAnnouncementsRequest(startParam, endParam,
          countParam);
      GetAnnouncementsResponse response = announcementEventsProcessor.getAnnouncements(request);
      end(ctx.response(), 200, JsonObject.mapFrom(response).toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handlePostAnnouncement(RoutingContext ctx) {
    try {
      PostAnnouncementsRequest requestData = RestFunctions.getJsonBodyAsClass(ctx, PostAnnouncementsRequest.class);
      JWTData userData = ctx.get("jwt_data");

      PostAnnouncementsResponse response = announcementEventsProcessor.postAnnouncements(requestData, userData);
      end(ctx.response(), 200, JsonObject.mapFrom(response).toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
