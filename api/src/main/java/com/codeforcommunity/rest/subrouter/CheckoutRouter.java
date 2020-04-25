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
import java.util.*;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;

import static com.codeforcommunity.rest.ApiRouter.end;

public class CheckoutRouter implements IRouter {

    private final IEventsProcessor processor;

    public CheckoutRouter(IEventsProcessor processor) {
        this.processor = processor;
    }

    @Override
    public Router initializeRouter(Vertx vertx) {
        Router router = Router.router(vertx);

        registerCheckoutSession(router);

        return router;
    }

    private void registerCheckoutSession(Router router) {
        Route getRequestsRoute = router.post("/");
        getRequestsRoute.handler(this::handleCheckoutSession);
    }

    private void handleCheckoutSession(RoutingContext ctx) {
        Stripe.apiKey = "sk_test_Q2wTkIY5Z3h9pjtgkksJULj200M84LsI3q";

        List<Object> paymentMethodTypes =
                new ArrayList<>();
        paymentMethodTypes.add("card");
        List<Object> lineItems = new ArrayList<>();
        Map<String, Object> lineItem1 = new HashMap<>();
        lineItem1.put("name", "T-shirt");
        lineItem1.put(
                "description",
                "Comfortable cotton t-shirt"
        );
        lineItem1.put("amount", 1500);
        lineItem1.put("currency", "usd");
        lineItem1.put("quantity", 2);
        lineItems.add(lineItem1);
        Map<String, Object> params = new HashMap<>();
        params.put(
                "success_url",
                "https://example.com/success"
        );
        params.put(
                "cancel_url",
                "https://example.com/cancel"
        );
        params.put(
                "payment_method_types",
                paymentMethodTypes
        );
        params.put("line_items", lineItems);

        try {
            Session session = Session.create(params);
        } catch (Exception e) {
            end(ctx.response(), 500, "Failed to create session");
        }

        end(ctx.response(), 200, "JsonObject.mapFrom(response).encode()");
    }

}
