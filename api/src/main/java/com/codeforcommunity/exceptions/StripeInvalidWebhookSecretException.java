package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class StripeInvalidWebhookSecretException extends HandledException {

  private String message;

  public StripeInvalidWebhookSecretException(String message) {
    this.message = message;
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleStripeInvalidWebhookSecretException(ctx, this);
  }

  public String getMessage() {
    return this.message;
  }
}
