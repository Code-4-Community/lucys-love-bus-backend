package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class EventRegistrationException extends RuntimeException implements HandledException {
    private String message;

    public EventRegistrationException(String message) {
        this.message = message;
    }

    @Override
    public void callHandler(FailureHandler handler, RoutingContext ctx) {
        handler.handleEventRegistrationException(ctx, this);
    }

    public String getMessage() {
        return this.message;
    }
}