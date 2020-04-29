package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.checkout.PostCheckoutRequest;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.WrongPrivilegeException;
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

        registerCreateEventRegistrationHandler(router);
        registerCheckoutSessionHandler(router);

        return router;
    }

    private void registerCreateEventRegistrationHandler(Router router) {
        Route getRequestsRoute = router.post("/event");
        getRequestsRoute.handler(this::handleCreateEventRegistration);
    }

    private void registerCheckoutSessionHandler(Router router) {
        Route getRequestsRoute = router.post("/session");
        getRequestsRoute.handler(this::handleCreateCheckoutSession);
    }

    private void handleCreateEventRegistration(RoutingContext ctx) {
        PostCheckoutRequest requestData = RestFunctions.getJsonBodyAsClass(ctx, PostCheckoutRequest.class);
        JWTData userData = ctx.get("jwt_data");

        if (userData.getPrivilegeLevel() != PrivilegeLevel.PF
                && userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
            throw new WrongPrivilegeException(PrivilegeLevel.PF);
        }

        processor.createEventRegistration(requestData, userData);
        end(ctx.response(), 200, "Nothing failed, but you weren't actually registered");
    }

    private void handleCreateCheckoutSession(RoutingContext ctx) {
        PostCheckoutRequest requestData = RestFunctions.getJsonBodyAsClass(ctx, PostCheckoutRequest.class);
        JWTData userData = ctx.get("jwt_data");

        if (userData.getPrivilegeLevel() != PrivilegeLevel.GP) {
            throw new WrongPrivilegeException(PrivilegeLevel.GP);
        }

        processor.createEventRegistration(requestData, userData);
        String checkoutSessionID = processor.createCheckoutSession(requestData, userData);
        end(ctx.response(), 200, checkoutSessionID);
    }

}
