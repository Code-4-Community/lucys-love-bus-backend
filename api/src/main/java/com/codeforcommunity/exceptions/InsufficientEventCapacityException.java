package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class InsufficientEventCapacityException extends HandledException {
  private String eventTitle;

  public InsufficientEventCapacityException(String eventTitle) {
    this.eventTitle = eventTitle;
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleInsufficientEventCapacityException(ctx, this);
  }

  public String getEventTitle() {
    return this.eventTitle;
  }
}
