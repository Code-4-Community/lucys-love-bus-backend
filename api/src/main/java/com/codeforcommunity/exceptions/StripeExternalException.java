package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class StripeExternalException extends HandledException {

  private String message;

  public StripeExternalException(String message) {
    this.message = message;
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleStripeExternalException(ctx, this);
  }

  public String getMessage() {
    return this.message;
  }
}
