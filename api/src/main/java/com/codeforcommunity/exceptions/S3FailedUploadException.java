package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class S3FailedUploadException extends RuntimeException implements HandledException {
  private String message;

  public S3FailedUploadException(String message) {
    this.message = message;
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleS3FailedUpload(ctx, this.message);
  }
}