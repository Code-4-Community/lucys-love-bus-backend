package com.codeforcommunity.exceptions;

import com.codeforcommunity.enums.VerificationKeyType;
import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class InvalidSecretKeyException extends RuntimeException implements HandledException {

  private final VerificationKeyType type;

  public InvalidSecretKeyException(VerificationKeyType type) {
    super();
    this.type = type;
  }

  public VerificationKeyType getType() {
    return type;
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleInvalidSecretKey(ctx, this);
  }
}