package com.codeforcommunity.enums;

public enum PrivilegeLevel {
  // TODO: do not alter the order of these until the corresponding DB column is migrated to a string
  STANDARD("standard"),
  PF("pf"),
  ADMIN("admin");

  private String name;

  PrivilegeLevel(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static PrivilegeLevel from(String name) {
    for (PrivilegeLevel privilegeLevel : PrivilegeLevel.values()) {
      if (privilegeLevel.name.equals(name)) {
        return privilegeLevel;
      }
    }
    throw new IllegalArgumentException(
        String.format("Given name `%s` doesn't correspond to any `PrivilegeLevel`", name));
  }
}
