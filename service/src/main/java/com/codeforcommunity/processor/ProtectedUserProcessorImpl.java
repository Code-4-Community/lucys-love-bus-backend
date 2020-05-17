package com.codeforcommunity.processor;

import com.codeforcommunity.api.IProtectedUserProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.auth.Passwords;
import com.codeforcommunity.dto.protected_user.SetContactsAndChildrenRequest;
import com.codeforcommunity.dto.protected_user.components.Child;
import com.codeforcommunity.dto.protected_user.components.Contact;
import com.codeforcommunity.dto.user.ChangePasswordRequest;
import com.codeforcommunity.exceptions.UserDoesNotExistException;
import com.codeforcommunity.exceptions.WrongPasswordException;
import org.jooq.DSLContext;
import org.jooq.generated.tables.records.ChildrenRecord;
import org.jooq.generated.tables.records.ContactsRecord;
import org.jooq.generated.tables.records.UsersRecord;

import java.util.List;

import static org.jooq.generated.Tables.*;

public class ProtectedUserProcessorImpl implements IProtectedUserProcessor {

  private final DSLContext db;

  public ProtectedUserProcessorImpl(DSLContext db) {
    this.db = db;
  }

  @Override
  public void deleteUser(JWTData userData) {
    int userId = userData.getUserId();

    db.deleteFrom(EVENT_REGISTRATIONS)
        .where(EVENT_REGISTRATIONS.USER_ID.eq(userId))
        .executeAsync();

    db.deleteFrom(VERIFICATION_KEYS)
        .where(VERIFICATION_KEYS.USER_ID.eq(userId))
        .executeAsync();

    db.deleteFrom(PF_REQUESTS)
        .where(PF_REQUESTS.USER_ID.eq(userId))
        .executeAsync();

    db.deleteFrom(USERS)
        .where(USERS.ID.eq(userId))
        .executeAsync();
  }

  @Override
  public void changePassword(JWTData userData, ChangePasswordRequest changePasswordRequest) {
    UsersRecord user = db.selectFrom(USERS)
        .where(USERS.ID.eq(userData.getUserId()))
        .fetchOne();

    if (user == null) {
      throw new UserDoesNotExistException(userData.getUserId());
    }

    if (Passwords.isExpectedPassword(changePasswordRequest.getCurrentPassword(), user.getPassHash())) {
      user.setPassHash(Passwords.createHash(changePasswordRequest.getNewPassword()));
      user.store();
    } else {
      throw new WrongPasswordException();
    }
  }

  @Override
  public void setContactsAndChildren(JWTData userData, SetContactsAndChildrenRequest setContactsAndChildrenRequest) {

    updateMainContact(userData, setContactsAndChildrenRequest.getMainContact());

    if (setContactsAndChildrenRequest.getChildren() != null &&
            !setContactsAndChildrenRequest.getChildren().isEmpty()) {
      addChildren(setContactsAndChildrenRequest.getChildren(), userData);
    }

    if (setContactsAndChildrenRequest.getAdditionalContacts() != null &&
            !setContactsAndChildrenRequest.getAdditionalContacts().isEmpty()) {
      addAdditionalContacts(setContactsAndChildrenRequest.getAdditionalContacts(), userData);
    }

  }
  
  private void updateMainContact(JWTData userData, Contact newContactData) {

    ContactsRecord mainContact = db.selectFrom(CONTACTS)
            .where(CONTACTS.USER_ID.eq(userData.getUserId())
                    .and(CONTACTS.IS_MAIN_CONTACT))
            .fetchOne();

    if (mainContact == null) {
      return;
    }

    mainContact.setFirstName(newContactData.getFirstName());
    mainContact.setLastName(newContactData.getLastName());
    mainContact.setDateOfBirth(newContactData.getDob());
    mainContact.setEmail(newContactData.getEmail());
    mainContact.setPhoneNumber(newContactData.getPhoneNumber());
    mainContact.setAllergies(newContactData.getAllergies());
    mainContact.setDiagnosis(newContactData.getDiagnosis());
    mainContact.setMedications(newContactData.getMedications());
    mainContact.setNotes(newContactData.getNotes());
    mainContact.setPronouns(newContactData.getPronouns());

    mainContact.store();

    UsersRecord usersRecord = db.selectFrom(USERS)
            .where(USERS.ID.eq(userData.getUserId()))
                    .fetchOne();

    usersRecord.setEmail(newContactData.getEmail());

    usersRecord.store();
  }

  private void addChildren(List<Child> children, JWTData userData) {
    
    for (Child c : children) {

      ChildrenRecord childrenRecord = db.newRecord(CHILDREN);
      childrenRecord.setUserId(userData.getUserId());
      childrenRecord.setFirstName(c.getFirstName());
      childrenRecord.setLastName(c.getLastName());
      childrenRecord.setDateOfBirth(c.getDob());
      childrenRecord.setPronouns(c.getPronouns());
      childrenRecord.setSchoolYear(c.getSchoolYear());
      childrenRecord.setSchool(c.getSchool());
      childrenRecord.setAllergies(c.getAllergies());
      childrenRecord.setDiagnosis(c.getDiagnosis());
      childrenRecord.setMedications(c.getMedications());
      childrenRecord.setNotes(c.getNotes());
      
      childrenRecord.store();
    }
  }

  private void addAdditionalContacts(List<Contact> additionalContacts, JWTData userData) {
    
    for (Contact c : additionalContacts) {

      ContactsRecord contactsRecord = db.newRecord(CONTACTS);
      contactsRecord.setUserId(userData.getUserId());
      contactsRecord.setFirstName(c.getFirstName());
      contactsRecord.setLastName(c.getLastName());
      contactsRecord.setDateOfBirth(c.getDob());
      contactsRecord.setEmail(c.getEmail());
      contactsRecord.setPronouns(c.getPronouns());
      contactsRecord.setAllergies(c.getAllergies());
      contactsRecord.setDiagnosis(c.getDiagnosis());
      contactsRecord.setMedications(c.getMedications());
      contactsRecord.setNotes(c.getNotes());
      contactsRecord.setShouldSendEmails(c.getShouldSendEmail());

      contactsRecord.store();
    }
  } 
  
}
