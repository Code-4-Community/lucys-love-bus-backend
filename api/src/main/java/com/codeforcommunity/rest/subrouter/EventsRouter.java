package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.dto.userEvents.requests.GetUserEventsRequest;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.rest.IRouter;
import com.codeforcommunity.rest.RestFunctions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

import static com.codeforcommunity.rest.ApiRouter.end;

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

  private void handleGetEvents(RoutingContext ctx) {

    List<Integer> intIds = RestFunctions.getMultipleQueryParams(ctx, "ids",
            RestFunctions.getParseIntParamMapper());

    GetEventsResponse response = processor.getEvents(intIds);

    end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
  }

  private void handleGetUserEventsQualified(RoutingContext ctx) {
     GetEventsResponse response = processor.getEventsQualified(ctx.get("jwt_data"));
     end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
  }

  private void handleGetUserEventsSignedUp(RoutingContext ctx) {

    GetUserEventsRequest request = new GetUserEventsRequest() {{
      setCount(RestFunctions.getOptionalQueryParam(ctx, "count", RestFunctions.getParseIntParamMapper()));
      setEndDate(RestFunctions.getOptionalQueryParam(ctx, "end", RestFunctions.getDateParamMapper()));
      setStartDate(RestFunctions.getOptionalQueryParam(ctx, "start", RestFunctions.getDateParamMapper()));
    }};

    JWTData userData = ctx.get("jwt_data");

    GetEventsResponse response = processor.getEventsSignedUp(request, userData);

    end(ctx.response(),200, JsonObject.mapFrom(response).encode());
  }

  private void handleCreateEventRoute(RoutingContext ctx) {
    CreateEventRequest requestData = RestFunctions.getJsonBodyAsClass(ctx, CreateEventRequest.class);
    JWTData userData = ctx.get("jwt_data");

    SingleEventResponse response = processor.createEvent(requestData, userData);

    end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
  }

  private void handleGetSingleEventRoute(RoutingContext ctx) {
    int eventId = RestFunctions.getRequestParameterAsInt(ctx.request(), "event_id");

    SingleEventResponse response = processor.getSingleEvent(eventId);

    end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
  }

}
