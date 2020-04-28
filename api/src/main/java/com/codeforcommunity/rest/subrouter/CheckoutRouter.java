package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.auth.JWTData;
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

        registerCheckoutHandler(router);

        return router;
    }

    private void registerCheckoutHandler(Router router) {
        Route getRequestsRoute = router.post("/");
        getRequestsRoute.handler(this::handleCheckoutEvents);
    }

    private void handleCheckoutEvents(RoutingContext ctx) {
        PostCheckoutRequest requestData = RestFunctions.getJsonBodyAsClass(ctx, PostCheckoutRequest.class);
        JWTData userData = ctx.get("jwt_data");

        switch (userData.getPrivilegeLevel()) {
            case PF:
            case ADMIN:
                processor.createEventRegistration(requestData, userData);
                end(ctx.response(), 200, "Nothing failed, but you weren't actually registered");
                break;
            case GP:
                processor.createEventRegistration(requestData, userData);
                String checkoutSessionID = processor.createCheckoutSession(requestData, userData);
                end(ctx.response(), 200, checkoutSessionID);
                break;
        }
    }

}
