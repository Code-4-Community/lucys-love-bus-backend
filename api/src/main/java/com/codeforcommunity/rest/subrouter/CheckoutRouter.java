package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.dto.checkout.PostCheckoutRequest;
import com.codeforcommunity.rest.IRouter;
import com.codeforcommunity.rest.RestFunctions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.util.*;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;

import static com.codeforcommunity.rest.ApiRouter.end;

public class CheckoutRouter implements IRouter {


    public CheckoutRouter() {}

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

        PostCheckoutRequest request = RestFunctions.getJsonBodyAsClass(ctx, PostCheckoutRequest.class);

        Map<String, Object> params = new HashMap<>();
        params.put(
                "success_url",
                request.getSuccess_url()
        );
        params.put(
                "cancel_url",
                request.getCancel_url()
        );
        params.put(
                "payment_method_types",
                request.getPayment_method_types()
        );
        params.put(
                "line_items",
                request.getLine_items()
        );

        try {
            Session session = Session.create(params);

            end(ctx.response(), 200, session.getId());
        } catch (Exception e) {
            end(ctx.response(), 500, "Failed to create session");
        }
    }

}
