package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.api.IProtectedEventsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.requests.ModifyEventRequest;
import com.codeforcommunity.dto.userEvents.responses.EventRegistrations;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.rest.IRouter;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import static com.codeforcommunity.rest.ApiRouter.end;
import static com.codeforcommunity.rest.RestFunctions.getJsonBodyAsClass;
import static com.codeforcommunity.rest.RestFunctions.getRequestParameterAsInt;

public class ProtectedEventsRouter implements IRouter {

    private final IProtectedEventsProcessor processor;

    public ProtectedEventsRouter(IProtectedEventsProcessor processor) {
        this.processor = processor;
    }

    @Override
    public Router initializeRouter(Vertx vertx) {
        Router router = Router.router(vertx);

        registerCreateEvent(router);
        registerModifyEvent(router);
        registerDeleteEvent(router);

        return router;
    }

    private void registerCreateEvent(Router router) {
        Route createRequestRoute = router.post("/");
        createRequestRoute.handler(this::handleCreateEventRoute);
    }

    private void registerModifyEvent(Router router) {
        Route modifyEventRoute = router.put("/:event_id");
        modifyEventRoute.handler(this::handleModifyEventRoute);
    }

    private void registerDeleteEvent(Router router) {
        Route deleteEventRoute = router.delete("/:event_id");
        deleteEventRoute.handler(this::handleDeleteEventRoute);
    }

    private void registerGetEventRegisteredUsers(Router router) {
        Route getUsersSignedUpEventRoute = router.get("/:event_id/registrations");
        getUsersSignedUpEventRoute.handler(this::handleGetEventRegisteredUsers);
    }

    private void registerGetEventRSVPs(Router router) {
        Route getUsersSignedUpEventRoute = router.get("/:event_id/rsvps");
        getUsersSignedUpEventRoute.handler(this::handleGetEventRSVPs);
    }

    private void handleCreateEventRoute(RoutingContext ctx) {
        CreateEventRequest requestData = getJsonBodyAsClass(ctx, CreateEventRequest.class);
        JWTData userData = ctx.get("jwt_data");

        SingleEventResponse response = processor.createEvent(requestData, userData);

        end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
    }

    private void handleModifyEventRoute(RoutingContext ctx) {
        int eventId = getRequestParameterAsInt(ctx.request(), "event_id");
        ModifyEventRequest requestData = getJsonBodyAsClass(ctx, ModifyEventRequest.class);
        JWTData userData = ctx.get("jwt_data");

        SingleEventResponse response = processor.modifyEvent(eventId, requestData, userData);
        end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
    }

    private void handleDeleteEventRoute(RoutingContext ctx) {
        int eventId = getRequestParameterAsInt(ctx.request(), "event_id");
        JWTData userData = ctx.get("jwt_data");

        processor.deleteEvent(eventId, userData);
        end(ctx.response(), 200);
    }

    private void handleGetEventRegisteredUsers(RoutingContext ctx) {
        int eventId = getRequestParameterAsInt(ctx.request(), "event_id");
        JWTData userData = ctx.get("jwt_data");

        EventRegistrations regs = processor.getEventRegisteredUsers(eventId, userData);
        end(ctx.response(), 200, JsonObject.mapFrom(regs).encode());
    }

    private void handleGetEventRSVPs(RoutingContext ctx) {
        int eventId = getRequestParameterAsInt(ctx.request(), "event_id");
        JWTData userData = ctx.get("jwt_data");

        String rsvp = processor.getEventRSVPs(eventId, userData);
        end(ctx.response(), 200, rsvp, "text/csv");
    }
}
