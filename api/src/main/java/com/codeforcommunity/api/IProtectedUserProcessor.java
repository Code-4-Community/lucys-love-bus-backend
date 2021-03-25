package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.protected_user.SetContactsAndChildrenRequest;
import com.codeforcommunity.dto.protected_user.UserInformation;
import com.codeforcommunity.dto.user.ChangeEmailRequest;
import com.codeforcommunity.dto.user.ChangePasswordRequest;
import com.codeforcommunity.dto.user.UserDataResponse;

public interface IProtectedUserProcessor {

  /** Deletes the given user from the database. Does NOT invalidate the user's JWTs */
  void deleteUser(JWTData userData);

  /**
   * If the given current password matches the user's current password, update the user's password
   * to the new password value.
   */
  void changePassword(JWTData userData, ChangePasswordRequest changePasswordRequest);

  /** Set the contact and children tables associated with the calling user's account. */
  void setContactsAndChildren(
      JWTData userData, SetContactsAndChildrenRequest setContactsAndChildrenRequest);

  /** Returns all information associated with a user's personal account. */
  UserInformation getPersonalUserInformation(JWTData userData);

  /** Admin route that returns all information associated with a given user's personal account. */
  UserInformation getPersonalUserInformation(int userId, JWTData userData);

  /** Updates a user's information to match the given userInformation object */
  void updatePersonalUserInformation(UserInformation userInformation, JWTData userData);

  /** Get the user's data for use in the site. */
  UserDataResponse getUserData(JWTData userData);

  /** Change the user's email to the provided one */
  void changeEmail(JWTData userData, ChangeEmailRequest changeEmailRequest);
}
