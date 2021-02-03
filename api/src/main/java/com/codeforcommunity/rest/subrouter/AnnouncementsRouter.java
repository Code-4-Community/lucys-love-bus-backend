package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IAnnouncementsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcements.GetEventSpecificAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementResponse;
import com.codeforcommunity.rest.IRouter;
import com.codeforcommunity.rest.RestFunctions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import static com.codeforcommunity.rest.ApiRouter.end;

public class AnnouncementsRouter implements IRouter {

  private final IAnnouncementsProcessor processor;

  public AnnouncementsRouter(IAnnouncementsProcessor processor) {
    this.processor = processor;
  }

  @Override
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

    registerPostAnnouncement(router);
    registerGetEventSpecificAnnouncements(router);
    registerPostEventSpecificAnnouncement(router);
    registerDeleteAnnouncement(router);

    return router;
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

  private void registerDeleteAnnouncement(Router router) {
    Route deleteAnnouncementRoute = router.delete("/:announcement_id");
    deleteAnnouncementRoute.handler(this::handleDeleteAnnouncement);
  }

  private void handlePostAnnouncement(RoutingContext ctx) {
    PostAnnouncementRequest requestData =
        RestFunctions.getJsonBodyAsClass(ctx, PostAnnouncementRequest.class);
    JWTData userData = ctx.get("jwt_data");

    PostAnnouncementResponse response = processor.postAnnouncement(requestData, userData);
    end(ctx.response(), 200, JsonObject.mapFrom(response).toString());
  }

  private void handleGetEventSpecificAnnouncements(RoutingContext ctx) {
    int eventId = RestFunctions.getRequestParameterAsInt(ctx.request(), "event_id");
    GetEventSpecificAnnouncementsRequest requestData =
        new GetEventSpecificAnnouncementsRequest(eventId);

    GetAnnouncementsResponse response = processor.getEventSpecificAnnouncements(requestData);
    end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
  }

  private void handlePostEventSpecificAnnouncement(RoutingContext ctx) {
    PostAnnouncementRequest requestData =
        RestFunctions.getJsonBodyAsClass(ctx, PostAnnouncementRequest.class);
    JWTData userData = ctx.get("jwt_data");

    PostAnnouncementResponse response =
        processor.postEventSpecificAnnouncement(
            requestData,
            userData,
            RestFunctions.getRequestParameterAsInt(ctx.request(), "event_id"));
    end(ctx.response(), 200, JsonObject.mapFrom(response).toString());
  }

  private void handleDeleteAnnouncement(RoutingContext ctx) {
    int announcementId = RestFunctions.getRequestParameterAsInt(ctx.request(), "announcement_id");
    JWTData userData = ctx.get("jwt_data");

    processor.deleteAnnouncement(announcementId, userData);
    end(ctx.response(), 200);
  }
}
