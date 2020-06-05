package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class EventDoesNotExistException extends HandledException {
  private final int eventId;

  public EventDoesNotExistException(int eventId) {
    this.eventId = eventId;
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleEventDoesNotExistException(ctx, this);
  }

  public int getEventId() {
    return eventId;
  }
}
