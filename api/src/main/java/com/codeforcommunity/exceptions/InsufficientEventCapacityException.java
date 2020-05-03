package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class InsufficientEventCapacityException extends RuntimeException implements HandledException {
    private String message;

    public InsufficientEventCapacityException(String message) {
        this.message = message;
    }

    @Override
    public void callHandler(FailureHandler handler, RoutingContext ctx) {
        handler.handleInsufficientEventCapacityException(ctx, this);
    }

    public String getMessage() {
        return this.message;
    }
}