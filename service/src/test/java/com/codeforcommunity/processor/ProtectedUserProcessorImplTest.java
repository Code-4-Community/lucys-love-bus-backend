package com.codeforcommunity.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.JooqMock.OperationType;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.auth.Passwords;
import com.codeforcommunity.dto.protected_user.SetContactsAndChildrenRequest;
import com.codeforcommunity.dto.protected_user.components.Child;
import com.codeforcommunity.dto.protected_user.components.Contact;
import com.codeforcommunity.dto.user.ChangePasswordRequest;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.UserDoesNotExistException;
import com.codeforcommunity.exceptions.WrongPasswordException;
import com.codeforcommunity.requester.Emailer;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.ChildrenRecord;
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
  private Emailer mockEmailer;

  // make examples for Contacts
  private final Contact CONTACT_EXAMPLE_1 =
      new Contact(
          0,
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
          true,
          "Brandon's referrer",
          null);

  private final Contact CONTACT_EXAMPLE_2 =
      new Contact(
          1,
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
          false,
          "Conner's referrer",
          null);

  private final Contact CONTACT_EXAMPLE_3 =
      new Contact(
          2,
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
          false,
          "Ada's referrer",
          null);

  private final Child CHILD_EXAMPLE_1 =
      new Child(
          0,
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
          "do not take his helmet off",
          null);

  private final Child CHILD_EXAMPLE_2 =
      new Child(
          1,
          "Chad",
          null,
          null,
          "he/him/his",
          "4th Grade",
          null,
          null,
          "type-2 diabetes",
          "Metaglip",
          null,
          null);

  // set up all the mocks
  @BeforeEach
  public void setup() {
    this.myJooqMock = new JooqMock();
    this.mockEmailer = mock(Emailer.class);
    this.myProtectedUserProcessorImpl =
        new ProtectedUserProcessorImpl(myJooqMock.getContext(), this.mockEmailer);
  }

  // test properly deleting a user
  @Test
  public void testDeleteUser() {
    JWTData myUser = new JWTData(1, PrivilegeLevel.ADMIN);

    // mock four associated tables with the user
    EventRegistrationsRecord erRecord =
        myJooqMock.getContext().newRecord(Tables.EVENT_REGISTRATIONS);
    erRecord.setUserId(0);
    erRecord.setId(1);
    VerificationKeysRecord vkRecord = myJooqMock.getContext().newRecord(Tables.VERIFICATION_KEYS);
    vkRecord.setUserId(0);
    vkRecord.setId("2");
    PfRequestsRecord pfRecord = myJooqMock.getContext().newRecord(Tables.PF_REQUESTS);
    pfRecord.setUserId(0);
    pfRecord.setId(3);
    ChildrenRecord childrenRecord = myJooqMock.getContext().newRecord(Tables.CHILDREN);
    childrenRecord.setUserId(0);
    childrenRecord.setId(4);
    ContactsRecord contactsRecord = myJooqMock.getContext().newRecord(Tables.CONTACTS);
    contactsRecord.setUserId(0);
    contactsRecord.setId(5);
    UsersRecord usersRecord = myJooqMock.getContext().newRecord(Tables.USERS);
    usersRecord.setId(0);

    myJooqMock.addReturn(OperationType.DELETE, erRecord);
    myJooqMock.addReturn(OperationType.DELETE, vkRecord);
    myJooqMock.addReturn(OperationType.DELETE, pfRecord);
    myJooqMock.addReturn(OperationType.DELETE, childrenRecord);
    myJooqMock.addReturn(OperationType.DELETE, contactsRecord);
    myJooqMock.addReturn(OperationType.DELETE, usersRecord);

    myProtectedUserProcessorImpl.deleteUser(myUser);
    List<Object[]> bindings = myJooqMock.getSqlOperationBindings().get(OperationType.DELETE);

    assertEquals(6, bindings.size());
    assertEquals(myUser.getUserId(), bindings.get(0)[0]);
    assertEquals(myUser.getUserId(), bindings.get(1)[0]);
    assertEquals(myUser.getUserId(), bindings.get(2)[0]);
    assertEquals(myUser.getUserId(), bindings.get(3)[0]);
    assertEquals(myUser.getUserId(), bindings.get(4)[0]);
    assertEquals(myUser.getUserId(), bindings.get(5)[0]);
  }

  // test changing the password if the user is nulll
  @Test
  public void testChangePassword1() {
    JWTData myUser = new JWTData(0, PrivilegeLevel.STANDARD);
    ChangePasswordRequest req = new ChangePasswordRequest("oldpasswd", "newpasswd");

    myJooqMock.addEmptyReturn(OperationType.SELECT);

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
    JWTData myUser = new JWTData(0, PrivilegeLevel.STANDARD);

    // mock the user in the DB
    UsersRecord myUsersRecord = new UsersRecord();
    myUsersRecord.setId(0);
    myUsersRecord.setPassHash(Passwords.createHash("currentpasswd"));
    myJooqMock.addReturn(OperationType.SELECT, myUsersRecord);

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
    JWTData myUser = new JWTData(0, PrivilegeLevel.STANDARD);

    // mock the user in the DB
    UsersRecord myUsersRecord = new UsersRecord();
    myUsersRecord.setId(0);
    myUsersRecord.setPassHash(Passwords.createHash("currentpasswd"));
    myJooqMock.addReturn(OperationType.SELECT, myUsersRecord);
    myJooqMock.addReturn(OperationType.UPDATE, myUsersRecord);

    ChangePasswordRequest req = new ChangePasswordRequest("currentpasswd", "newpasswd");

    myProtectedUserProcessorImpl.changePassword(myUser, req);

    List<Object[]> updateBindings = myJooqMock.getSqlOperationBindings().get(OperationType.UPDATE);

    assertEquals(1, updateBindings.size());
  }

  // setting contacts and children fails if there's no main contact
  @Test
  public void testSetContactsAndChildren1() {
    JWTData myUser = new JWTData(0, PrivilegeLevel.STANDARD);
    SetContactsAndChildrenRequest req = new SetContactsAndChildrenRequest(null, null, null);

    myJooqMock.addEmptyReturn(OperationType.SELECT);

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
    JWTData myUser = new JWTData(0, PrivilegeLevel.STANDARD);

    List<Child> children = new ArrayList<>();
    children.add(CHILD_EXAMPLE_1);
    children.add(CHILD_EXAMPLE_2);

    SetContactsAndChildrenRequest req =
        new SetContactsAndChildrenRequest(CONTACT_EXAMPLE_1, null, children);

    ContactsRecord myContact = myJooqMock.getContext().newRecord(Tables.CONTACTS);
    myContact.setUserId(0);
    myContact.setIsMainContact(true);
    myJooqMock.addReturn(OperationType.SELECT, myContact);
    myJooqMock.addReturn(OperationType.INSERT, myContact);
    myJooqMock.addReturn(OperationType.UPDATE, myContact);

    myProtectedUserProcessorImpl.setContactsAndChildren(myUser, req);

    List<Object[]> insertBindings = myJooqMock.getSqlOperationBindings().get(OperationType.INSERT);
    List<Object[]> updateBindings = myJooqMock.getSqlOperationBindings().get(OperationType.UPDATE);

    assertEquals(2, insertBindings.size());
    assertEquals(2, updateBindings.size());
    assertEquals("Brandon", updateBindings.get(0)[2]);
    assertEquals("brandon@example.com", updateBindings.get(0)[0]);
    assertEquals("Kazuto", insertBindings.get(0)[2]);
    assertEquals("Chad", insertBindings.get(1)[2]);
  }

  // setting contacts and children responds correctly to non-null and non-empty additional contacts
  @Test
  public void testSetContactsAndChildren3() {
    JWTData myUser = new JWTData(0, PrivilegeLevel.STANDARD);

    List<Contact> additionalContacts = new ArrayList<>();
    additionalContacts.add(CONTACT_EXAMPLE_2);
    additionalContacts.add(CONTACT_EXAMPLE_3);

    SetContactsAndChildrenRequest req =
        new SetContactsAndChildrenRequest(CONTACT_EXAMPLE_1, additionalContacts, null);

    ContactsRecord myContact = myJooqMock.getContext().newRecord(Tables.CONTACTS);
    myContact.setUserId(0);
    myContact.setIsMainContact(true);
    myJooqMock.addReturn(OperationType.SELECT, myContact);
    myJooqMock.addReturn(OperationType.INSERT, myContact);
    myJooqMock.addReturn(OperationType.UPDATE, myContact);

    myProtectedUserProcessorImpl.setContactsAndChildren(myUser, req);

    List<Object[]> insertBindings = myJooqMock.getSqlOperationBindings().get(OperationType.INSERT);
    List<Object[]> updateBindings = myJooqMock.getSqlOperationBindings().get(OperationType.UPDATE);

    assertEquals(2, insertBindings.size());
    assertEquals(2, updateBindings.size());
    assertEquals("Brandon", updateBindings.get(0)[2]);
    assertEquals("brandon@example.com", updateBindings.get(1)[0]);
    assertEquals("Conner", insertBindings.get(0)[4]);
    assertEquals("Ada", insertBindings.get(1)[4]);
  }

  // setting contacts and children responds correctly to non-null and non-empty children and
  // additional contacts
  @Test
  public void testSetContactsAndChildren4() {
    JWTData myUser = new JWTData(0, PrivilegeLevel.STANDARD);

    List<Contact> additionalContacts = new ArrayList<>();
    additionalContacts.add(CONTACT_EXAMPLE_2);
    additionalContacts.add(CONTACT_EXAMPLE_3);

    List<Child> children = new ArrayList<>();
    children.add(CHILD_EXAMPLE_1);
    children.add(CHILD_EXAMPLE_2);

    SetContactsAndChildrenRequest req =
        new SetContactsAndChildrenRequest(CONTACT_EXAMPLE_1, additionalContacts, children);

    ContactsRecord myContact = myJooqMock.getContext().newRecord(Tables.CONTACTS);
    myContact.setUserId(0);
    myContact.setIsMainContact(true);
    myJooqMock.addReturn(OperationType.SELECT, myContact);
    myJooqMock.addReturn(OperationType.INSERT, myContact);
    myJooqMock.addReturn(OperationType.UPDATE, myContact);

    myProtectedUserProcessorImpl.setContactsAndChildren(myUser, req);

    List<Object[]> insertBindings = myJooqMock.getSqlOperationBindings().get(OperationType.INSERT);
    List<Object[]> updateBindings = myJooqMock.getSqlOperationBindings().get(OperationType.UPDATE);

    assertEquals(2, updateBindings.size());
    assertEquals(4, insertBindings.size());
    assertEquals("Kazuto", insertBindings.get(0)[2]);
    assertEquals("Chad", insertBindings.get(1)[2]);
    assertEquals("Conner", insertBindings.get(2)[4]);
    assertEquals("Ada", insertBindings.get(3)[4]);
    assertEquals("Brandon", updateBindings.get(0)[2]);
    assertEquals("brandon@example.com", updateBindings.get(1)[0]);
  }
}
