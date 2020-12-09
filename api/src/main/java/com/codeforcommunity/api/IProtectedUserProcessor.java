package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
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
  
  /** Get the user's data for use in the site. */
  UserDataResponse getUserData(JWTData userData);

  /** Change the user's email to the provided one */
  void changeEmail(JWTData userData, ChangeEmailRequest changeEmailRequest);
}
