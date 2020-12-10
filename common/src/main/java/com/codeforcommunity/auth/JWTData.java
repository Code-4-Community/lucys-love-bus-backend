package com.codeforcommunity.auth;

import com.codeforcommunity.enums.PrivilegeLevel;

public class JWTData {

  private final int userId;
  private final PrivilegeLevel privilegeLevel;

  public JWTData(int userId, PrivilegeLevel privilegeLevel) {
    this.userId = userId;
    this.privilegeLevel = privilegeLevel;
  }

  public int getUserId() {
    return this.userId;
  }

  public PrivilegeLevel getPrivilegeLevel() {
    return this.privilegeLevel;
  }
}
