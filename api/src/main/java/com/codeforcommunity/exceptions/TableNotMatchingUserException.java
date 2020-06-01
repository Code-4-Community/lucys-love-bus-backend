package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class TableNotMatchingUserException extends HandledException {

  private String tableName;
  private int tableId;

  public TableNotMatchingUserException(String tableName, int tableId) {
    this.tableName = tableName;
    this.tableId = tableId;
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleTableNotMatchingUser(ctx, this);
  }

  public String getTableName() {
    return tableName;
  }

  public int getTableId() {
    return tableId;
  }
}
