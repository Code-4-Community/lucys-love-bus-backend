package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.exceptions.StripeExternalException;
import com.codeforcommunity.rest.IRouter;
import com.codeforcommunity.rest.RestFunctions;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
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
        String endpointSecret = "whsec_TXciotTvq1luyU8wMppJAMJE9pBLDZ33"; // TODO: where does this go?

        try {
            Event event = Webhook.constructEvent(
                    payload, sigHeader, endpointSecret
            );
            if (event.getType().equals("checkout.session.completed")) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().get();
                checkoutProcessor.handleStripeCheckoutEventComplete(session);
            } else {
                throw new StripeExternalException("Invalid event sent to stripe webhook");
            }
        } catch (SignatureVerificationException e) {
            throw new StripeExternalException("Error verifying signature of incoming webhook");
        }
    }
}
