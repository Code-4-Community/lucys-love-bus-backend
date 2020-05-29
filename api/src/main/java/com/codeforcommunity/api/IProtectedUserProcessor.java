package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.protected_user.ChangeEmailRequest;
import com.codeforcommunity.dto.protected_user.SetContactsAndChildrenRequest;
import com.codeforcommunity.dto.protected_user.UserInformation;
import com.codeforcommunity.dto.user.ChangePasswordRequest;

public interface IProtectedUserProcessor {

  /** Deletes the given user from the database. Does NOT invalidate the user's JWT tokens. */
  void deleteUser(JWTData userData);

  /**
   * If the given current password matches the user's current password, update the user's password
   * to the new password value.
   */
  void changePassword(JWTData userData, ChangePasswordRequest changePasswordRequest);

  /**
   * If the given password matches the calling user's password, update the email associated with the
   * main contact and the user account.
   */
  void changePrimaryEmail(JWTData userData, ChangeEmailRequest changeEmailRequest);

  /** Set the contact and children tables associated with the calling user's account. */
  void setContactsAndChildren(
      JWTData userData, SetContactsAndChildrenRequest setContactsAndChildrenRequest);

  /** Returns all information associated with a user's personal account. */
  UserInformation getPersonalUserInformation(JWTData userData);

  /** Updates a user's information to match the given userInformation object */
  void updatePersonalUserInformation(UserInformation userInformation, JWTData userData);
}
