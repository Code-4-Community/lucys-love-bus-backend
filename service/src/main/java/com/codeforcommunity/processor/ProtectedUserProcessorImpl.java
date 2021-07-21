package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.*;

import com.codeforcommunity.api.IProtectedUserProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.auth.Passwords;
import com.codeforcommunity.dataaccess.AuthDatabaseOperations;
import com.codeforcommunity.dataaccess.UserInformationDatabaseOperations;
import com.codeforcommunity.dto.auth.AddressData;
import com.codeforcommunity.dto.protected_user.GetAllUserInfoResponse;
import com.codeforcommunity.dto.protected_user.SetContactsAndChildrenRequest;
import com.codeforcommunity.dto.protected_user.UserInformation;
import com.codeforcommunity.dto.protected_user.components.Child;
import com.codeforcommunity.dto.protected_user.components.Contact;
import com.codeforcommunity.dto.protected_user.components.UserSummary;
import com.codeforcommunity.dto.user.ChangeEmailRequest;
import com.codeforcommunity.dto.user.ChangePasswordRequest;
import com.codeforcommunity.dto.user.UserDataResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.*;
import com.codeforcommunity.requester.Emailer;
import java.util.List;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.Users;
import org.jooq.generated.tables.records.ChildrenRecord;
import org.jooq.generated.tables.records.ContactsRecord;
import org.jooq.generated.tables.records.UsersRecord;
import org.jooq.impl.UpdatableRecordImpl;

public class ProtectedUserProcessorImpl implements IProtectedUserProcessor {

  private final DSLContext db;
  private final Emailer emailer;
  private final AuthDatabaseOperations authDatabaseOperations;
  private final UserInformationDatabaseOperations userInformationDatabaseOperations;

  public ProtectedUserProcessorImpl(DSLContext db, Emailer emailer) {
    this.db = db;
    this.emailer = emailer;
    this.authDatabaseOperations = new AuthDatabaseOperations(db);
    this.userInformationDatabaseOperations = new UserInformationDatabaseOperations(db);
  }

  @Override
  public void deleteUser(JWTData userData) {
    int userId = userData.getUserId();
    emailer.sendEmailToAllContacts(userId, emailer::sendAccountDeactivated);
    this.userInformationDatabaseOperations.deleteUserRelatedTables(userId);
  }

  @Override
  public void changePassword(JWTData userData, ChangePasswordRequest changePasswordRequest) {
    UsersRecord user = db.selectFrom(USERS).where(USERS.ID.eq(userData.getUserId())).fetchOne();

    if (user == null) {
      throw new UserDoesNotExistException(userData.getUserId());
    }

    if (Passwords.isExpectedPassword(
        changePasswordRequest.getCurrentPassword(), user.getPassHash())) {
      user.setPassHash(Passwords.createHash(changePasswordRequest.getNewPassword()));
      user.store();

      emailer.sendEmailToMainContact(user.getId(), emailer::sendPasswordChangeConfirmationEmail);
    } else {
      throw new WrongPasswordException();
    }
  }

  @Override
  public UserDataResponse getUserData(JWTData userData) {
    UsersRecord user = db.selectFrom(USERS).where(USERS.ID.eq(userData.getUserId())).fetchOne();

    if (user == null) {
      throw new UserDoesNotExistException(userData.getUserId());
    }

    throw new RuntimeException("Not implemented");
    //    return new UserDataResponse(user.getFirstName(), user.getLastName(), user.getEmail());
  }

  @Override
  public void setContactsAndChildren(
      JWTData userData, SetContactsAndChildrenRequest setContactsAndChildrenRequest) {

    userInformationDatabaseOperations.updateMainContact(
        setContactsAndChildrenRequest.getMainContact(), userData);

    if (setContactsAndChildrenRequest.getChildren() != null
        && !setContactsAndChildrenRequest.getChildren().isEmpty()) {
      userInformationDatabaseOperations.addChildren(
          setContactsAndChildrenRequest.getChildren(), userData);
    }

    if (setContactsAndChildrenRequest.getAdditionalContacts() != null
        && !setContactsAndChildrenRequest.getAdditionalContacts().isEmpty()) {
      userInformationDatabaseOperations.addAdditionalContacts(
          setContactsAndChildrenRequest.getAdditionalContacts(), userData);
    }
  }

  @Override
  public UserInformation getPersonalUserInformation(JWTData userData) {
    Users user =
        db.selectFrom(USERS).where(USERS.ID.eq(userData.getUserId())).fetchOneInto(Users.class);

    if (user == null) {
      throw new UserDoesNotExistException(userData.getUserId());
    }

    return authDatabaseOperations.getUserInformation(user);
  }

  @Override
  public UserInformation getPersonalUserInformation(int userId, JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }
    Users user = db.selectFrom(USERS).where(USERS.ID.eq(userId)).fetchOneInto(Users.class);

    if (user == null) {
      throw new UserDoesNotExistException(userId);
    }

