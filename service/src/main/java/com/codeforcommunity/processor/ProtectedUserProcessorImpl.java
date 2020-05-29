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
import com.codeforcommunity.exceptions.UserDoesNotExistException;
import com.codeforcommunity.exceptions.WrongPasswordException;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.Users;
import org.jooq.generated.tables.records.ChildrenRecord;
import org.jooq.generated.tables.records.ContactsRecord;
import org.jooq.generated.tables.records.UsersRecord;

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
    List<Contact> newContacts =
        additionalContacts.stream()
            .filter(contact -> contact.getId() == null)
            .collect(Collectors.toList());
    List<Contact> updatableContacts =
        additionalContacts.stream()
            .filter(contact -> contact.getId() != null)
            .collect(Collectors.toList());

    List<Child> children = userInformation.getChildren();
    List<Child> newChildren =
        children.stream().filter(child -> child.getId() == null).collect(Collectors.toList());
    List<Child> updatableChildren =
        children.stream().filter(child -> child.getId() != null).collect(Collectors.toList());

    userInformationDbOps.addAdditionalContacts(newContacts, userData);
    userInformationDbOps.addChildren(newChildren, userData);

    updatableContacts.forEach(
        contact -> {
          ContactsRecord contactsRecord =
              db.selectFrom(CONTACTS).where(CONTACTS.ID.eq(contact.getId())).fetchOne();

          if (!contactsRecord.getUserId().equals(userData.getUserId())) {
            // Do something
          }

          userInformationDbOps.updateStoreContactRecord(contactsRecord, contact);
        });

    updatableChildren.forEach(
        child -> {
          ChildrenRecord childrenRecord =
              db.selectFrom(CHILDREN).where(CHILDREN.ID.eq(child.getId())).fetchOne();

          if (!childrenRecord.getUserId().equals(userData.getUserId())) {
            // Do something
          }

          userInformationDbOps.updateStoreChildRecord(childrenRecord, child);
        });
  }
}
