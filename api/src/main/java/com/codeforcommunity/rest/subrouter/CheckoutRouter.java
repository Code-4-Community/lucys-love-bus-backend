package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.PostCreateCheckoutSession;
import com.codeforcommunity.dto.checkout.PostCreateEventRegistrations;
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

        registerCreateEventRegistrationsHandler(router);
        registerCheckoutSessionHandler(router);

        return router;
    }

    private void registerCreateEventRegistrationsHandler(Router router) {
        Route getRequestsRoute = router.post("/events");
        getRequestsRoute.handler(this::handleCreateEventsRegistration);
    }

    private void registerCheckoutSessionHandler(Router router) {
        Route getRequestsRoute = router.post("/session");
        getRequestsRoute.handler(this::handleCreateCheckoutSession);
    }

    private void handleCreateEventsRegistration(RoutingContext ctx) {
        PostCreateEventRegistrations requestData =
                RestFunctions.getJsonBodyAsClass(ctx, PostCreateEventRegistrations.class);
        JWTData userData = ctx.get("jwt_data");

        processor.createEventRegistration(requestData, userData);

        end(ctx.response(), 200, "Nothing failed, but you weren't actually registered");
    }

    private void handleCreateCheckoutSession(RoutingContext ctx) {
        PostCreateCheckoutSession requestData = RestFunctions.getJsonBodyAsClass(ctx, PostCreateCheckoutSession.class);
        JWTData userData = ctx.get("jwt_data");

        String checkoutSessionID = processor.createCheckoutSession(requestData, userData);

        end(ctx.response(), 200, checkoutSessionID);
    }

}
