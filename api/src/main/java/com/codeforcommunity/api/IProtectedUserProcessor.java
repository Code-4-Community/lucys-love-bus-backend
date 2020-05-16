package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.protected_user.SetContactsAndChildrenoRequest;
import com.codeforcommunity.dto.user.ChangePasswordRequest;

public interface IProtectedUserProcessor {

  /**
   * Deletes the given user from the database. Does NOT invalidate the user's JWT tokens.
   */
  void deleteUser(JWTData userData);

  /**
   * If the given current password matches the user's current password, update the
   * user's password to the new password value.
   */
  void changePassword(JWTData userData, ChangePasswordRequest changePasswordRequest);

  void setContactsAndChildren(JWTData userData, SetContactsAndChildrenoRequest setContactsAndChildrenRequest);
}
