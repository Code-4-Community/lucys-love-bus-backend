package com.codeforcommunity.enums;

public enum EventRegistrationStatus {
  ACTIVE(1),
  CANCELLED(2),
  PAYMENT_INCOMPLETE(3);

  private int val;

  EventRegistrationStatus(int val) {
    this.val = val;
  }

  public int getVal() {
    return val;
  }

  public static EventRegistrationStatus from(Integer val) {
    for (EventRegistrationStatus registrationStatus : EventRegistrationStatus.values()) {
      if (registrationStatus.val == val) {
        return registrationStatus;
      }
    }
    throw new IllegalArgumentException(
        String.format("Given num (%d) that doesn't correspond to any PrivilegeLevel", val));
  }
}
