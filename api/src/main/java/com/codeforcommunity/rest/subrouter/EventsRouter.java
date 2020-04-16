package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.events.CreateEventRequest;
import com.codeforcommunity.dto.events.SingleEventResponse;
import com.codeforcommunity.rest.IRouter;
import com.codeforcommunity.rest.RestFunctions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

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
