package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class RequestDoesNotExistException extends HandledException {

  private final int requestId;

  public RequestDoesNotExistException(int requestId) {
    this.requestId = requestId;
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleRequestDoesNotExist(ctx, this);
  }

  public int getRequestId() {
    return requestId;
  }
}
