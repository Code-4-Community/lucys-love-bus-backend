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
        Route getRequestsRoute = router.get("/checkout");
        getRequestsRoute.handler(this::handleCheckoutSession);
    }

    private void handleCheckoutSession(RoutingContext ctx) {
        end(ctx.response(), 200, "JsonObject.mapFrom(response).encode()");
    }

}
