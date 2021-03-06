package com.codeforcommunity.dataaccess;

import static org.jooq.generated.Tables.CHILDREN;
import static org.jooq.generated.Tables.CONTACTS;
import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;
import static org.jooq.generated.Tables.PF_REQUESTS;
import static org.jooq.generated.Tables.USERS;
import static org.jooq.generated.Tables.VERIFICATION_KEYS;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.auth.AddressData;
import com.codeforcommunity.dto.protected_user.components.Child;
import com.codeforcommunity.dto.protected_user.components.Contact;
import com.codeforcommunity.exceptions.UserDoesNotExistException;
import com.codeforcommunity.requester.S3Requester;
import java.util.List;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.generated.tables.records.ChildrenRecord;
import org.jooq.generated.tables.records.ContactsRecord;
import org.jooq.generated.tables.records.UsersRecord;

public class UserInformationDatabaseOperations {

  private final DSLContext db;

  public UserInformationDatabaseOperations(DSLContext db) {
    this.db = db;
  }

  /** Delete a user and all related tables. */
  public void deleteUserRelatedTables(int userId) {
    db.deleteFrom(EVENT_REGISTRATIONS).where(EVENT_REGISTRATIONS.USER_ID.eq(userId)).execute();

    db.deleteFrom(VERIFICATION_KEYS).where(VERIFICATION_KEYS.USER_ID.eq(userId)).execute();

    db.deleteFrom(PF_REQUESTS).where(PF_REQUESTS.USER_ID.eq(userId)).execute();

    db.deleteFrom(CHILDREN).where(CHILDREN.USER_ID.eq(userId)).execute();

    db.deleteFrom(CONTACTS).where(CONTACTS.USER_ID.eq(userId)).execute();

    db.deleteFrom(USERS).where(USERS.ID.eq(userId)).execute();
  }

  /**
   * Given a contact dto and a JWTData object, update the user's main contact record to reflect the
   * dto data and store in the database.
   */
  public void updateMainContact(Contact newContactData, JWTData userData) {
    ContactsRecord mainContact =
        db.selectFrom(CONTACTS)
            .where(CONTACTS.USER_ID.eq(userData.getUserId()).and(CONTACTS.IS_MAIN_CONTACT))
            .fetchOne();

    if (mainContact == null) {
      throw new UserDoesNotExistException(userData.getUserId());
    }

    String filename = "profile-" + UUID.randomUUID();
    String publicImageUrl =
        S3Requester.validateUploadImageToS3LucyEvents(filename, newContactData.getProfilePicture());

    newContactData.setProfilePicture(publicImageUrl); // Actually setting Image URL

    newContactData.setShouldSendEmails(true);
    updateStoreContactRecord(mainContact, newContactData);

    UsersRecord usersRecord =
        db.selectFrom(USERS).where(USERS.ID.eq(userData.getUserId())).fetchOne();

    usersRecord.setEmail(newContactData.getEmail());

    usersRecord.store();
  }

  /**
   * Given a list of Contact dtos and a JWTData object, store all contacts as new records in the
   * database.
   */
  public void addAdditionalContacts(List<Contact> additionalContacts, JWTData userData) {
    for (Contact contact : additionalContacts) {
      ContactsRecord contactsRecord = db.newRecord(CONTACTS);

      String filename = "profile-" + UUID.randomUUID();
      String publicImageUrl =
          S3Requester.validateUploadImageToS3LucyEvents(filename, contact.getProfilePicture());
      contact.setProfilePicture(publicImageUrl); // Actually setting Image URL

      contactsRecord.setUserId(userData.getUserId());
      updateStoreContactRecord(contactsRecord, contact);
    }
  }

  /**
   * Given a list of Child dtos and a JWTData object, store all children as new records in the
   * database.
   */
  public void addChildren(List<Child> children, JWTData userData) {
    for (Child child : children) {
      ChildrenRecord childrenRecord = db.newRecord(CHILDREN);

      String filename = "profile-" + UUID.randomUUID();
      String publicImageUrl =
          S3Requester.validateUploadImageToS3LucyEvents(filename, child.getProfilePicture());
      child.setProfilePicture(publicImageUrl); // Actually setting Image URL

      childrenRecord.setUserId(userData.getUserId());
      updateStoreChildRecord(childrenRecord, child);
    }
  }

  /** Update and store a users record location data to reflect an AddressData dto. */
  public void updateStoreLocationRecord(UsersRecord usersRecord, AddressData locationData) {
    usersRecord.setAddress(locationData.getAddress());
    usersRecord.setCity(locationData.getCity());
    usersRecord.setState(locationData.getState());
    usersRecord.setZipcode(locationData.getZipCode());

    usersRecord.store();
  }

  /** Update and store a contact record to reflect a contact dto. */
  public void updateStoreContactRecord(ContactsRecord contactsRecord, Contact contactDto) {
    contactsRecord.setFirstName(contactDto.getFirstName());
    contactsRecord.setLastName(contactDto.getLastName());
    contactsRecord.setDateOfBirth(contactDto.getDateOfBirth());
    contactsRecord.setEmail(contactDto.getEmail());
    contactsRecord.setPronouns(contactDto.getPronouns());
    contactsRecord.setAllergies(contactDto.getAllergies());
    contactsRecord.setDiagnosis(contactDto.getDiagnosis());
    contactsRecord.setMedications(contactDto.getMedication());
    contactsRecord.setNotes(contactDto.getNotes());
    contactsRecord.setShouldSendEmails(contactDto.getShouldSendEmails());
    contactsRecord.setPhoneNumber(contactDto.getPhoneNumber());
    contactsRecord.setProfilePicture(contactDto.getProfilePicture());
    contactsRecord.setReferrer(contactDto.getReferrer());

    contactsRecord.store();
  }

  /** Update and store a children record to reflect a child dto. */
  public void updateStoreChildRecord(ChildrenRecord childrenRecord, Child childDto) {
    childrenRecord.setFirstName(childDto.getFirstName());
    childrenRecord.setLastName(childDto.getLastName());
    childrenRecord.setDateOfBirth(childDto.getDateOfBirth());
    childrenRecord.setPronouns(childDto.getPronouns());
    childrenRecord.setSchoolYear(childDto.getSchoolYear());
    childrenRecord.setSchool(childDto.getSchool());
    childrenRecord.setAllergies(childDto.getAllergies());
    childrenRecord.setDiagnosis(childDto.getDiagnosis());
    childrenRecord.setMedications(childDto.getMedications());
    childrenRecord.setNotes(childDto.getNotes());
    childrenRecord.setProfilePicture(childDto.getProfilePicture());

    childrenRecord.store();
  }
}
