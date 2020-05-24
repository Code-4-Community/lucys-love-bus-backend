package com.codeforcommunity.rest.subrouter;

import static com.codeforcommunity.rest.ApiRouter.end;

import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.PostCreateEventRegistrations;
import com.codeforcommunity.rest.IRouter;
import com.codeforcommunity.rest.RestFunctions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class CheckoutRouter implements IRouter {

  private final ICheckoutProcessor processor;

  public CheckoutRouter(ICheckoutProcessor processor) {
    this.processor = processor;
  }

  @Override
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

    registerCreateEventRegistrationsHandler(router);
    registerCheckoutSessionAndEventRegistrationHandler(router);

    return router;
  }

  private void registerCreateEventRegistrationsHandler(Router router) {
    Route getRequestsRoute = router.post("/register");
    getRequestsRoute.handler(this::handleCreateEventsRegistration);
  }

  private void registerCheckoutSessionAndEventRegistrationHandler(Router router) {
    Route getRequestsRoute = router.post("/payment");
    getRequestsRoute.handler(this::handleCreateCheckoutSessionAndEventRegistration);
  }

  private void handleCreateEventsRegistration(RoutingContext ctx) {
    PostCreateEventRegistrations requestData =
        RestFunctions.getJsonBodyAsClass(ctx, PostCreateEventRegistrations.class);
    JWTData userData = ctx.get("jwt_data");

    processor.createEventRegistration(requestData, userData);

    end(ctx.response(), 200, "Successfully registered!");
  }

  private void handleCreateCheckoutSessionAndEventRegistration(RoutingContext ctx) {
    PostCreateEventRegistrations requestData =
        RestFunctions.getJsonBodyAsClass(ctx, PostCreateEventRegistrations.class);
    JWTData userData = ctx.get("jwt_data");

    String checkoutSessionID =
        processor.createCheckoutSessionAndEventRegistration(requestData, userData);

    end(ctx.response(), 200, checkoutSessionID);
  }
}
