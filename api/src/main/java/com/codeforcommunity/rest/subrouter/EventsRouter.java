package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.requests.ModifyEventRequest;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.dto.userEvents.requests.GetUserEventsRequest;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.rest.IRouter;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static com.codeforcommunity.rest.ApiRouter.end;
import static com.codeforcommunity.rest.RestFunctions.getJsonBodyAsClass;
import static com.codeforcommunity.rest.RestFunctions.getMultipleQueryParams;
import static com.codeforcommunity.rest.RestFunctions.getOptionalQueryParam;
import static com.codeforcommunity.rest.RestFunctions.getRequestParameterAsInt;

public class EventsRouter implements IRouter {

  private final IEventsProcessor processor;

  public EventsRouter(IEventsProcessor processor) {
    this.processor = processor;
  }

  @Override
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

    registerCreateEvent(router);
    registerGetEvents(router);
    registerGetUserEventsQualified(router);
    registerGetUserEventsSignedUp(router);
    registerGetSingleEvent(router);
    registerModifyEvent(router);
    registerDeleteEvent(router);

    return router;
  }

  private void registerCreateEvent(Router router) {
    Route createRequestRoute = router.post("/");
    createRequestRoute.handler(this::handleCreateEventRoute);
  }

  private void registerGetSingleEvent(Router router) {
    Route getRequestsRoute = router.get("/:event_id");
    getRequestsRoute.handler(this::handleGetSingleEventRoute);
  }

  private void registerGetUserEventsSignedUp(Router router) {
    Route getUserEventsSignedUp = router.get("/signed_up");
    getUserEventsSignedUp.handler(this::handleGetUserEventsSignedUp);
  }

  private void registerGetUserEventsQualified(Router router) {
    Route getUserEventQualified = router.get("/qualified");
    getUserEventQualified.handler(this::handleGetUserEventsQualified);
  }

  private void registerGetEvents(Router router) {
    Route getEvent = router.get("/");
    getEvent.handler(this::handleGetEvents);
  }

  private void registerModifyEvent(Router router) {
    Route modifyEventRoute = router.put("/:event_id");
    modifyEventRoute.handler(this::handleModifyEventRoute);
  }

  private void registerDeleteEvent(Router router) {
    Route deleteEventRoute = router.delete("/:event_id");
    deleteEventRoute.handler(this::handleDeleteEventRoute);
  }

  private void handleGetEvents(RoutingContext ctx) {

    List<Integer> intIds = getMultipleQueryParams(ctx, "ids",
            str -> Integer.parseInt(str));

    GetEventsResponse response = processor.getEvents(intIds);

    end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
  }

  private void handleGetUserEventsQualified(RoutingContext ctx) {
     GetEventsResponse response = processor.getEventsQualified(ctx.get("jwt_data"));
     end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
  }

  private void handleGetUserEventsSignedUp(RoutingContext ctx) {

    Optional<Integer> count = getOptionalQueryParam(ctx, "count", str -> Integer.parseInt(str));
    Optional<Timestamp> endDate = getOptionalQueryParam(ctx, "end", str -> Timestamp.valueOf(str));
    Optional<Timestamp> startDate = getOptionalQueryParam(ctx, "start", str -> Timestamp.valueOf(str));

    GetUserEventsRequest request = new GetUserEventsRequest(endDate, startDate, count);

    JWTData userData = ctx.get("jwt_data");

    GetEventsResponse response = processor.getEventsSignedUp(request, userData);

    end(ctx.response(),200, JsonObject.mapFrom(response).encode());
  }

  private void handleCreateEventRoute(RoutingContext ctx) {
    CreateEventRequest requestData = getJsonBodyAsClass(ctx, CreateEventRequest.class);
    JWTData userData = ctx.get("jwt_data");

    SingleEventResponse response = processor.createEvent(requestData, userData);

    end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
  }

  private void handleGetSingleEventRoute(RoutingContext ctx) {
    int eventId = getRequestParameterAsInt(ctx.request(), "event_id");

    SingleEventResponse response = processor.getSingleEvent(eventId);

    end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
  }

  private void handleModifyEventRoute(RoutingContext ctx) {
    int eventId = getRequestParameterAsInt(ctx.request(), "event_id");
    ModifyEventRequest requestData = getJsonBodyAsClass(ctx, ModifyEventRequest.class);
    JWTData userData = ctx.get("jwt_data");

    SingleEventResponse response = processor.modifyEvent(eventId, requestData, userData);
    end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
  }

  private void handleDeleteEventRoute(RoutingContext ctx) {
    int eventId = getRequestParameterAsInt(ctx.request(), "event_id");
    JWTData userData = ctx.get("jwt_data");

    processor.deleteEvent(eventId, userData);
    end(ctx.response(), 200);
  }

}
