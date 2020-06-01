package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class NotRegisteredException extends HandledException {

  private String eventTitle;

  public NotRegisteredException(String eventTitle) {
    this.eventTitle = eventTitle;
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleNotRegisteredException(ctx, this);
  }

  public String getEventTitle() {
    return this.eventTitle;
  }
}
