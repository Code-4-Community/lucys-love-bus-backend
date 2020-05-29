package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.*;

import com.codeforcommunity.api.IProtectedUserProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.auth.Passwords;
import com.codeforcommunity.dataaccess.AuthDatabaseOperations;
import com.codeforcommunity.dataaccess.UserInformationDatabaseOperations;
import com.codeforcommunity.dto.auth.AddressData;
import com.codeforcommunity.dto.protected_user.ChangeEmailRequest;
import com.codeforcommunity.dto.protected_user.SetContactsAndChildrenRequest;
import com.codeforcommunity.dto.protected_user.UserInformation;
import com.codeforcommunity.dto.protected_user.components.Child;
import com.codeforcommunity.dto.protected_user.components.Contact;
import com.codeforcommunity.dto.user.ChangePasswordRequest;
import com.codeforcommunity.exceptions.EmailAlreadyInUseException;
import com.codeforcommunity.exceptions.TableNotMatchingUserException;
import com.codeforcommunity.exceptions.UserDoesNotExistException;
import com.codeforcommunity.exceptions.WrongPasswordException;
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
  private final AuthDatabaseOperations authDatabaseOperations;
  private final UserInformationDatabaseOperations userInformationDbOps;

  public ProtectedUserProcessorImpl(DSLContext db) {
    this.db = db;
    this.authDatabaseOperations = new AuthDatabaseOperations(db);
    this.userInformationDbOps = new UserInformationDatabaseOperations(db);
  }

  @Override
  public void deleteUser(JWTData userData) {
    int userId = userData.getUserId();
    userInformationDbOps.deleteUserRelatedTables(userId);
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
    } else {
      throw new WrongPasswordException();
    }
  }

  @Override
  public void changePrimaryEmail(JWTData userData, ChangeEmailRequest changeEmailRequest) {
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

      user.store();
    } else {
      throw new WrongPasswordException();
    }
  }

  @Override
  public void setContactsAndChildren(
      JWTData userData, SetContactsAndChildrenRequest setContactsAndChildrenRequest) {

    userInformationDbOps.updateMainContact(
        setContactsAndChildrenRequest.getMainContact(), userData);

    if (setContactsAndChildrenRequest.getChildren() != null
        && !setContactsAndChildrenRequest.getChildren().isEmpty()) {
      userInformationDbOps.addChildren(setContactsAndChildrenRequest.getChildren(), userData);
    }

    if (setContactsAndChildrenRequest.getAdditionalContacts() != null
        && !setContactsAndChildrenRequest.getAdditionalContacts().isEmpty()) {
      userInformationDbOps.addAdditionalContacts(
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
  public void updatePersonalUserInformation(UserInformation userInformation, JWTData userData) {
    userInformationDbOps.updateMainContact(userInformation.getMainContact(), userData);

    AddressData locationData = userInformation.getLocation();
    UsersRecord usersRecord =
        db.selectFrom(USERS).where(USERS.ID.eq(userData.getUserId())).fetchOne();
    userInformationDbOps.updateStoreLocationRecord(usersRecord, locationData);

    List<Contact> additionalContacts = userInformation.getAdditionalContacts();
    updateContacts(additionalContacts, userData);

    List<Child> children = userInformation.getChildren();
    updateChildren(children, userData);
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
            userInformationDbOps.updateStoreContactRecord(newContact, contact);
          } else {
            ContactsRecord updatableContact = currentContacts.remove(contact.getId());
            if (updatableContact == null) {
              throw new TableNotMatchingUserException("Contact", contact.getId());
            }
            userInformationDbOps.updateStoreContactRecord(updatableContact, contact);
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
            userInformationDbOps.updateStoreChildRecord(newChild, child);
          } else {
            ChildrenRecord updatableChild = currentChildren.remove(child.getId());
            if (updatableChild == null) {
              throw new TableNotMatchingUserException("Child", child.getId());
            }
            userInformationDbOps.updateStoreChildRecord(updatableChild, child);
          }
        });
    currentChildren.values().forEach(UpdatableRecordImpl::delete);
  }
}
