package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class AccessTokenInvalidException extends RuntimeException implements HandledException {

  public AccessTokenInvalidException() {
    super();
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleAccessTokenInvalid(ctx);
  }
}