    return authDatabaseOperations.getUserInformation(user);
  }

  @Override
  public GetAllUserInfoResponse getAllUserInformation(JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }
    return new GetAllUserInfoResponse(
        db.select(
                CONTACTS.FIRST_NAME,
                CONTACTS.LAST_NAME,
                CONTACTS.EMAIL,
                CONTACTS.USER_ID,
                USERS.PRIVILEGE_LEVEL,
                CONTACTS.PHONE_NUMBER,
                CONTACTS.PROFILE_PICTURE,
                USERS.PHOTO_RELEASE)
            .from(USERS)
            .join(CONTACTS)
            .on(USERS.ID.eq(CONTACTS.USER_ID))
            .where(CONTACTS.IS_MAIN_CONTACT.isTrue())
            .fetchInto(UserSummary.class));
  }

  @Override
  public void updatePersonalUserInformation(UserInformation userInformation, JWTData userData) {
    userInformationDatabaseOperations.updateMainContact(userInformation.getMainContact(), userData);

    AddressData locationData = userInformation.getLocation();
    UsersRecord usersRecord =
        db.selectFrom(USERS).where(USERS.ID.eq(userData.getUserId())).fetchOne();
    userInformationDatabaseOperations.updateStoreLocationRecord(usersRecord, locationData);

    List<Contact> additionalContacts = userInformation.getAdditionalContacts();
    updateContacts(additionalContacts, userData);

    List<Child> children = userInformation.getChildren();
    updateChildren(children, userData);
  }

  @Override
  public void changeEmail(JWTData userData, ChangeEmailRequest changeEmailRequest) {
    UsersRecord user = db.selectFrom(USERS).where(USERS.ID.eq(userData.getUserId())).fetchOne();
    if (user == null) {
      throw new UserDoesNotExistException(userData.getUserId());
    }

    if (Passwords.isExpectedPassword(changeEmailRequest.getPassword(), user.getPassHash())) {
      if (db.fetchExists(USERS, USERS.EMAIL.eq(changeEmailRequest.getNewEmail()))) {
        throw new EmailAlreadyInUseException(changeEmailRequest.getNewEmail());
      }

      user.setEmail(changeEmailRequest.getNewEmail());

      ContactsRecord mainContact =
          db.selectFrom(CONTACTS)
              .where(CONTACTS.ID.eq(user.getId()))
              .and(CONTACTS.IS_MAIN_CONTACT.isTrue())
              .fetchOne();
      if (mainContact != null) {
        mainContact.setEmail(changeEmailRequest.getNewEmail());
        mainContact.store();
      }

      emailer.sendEmailToAllContacts(
          userData.getUserId(),
          (e, n) ->
              emailer.sendEmailChangeConfirmationEmail(e, n, changeEmailRequest.getNewEmail()));
      user.store();
      emailer.sendEmailToMainContact(
          userData.getUserId(),
          (e, n) ->
              emailer.sendEmailChangeConfirmationEmail(e, n, changeEmailRequest.getNewEmail()));
    } else {
      throw new WrongPasswordException();
    }
  }

  /**
   * Based on a list of new contact dtos update, insert, or remove the given user's contact table to
   * reflect the dto list.
   */
  private void updateContacts(List<Contact> contacts, JWTData userData) {
    Map<Integer, ContactsRecord> currentContacts =
        db.selectFrom(CONTACTS)
            .where(CONTACTS.USER_ID.eq(userData.getUserId()))
            .and(CONTACTS.IS_MAIN_CONTACT.isFalse())
            .fetchMap(CONTACTS.ID);
    contacts.forEach(
        contact -> {
          if (contact.getId() == null) {
            ContactsRecord newContact = db.newRecord(CONTACTS);
            newContact.setUserId(userData.getUserId());
            userInformationDatabaseOperations.updateStoreContactRecord(newContact, contact);
          } else {
            ContactsRecord updatableContact = currentContacts.remove(contact.getId());
            if (updatableContact == null) {
              throw new TableNotMatchingUserException("Contact", contact.getId());
            }
            userInformationDatabaseOperations.updateStoreContactRecord(updatableContact, contact);
          }
        });
    currentContacts.values().forEach(UpdatableRecordImpl::delete);
  }

  /**
   * Based on a list of new children dtos update, insert, or remove the given user's children table
   * to reflect the dto list.
   */
  private void updateChildren(List<Child> children, JWTData userData) {
    Map<Integer, ChildrenRecord> currentChildren =
        db.selectFrom(CHILDREN)
            .where(CHILDREN.USER_ID.eq(userData.getUserId()))
            .fetchMap(CHILDREN.ID);
    children.forEach(
        child -> {
          if (child.getId() == null) {
            ChildrenRecord newChild = db.newRecord(CHILDREN);
            newChild.setUserId(userData.getUserId());
            userInformationDatabaseOperations.updateStoreChildRecord(newChild, child);
          } else {
            ChildrenRecord updatableChild = currentChildren.remove(child.getId());
            if (updatableChild == null) {
              throw new TableNotMatchingUserException("Child", child.getId());
            }
            userInformationDatabaseOperations.updateStoreChildRecord(updatableChild, child);
          }
        });
    currentChildren.values().forEach(UpdatableRecordImpl::delete);
  }
}
