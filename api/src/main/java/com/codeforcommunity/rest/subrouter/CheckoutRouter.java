package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.dto.checkout.PostCheckoutRequest;
import com.codeforcommunity.rest.IRouter;
import com.codeforcommunity.rest.RestFunctions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import static com.codeforcommunity.rest.ApiRouter.end;

public class CheckoutRouter implements IRouter {

    private final ICheckoutProcessor processor;

    public CheckoutRouter(ICheckoutProcessor processor) { this.processor = processor; }

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
        PostCheckoutRequest request = RestFunctions.getJsonBodyAsClass(ctx, PostCheckoutRequest.class);

        try {
            String response = processor.createCheckoutSession(request);
            end(ctx.response(), 200, response);
        } catch (Exception e) {
            end(ctx.response(), 500, "Failed to create session");
        }
    }

}
