package com.codeforcommunity.rest.subrouter;

import static com.codeforcommunity.rest.ApiRouter.end;

import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.rest.IRouter;
import com.codeforcommunity.rest.RestFunctions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class WebhooksRouter implements IRouter {

  private final ICheckoutProcessor checkoutProcessor;

  public WebhooksRouter(ICheckoutProcessor checkoutProcessor) {
    this.checkoutProcessor = checkoutProcessor;
  }

  @Override
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

    registerStripeWebhookHandler(router);

    return router;
  }

  private void registerStripeWebhookHandler(Router router) {
    Route getRequestsRoute = router.post("/stripe");
    getRequestsRoute.handler(this::handleStripeWebhookEvent);
  }

  private void handleStripeWebhookEvent(RoutingContext ctx) {
    String payload = ctx.getBodyAsString();
    String sigHeader = RestFunctions.getRequestHeader(ctx.request(), "Stripe-Signature");

    this.checkoutProcessor.handleStripeCheckoutEventComplete(payload, sigHeader);

    end(ctx.response(), 200);
  }
}
