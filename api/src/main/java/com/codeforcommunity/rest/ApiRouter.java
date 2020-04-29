package com.codeforcommunity.rest;

import com.codeforcommunity.api.IAnnouncementsProcessor;
import com.codeforcommunity.api.IAuthProcessor;
import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.api.IRequestsProcessor;
import com.codeforcommunity.auth.JWTAuthorizer;

import com.codeforcommunity.rest.subrouter.AnnouncementsRouter;
import com.codeforcommunity.rest.subrouter.AuthRouter;
import com.codeforcommunity.rest.subrouter.CommonRouter;
import com.codeforcommunity.rest.subrouter.EventsRouter;
import com.codeforcommunity.rest.subrouter.PfRequestRouter;
import io.vertx.core.Vertx;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;


public class ApiRouter implements IRouter {
    // allows us to test by allowing public access to private fields
    public static class Externals {
        private CommonRouter commonRouter;
        private AuthRouter authRouter;
        private PfRequestRouter requestRouter;
        private EventsRouter eventsRouter;
        private AnnouncementsRouter announcementsRouter;

        public Externals(JWTAuthorizer myJWTAuthorizer,
                         IAuthProcessor authProcessor,
                         IRequestsProcessor requestsProcessor,
                         IEventsProcessor eventsProcessor,
                         IAnnouncementsProcessor announcementEventsProcessor) {
            this.commonRouter = new CommonRouter(myJWTAuthorizer);
            this.authRouter = new AuthRouter(authProcessor);
            this.requestRouter = new PfRequestRouter(requestsProcessor);
            this.eventsRouter = new EventsRouter(eventsProcessor);
            this.announcementsRouter = new AnnouncementsRouter(announcementEventsProcessor);
        }

        public CommonRouter getCommonRouter() {
            return this.commonRouter;
        }

        public AuthRouter getAuthRouter() {
            return this.authRouter;
        }

        public PfRequestRouter getRequestRouter() {
            return this.requestRouter;
        }

        public EventsRouter getEventsRouter() {
            return this.eventsRouter;
        }

        public AnnouncementsRouter getAnnouncementsRouter() {
            return this.announcementsRouter;
        }

        public Router getRouter(Vertx vertx) {
            return Router.router(vertx);
        }
    }

    private final Externals externs;

    public ApiRouter(IAuthProcessor authProcessor, IRequestsProcessor requestsProcessor,
        IEventsProcessor eventsProcessor, IAnnouncementsProcessor announcementEventsProcessor,
        JWTAuthorizer jwtAuthorizer) {
        this.externs = new Externals(jwtAuthorizer, authProcessor, requestsProcessor, eventsProcessor,
            announcementEventsProcessor);
    }

    public ApiRouter(Externals externs) {
        this.externs = externs;
    }

    /**
     * Initialize a router and register all route handlers on it.
     */
    public Router initializeRouter(Vertx vertx) {
        Router router = externs.getCommonRouter().initializeRouter(vertx);

        router.mountSubRouter("/user", externs.getAuthRouter().initializeRouter(vertx));
        router.mountSubRouter("/protected", defineProtectedRoutes(vertx));

        return router;
    }

    /**
     * Mounts all routes that require a user to be logged in. All routes defined here
     * require a user to have a valid JWT access token in their header.
     */
    private Router defineProtectedRoutes(Vertx vertx) {
        Router router = externs.getRouter(vertx);

        router.mountSubRouter("/requests", externs.getRequestRouter().initializeRouter(vertx));
        router.mountSubRouter("/events", externs.getEventsRouter().initializeRouter(vertx));
        router.mountSubRouter("/announcements", externs.getAnnouncementsRouter().initializeRouter(vertx));

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
