package com.codeforcommunity.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.auth.Passwords;
import com.codeforcommunity.dto.protected_user.SetContactsAndChildrenRequest;
import com.codeforcommunity.dto.protected_user.components.Child;
import com.codeforcommunity.dto.protected_user.components.Contact;
import com.codeforcommunity.dto.user.ChangePasswordRequest;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.EventDoesNotExistException;
import com.codeforcommunity.exceptions.UserDoesNotExistException;
import com.codeforcommunity.exceptions.WrongPasswordException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.annotation.Contract;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.ContactsRecord;
import org.jooq.generated.tables.records.EventRegistrationsRecord;
import org.jooq.generated.tables.records.PfRequestsRecord;
import org.jooq.generated.tables.records.UsersRecord;
import org.jooq.generated.tables.records.VerificationKeysRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Contains tests for ProtectedUserProcessorImpl.java in service
public class ProtectedUserProcessorImplTest {
  private JooqMock myJooqMock;
  private ProtectedUserProcessorImpl myProtectedUserProcessorImpl;

  // make examples for Contacts
  private final Contact CONTACT_EXAMPLE_1 = new Contact(
      "Brandon",
      "Liang",
      // January 1, 1970 00:00 UTC
      new Date(0),
      "brandon@example.com",
      "555-555-5555",
      "peanuts",
      "some illness",
      "claritin",
      "none",
      "he/him/his",
      true);

  private final Contact CONTACT_EXAMPLE_2 = new Contact(
      "Conner",
      "Nilsen",
      // May 1, 1990 00:00 UTC
      new Date(641520000),
      "conner@example.com",
      "123-456-7890",
      "none",
      null,
      null,
      "helped me a lot with writing these tests :)",
      "he/him/his",
      false);

  private final Contact CONTACT_EXAMPLE_3 = new Contact(
      "Ada",
      "Lovelace",
      null,
      "ada@example.com",
      null,
      null,
      "Uterine cancer",
      null,
      "analytical engine",
      "she/her/hers",
      false);

  private final Child CHILD_EXAMPLE_1 = new Child(
      "Kazuto",
      "Kirigaya",
      // October 7, 2008 @ 7:00:00 am UTC
      new Date(1223362800),
      "he/him/his",
      "7th Grade",
      "SAO Survivor School",
      "none",
      "brain damage",
      "IV fluid",
      "do not take his helmet off");

  private final Child CHILD_EXAMPLE_2 = new Child(
      "Chad",
      null,
      null,
      "he/him/his",
      "4th Grade",
      null,
      null,
      "type-2 diabetes",
      "Metaglip",
      null);

  // set up all the mocks
  @BeforeEach
  public void setup() {
    this.myJooqMock = new JooqMock();
    this.myProtectedUserProcessorImpl = new ProtectedUserProcessorImpl(myJooqMock.getContext());
  }

  // test properly deleting a user
  @Test
  public void testDeleteUser() {
    JWTData myUser = new JWTData(0, PrivilegeLevel.ADMIN);

    // mock four associated tables with the user
    EventRegistrationsRecord erRecord = new EventRegistrationsRecord();
    erRecord.setUserId(0);
    VerificationKeysRecord vkRecord = new VerificationKeysRecord();
    vkRecord.setUserId(0);
    PfRequestsRecord pfRecord = new PfRequestsRecord();
    pfRecord.setUserId(0);
    UsersRecord usersRecord = new UsersRecord();
    usersRecord.setId(0);

    myJooqMock.addReturn("SELECT", erRecord);
    myJooqMock.addReturn("SELECT", vkRecord);
    myJooqMock.addReturn("SELECT", pfRecord);
    myJooqMock.addReturn("SELECT", usersRecord);
    myJooqMock.addReturn("DELETE", erRecord);
    myJooqMock.addReturn("DELETE", vkRecord);
    myJooqMock.addReturn("DELETE", pfRecord);
    myJooqMock.addReturn("DELETE", usersRecord);

    myProtectedUserProcessorImpl.deleteUser(myUser);
    Object bindings = myJooqMock.getSqlStrings();

    // TODO: add a feature in JooqMock to help with executeAsync()
    fail("TODO!!!");
  }

  // test changing the password if the user is nulll
  @Test
  public void testChangePassword1() {
    JWTData myUser = new JWTData(0, PrivilegeLevel.GP);
    ChangePasswordRequest req = new ChangePasswordRequest("oldpasswd", "newpasswd");

    myJooqMock.addEmptyReturn("SELECT");

    try {
      myProtectedUserProcessorImpl.changePassword(myUser, req);
      fail();
    } catch (UserDoesNotExistException e) {
      assertEquals("id = 0", e.getIdentifierMessage());
    }
  }

  // test changing the password if the user gives the wrong current password
  @Test
  public void testChangePassword2() {
    JWTData myUser = new JWTData(0, PrivilegeLevel.GP);

    // mock the user in the DB
    UsersRecord myUsersRecord = new UsersRecord();
    myUsersRecord.setId(0);
    myUsersRecord.setPassHash(Passwords.createHash("currentpasswd"));
    myJooqMock.addReturn("SELECT", myUsersRecord);

    ChangePasswordRequest req = new ChangePasswordRequest("oldpasswd", "newpasswd");

    try {
      myProtectedUserProcessorImpl.changePassword(myUser, req);
      fail();
    } catch (WrongPasswordException e) {
      // we're good
    }
  }

