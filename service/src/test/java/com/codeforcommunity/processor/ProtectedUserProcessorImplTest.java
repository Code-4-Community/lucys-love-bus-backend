package com.codeforcommunity.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.auth.Passwords;
import com.codeforcommunity.dto.user.ChangePasswordRequest;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.UserDoesNotExistException;
import com.codeforcommunity.exceptions.WrongPasswordException;
import java.util.List;
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

  // TODO
  @Test
  public void testSetContactsAndChildren() {
    fail("TODO!!!");
  }
}
