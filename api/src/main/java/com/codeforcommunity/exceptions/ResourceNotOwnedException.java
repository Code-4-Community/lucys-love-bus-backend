package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class ResourceNotOwnedException extends HandledException {

  private String resource;

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleResourceNotOwned(ctx, this);
  }

  public ResourceNotOwnedException(String resource) {
    this.resource = resource;
  }

  public String getResource() {
    return resource;
  }
}
