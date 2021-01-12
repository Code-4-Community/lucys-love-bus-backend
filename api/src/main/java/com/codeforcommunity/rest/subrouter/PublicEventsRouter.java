package com.codeforcommunity.rest.subrouter;

import static com.codeforcommunity.rest.ApiRouter.end;
import static com.codeforcommunity.rest.RestFunctions.getMultipleQueryParams;

import com.codeforcommunity.api.IPublicEventsProcessor;
import com.codeforcommunity.dto.userEvents.responses.GetPublicEventsResponse;
import com.codeforcommunity.rest.IRouter;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.util.List;

public class PublicEventsRouter implements IRouter {

  private final IPublicEventsProcessor processor;

  public PublicEventsRouter(IPublicEventsProcessor processor) {
    this.processor = processor;
  }

  @Override
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);
    registerGetEvents(router);
    return router;
  }

  private void registerGetEvents(Router router) {
    Route getEvent = router.get("/");
    getEvent.handler(this::handleGetEvents);
  }

  private void handleGetEvents(RoutingContext ctx) {
    List<Integer> eventIds = getMultipleQueryParams(ctx, "ids", Integer::parseInt);
    GetPublicEventsResponse response = processor.getPublicEvents(eventIds);
    end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
  }
}
