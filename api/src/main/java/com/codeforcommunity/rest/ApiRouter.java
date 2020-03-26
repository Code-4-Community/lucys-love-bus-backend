package com.codeforcommunity.rest;

import com.codeforcommunity.api.IAuthProcessor;
import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.api.IRequestsProcessor;
import com.codeforcommunity.auth.JWTAuthorizer;

import com.codeforcommunity.rest.subrouter.AuthRouter;
import com.codeforcommunity.rest.subrouter.CommonRouter;
import com.codeforcommunity.rest.subrouter.EventsRouter;
import com.codeforcommunity.rest.subrouter.PfRequestRouter;
import io.vertx.core.Vertx;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;


public class ApiRouter implements IRouter {
    private final CommonRouter commonRouter;
    private final AuthRouter authRouter;
    private final PfRequestRouter requestRouter;
    private final EventsRouter eventsRouter;

    public ApiRouter(IAuthProcessor authProcessor, IRequestsProcessor requestsProcessor, IEventsProcessor eventsProcessor, JWTAuthorizer jwtAuthorizer) {
        this.commonRouter = new CommonRouter(jwtAuthorizer);
        this.authRouter = new AuthRouter(authProcessor);
        this.requestRouter = new PfRequestRouter(requestsProcessor);
        this.eventsRouter = new EventsRouter(eventsProcessor);
    }

    /**
     * Initialize a router and register all route handlers on it.
     */
    public Router initializeRouter(Vertx vertx) {
        Router router = commonRouter.initializeRouter(vertx);

        router.mountSubRouter("/user", authRouter.initializeRouter(vertx));
        router.mountSubRouter("/protected", defineProtectedRoutes(vertx));

        return router;
    }

    /**
     * Mounts all routes that require a user to be logged in. All routes defined here
     * require a user to have a valid JWT access token in their header.
     */
    private Router defineProtectedRoutes(Vertx vertx) {
        Router router = Router.router(vertx);

        router.mountSubRouter("/requests", requestRouter.initializeRouter(vertx));
        router.mountSubRouter("/events", eventsRouter.initializeRouter(vertx));

        return router;
    }


    public static void end(HttpServerResponse response, int statusCode) {
        end(response, statusCode, null);
    }

    public static void end(HttpServerResponse response, int statusCode, String jsonBody) {
        response.setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json")
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Access-Control-Allow-Methods", "DELETE, POST, GET, OPTIONS")
                .putHeader("Access-Control-Allow-Headers", "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
        if (jsonBody == null || jsonBody.equals("")) {
            response.end();
        } else {
            response.end(jsonBody);
        }
    }
}
