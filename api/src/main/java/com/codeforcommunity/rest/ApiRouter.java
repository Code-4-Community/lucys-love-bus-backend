package com.codeforcommunity.rest;

import com.codeforcommunity.api.IAnnouncementsProcessor;
import com.codeforcommunity.api.IAuthProcessor;
import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.api.IProtectedUserProcessor;
import com.codeforcommunity.api.IRequestsProcessor;
import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.auth.JWTAuthorizer;

import com.codeforcommunity.rest.subrouter.AnnouncementsRouter;
import com.codeforcommunity.rest.subrouter.AuthRouter;
import com.codeforcommunity.rest.subrouter.CommonRouter;
import com.codeforcommunity.rest.subrouter.EventsRouter;
import com.codeforcommunity.rest.subrouter.PfRequestRouter;
import com.codeforcommunity.rest.subrouter.CheckoutRouter;
import com.codeforcommunity.rest.subrouter.ProtectedUserRouter;
import com.codeforcommunity.rest.subrouter.WebhooksRouter;
import io.vertx.core.Vertx;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;


public class ApiRouter implements IRouter {
    // allows us to test by allowing public access to private fields
    public static class Externals {
        private final CommonRouter commonRouter;
        private final AuthRouter authRouter;
        private final ProtectedUserRouter protectedUserRouter;
        private final PfRequestRouter requestRouter;
        private final EventsRouter eventsRouter;
        private final AnnouncementsRouter announcementsRouter;
        private final CheckoutRouter checkoutRouter;
        private final WebhooksRouter webhooksRouter;

        public Externals(JWTAuthorizer myJWTAuthorizer,
                         IAuthProcessor authProcessor,
                         IProtectedUserProcessor protectedUserProcessor,
                         IRequestsProcessor requestsProcessor,
                         IEventsProcessor eventsProcessor,
                         IAnnouncementsProcessor announcementEventsProcessor,
                         ICheckoutProcessor checkoutProcessor) {
            this.commonRouter = new CommonRouter(myJWTAuthorizer);
            this.authRouter = new AuthRouter(authProcessor);
            this.protectedUserRouter = new ProtectedUserRouter(protectedUserProcessor);
            this.requestRouter = new PfRequestRouter(requestsProcessor);
            this.eventsRouter = new EventsRouter(eventsProcessor);
            this.announcementsRouter = new AnnouncementsRouter(announcementEventsProcessor);
            this.checkoutRouter = new CheckoutRouter(checkoutProcessor);
            this.webhooksRouter = new WebhooksRouter(checkoutProcessor);
        }

        public CommonRouter getCommonRouter() {
            return this.commonRouter;
        }

        public AuthRouter getAuthRouter() {
            return this.authRouter;
        }

        public ProtectedUserRouter getProtectedUserRouter() {
            return this.protectedUserRouter;
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

        public CheckoutRouter getCheckoutRouter() { 
            return this.checkoutRouter; 
        }

        public WebhooksRouter getWebhooksRouter() { 
            return this.webhooksRouter; 
        }

        public Router getRouter(Vertx vertx) {
            return Router.router(vertx);
        }
    }

    private final Externals externs;

    public ApiRouter(IAuthProcessor authProcessor,
                     IProtectedUserProcessor protectedUserProcessor, 
                     IRequestsProcessor requestsProcessor,
                     IEventsProcessor eventsProcessor, 
                     IAnnouncementsProcessor announcementEventsProcessor,
                     ICheckoutProcessor checkoutProcessor, 
                     JWTAuthorizer jwtAuthorizer) {
        this.externs = new Externals(jwtAuthorizer, authProcessor, protectedUserProcessor,
        requestsProcessor, eventsProcessor, announcementEventsProcessor, checkoutProcessor);
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
        router.mountSubRouter("/webhooks", externs.getWebhooksRouter().initializeRouter(vertx));
        router.mountSubRouter("/protected", defineProtectedRoutes(vertx));

        return router;
    }

    /**
     * Mounts all routes that require a user to be logged in. All routes defined here
     * require a user to have a valid JWT access token in their header.
     */
    private Router defineProtectedRoutes(Vertx vertx) {
        Router router = externs.getRouter(vertx);
        
        router.mountSubRouter("/user", externs.getProtectedUserRouter().initializeRouter(vertx));
        router.mountSubRouter("/requests", externs.getRequestRouter().initializeRouter(vertx));
        router.mountSubRouter("/events", externs.getEventsRouter().initializeRouter(vertx));
        router.mountSubRouter("/announcements", externs.getAnnouncementsRouter().initializeRouter(vertx));
        router.mountSubRouter("/checkout", externs.getCheckoutRouter().initializeRouter(vertx));

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
