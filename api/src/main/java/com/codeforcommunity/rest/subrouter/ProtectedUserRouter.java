package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.api.IProtectedUserProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.requests.GetUserEventsRequest;
import com.codeforcommunity.dto.userEvents.requests.ModifyEventRequest;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
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

public class ProtectedUserRouter implements IRouter {

  private final IProtectedUserProcessor processor;

  public ProtectedUserRouter(IProtectedUserProcessor processor) {
    this.processor = processor;
  }

  @Override
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

    registerDeleteUser(router);

    return router;
  }

  private void registerDeleteUser(Router router) {
    Route deleteUserRoute = router.delete("/");
    deleteUserRoute.handler(this::handleDeleteUser);
  }

  private void handleDeleteUser(RoutingContext ctx) {
    JWTData userData = ctx.get("jwt_data");

    processor.deleteUser(userData);

    end(ctx.response(), 200);
  }
}
