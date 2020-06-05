package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class AlreadyRegisteredException extends HandledException {

  private String eventTitle;

  public AlreadyRegisteredException(String eventTitle) {
    this.eventTitle = eventTitle;
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleAlreadyRegisteredException(ctx, this);
  }

  public String getEventTitle() {
    return this.eventTitle;
  }
}
