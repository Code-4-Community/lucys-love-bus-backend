package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IPublicAnnouncementsProcessor;
import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
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

public class PublicAnnouncementsRouter implements IRouter {

  private final IPublicAnnouncementsProcessor processor;
  private static final long MILLIS_IN_WEEK = 1000 * 60 * 60 * 24 * 7;
  private static final int DEFAULT_COUNT = 50;

  public PublicAnnouncementsRouter(IPublicAnnouncementsProcessor processor) {
    this.processor = processor;
  }

  @Override
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);
    registerGetAnnouncements(router);
    return router;
  }

  private void registerGetAnnouncements(Router router) {
    Route getAnnouncementsRoute = router.get("/");
    getAnnouncementsRoute.handler(this::handleGetAnnouncements);
  }

  private void handleGetAnnouncements(RoutingContext ctx) {
    Optional<Timestamp> start =
        RestFunctions.getOptionalQueryParam(ctx, "start", Timestamp::valueOf);
    Optional<Timestamp> end = RestFunctions.getOptionalQueryParam(ctx, "end", Timestamp::valueOf);
    Optional<Integer> count = RestFunctions.getOptionalQueryParam(ctx, "count", Integer::parseInt);

    Timestamp endParam = end.orElseGet(() -> new Timestamp(System.currentTimeMillis()));
    Timestamp startParam =
        start.orElseGet(() -> new Timestamp(endParam.getTime() - 3 * MILLIS_IN_WEEK));
    int countParam = count.orElse(DEFAULT_COUNT);

    GetAnnouncementsRequest request = new GetAnnouncementsRequest(startParam, endParam, countParam);
    GetAnnouncementsResponse response = processor.getAnnouncements(request);
    end(ctx.response(), 200, JsonObject.mapFrom(response).toString());
  }
}
