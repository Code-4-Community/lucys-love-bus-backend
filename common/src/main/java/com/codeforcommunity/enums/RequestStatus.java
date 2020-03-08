package com.codeforcommunity.enums;

public enum RequestStatus {
  PENDING(0), APPROVED(1), REJECTED(2);

  private int val;

  RequestStatus(int val) {
    this.val = val;
  }

  public int getVal() {
    return val;
  }

  public static RequestStatus from(Integer val) {
    for (RequestStatus status : RequestStatus.values()) {
      if (status.val == val) {
        return status;
      }
    }
    throw new IllegalArgumentException(String.format("Given num (%d) that doesn't correspond to any RequestStatus", val));
  }
}