  // test changing the password correctly
  @Test
  public void testChangePassword3() {
    JWTData myUser = new JWTData(0, PrivilegeLevel.GP);

    // mock the user in the DB
    UsersRecord myUsersRecord = new UsersRecord();
    myUsersRecord.setId(0);
    myUsersRecord.setPassHash(Passwords.createHash("currentpasswd"));
    myJooqMock.addReturn("SELECT", myUsersRecord);
    myJooqMock.addReturn("UPDATE", myUsersRecord);

    ChangePasswordRequest req = new ChangePasswordRequest("currentpasswd", "newpasswd");

    myProtectedUserProcessorImpl.changePassword(myUser, req);

    List<Object[]> updateBindings = myJooqMock.getSqlBindings().get("UPDATE");

    assertEquals(1, updateBindings.size());
  }

  // setting contacts and children fails if there's no main contact
  @Test
  public void testSetContactsAndChildren1() {
    JWTData myUser = new JWTData(0, PrivilegeLevel.GP);
    SetContactsAndChildrenRequest req = new SetContactsAndChildrenRequest(
        null,
        null,
        null);

    myJooqMock.addEmptyReturn("SELECT");

    try {
      myProtectedUserProcessorImpl.setContactsAndChildren(myUser, req);
      fail();
    } catch (UserDoesNotExistException e) {
      assertEquals("id = 0", e.getIdentifierMessage());
    }
  }

  // setting contacts and children responds correctly to non-null and non-empty children
  @Test
  public void testSetContactsAndChildren2() {
    JWTData myUser = new JWTData(0, PrivilegeLevel.GP);

    List<Child> children = new ArrayList<>();
    children.add(CHILD_EXAMPLE_1);
    children.add(CHILD_EXAMPLE_2);

    SetContactsAndChildrenRequest req = new SetContactsAndChildrenRequest(
         CONTACT_EXAMPLE_1,
        null,
        children);

    ContactsRecord myContact = myJooqMock.getContext().newRecord(Tables.CONTACTS);
    myContact.setUserId(0);
    myContact.setIsMainContact(true);
    myJooqMock.addReturn("SELECT", myContact);
    myJooqMock.addReturn("INSERT", myContact);

    myProtectedUserProcessorImpl.setContactsAndChildren(myUser, req);

    List<Object[]> insertBindings = myJooqMock.getSqlBindings().get("INSERT");

    assertEquals(4, insertBindings.size());
    assertEquals("Brandon", insertBindings.get(0)[1]);
    assertEquals("brandon@example.com", insertBindings.get(1)[0]);
    assertEquals("Kazuto", insertBindings.get(2)[1]);
    assertEquals("Chad", insertBindings.get(3)[1]);
  }

  // setting contacts and children responds correctly to non-null and non-empty additional contacts
  @Test
  public void testSetContactsAndChildren3() {
    JWTData myUser = new JWTData(0, PrivilegeLevel.GP);

    List<Contact> additionalContacts = new ArrayList<>();
    additionalContacts.add(CONTACT_EXAMPLE_2);
    additionalContacts.add(CONTACT_EXAMPLE_3);

    SetContactsAndChildrenRequest req = new SetContactsAndChildrenRequest(
        CONTACT_EXAMPLE_1,
        additionalContacts,
        null);

    ContactsRecord myContact = myJooqMock.getContext().newRecord(Tables.CONTACTS);
    myContact.setUserId(0);
    myContact.setIsMainContact(true);
    myJooqMock.addReturn("SELECT", myContact);
    myJooqMock.addReturn("INSERT", myContact);

    myProtectedUserProcessorImpl.setContactsAndChildren(myUser, req);

    List<Object[]> insertBindings = myJooqMock.getSqlBindings().get("INSERT");

    assertEquals(4, insertBindings.size());
    assertEquals("Brandon", insertBindings.get(0)[1]);
    assertEquals("brandon@example.com", insertBindings.get(1)[0]);
    assertEquals("Conner", insertBindings.get(2)[3]);
    assertEquals("Ada", insertBindings.get(3)[3]);
  }

  // setting contacts and children responds correctly to non-null and non-empty children and additional contacts
  @Test
  public void testSetContactsAndChildren4() {
    JWTData myUser = new JWTData(0, PrivilegeLevel.GP);

    List<Contact> additionalContacts = new ArrayList<>();
    additionalContacts.add(CONTACT_EXAMPLE_2);
    additionalContacts.add(CONTACT_EXAMPLE_3);

    List<Child> children = new ArrayList<>();
    children.add(CHILD_EXAMPLE_1);
    children.add(CHILD_EXAMPLE_2);

    SetContactsAndChildrenRequest req = new SetContactsAndChildrenRequest(
        CONTACT_EXAMPLE_1,
        additionalContacts,
        children);

    ContactsRecord myContact = myJooqMock.getContext().newRecord(Tables.CONTACTS);
    myContact.setUserId(0);
    myContact.setIsMainContact(true);
    myJooqMock.addReturn("SELECT", myContact);
    myJooqMock.addReturn("INSERT", myContact);

    myProtectedUserProcessorImpl.setContactsAndChildren(myUser, req);

    List<Object[]> insertBindings = myJooqMock.getSqlBindings().get("INSERT");

    assertEquals(6, insertBindings.size());
    assertEquals("Brandon", insertBindings.get(0)[1]);
    assertEquals("brandon@example.com", insertBindings.get(1)[0]);
    assertEquals("Kazuto", insertBindings.get(2)[1]);
    assertEquals("Chad", insertBindings.get(3)[1]);
    assertEquals("Conner", insertBindings.get(4)[3]);
    assertEquals("Ada", insertBindings.get(5)[3]);
  }
}
