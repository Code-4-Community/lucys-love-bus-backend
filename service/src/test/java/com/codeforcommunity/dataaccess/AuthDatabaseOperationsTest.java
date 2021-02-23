package com.codeforcommunity.dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.JooqMock.OperationType;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.auth.Passwords;
import com.codeforcommunity.dto.auth.AddressData;
import com.codeforcommunity.dto.auth.NewUserRequest;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.enums.VerificationKeyType;
import com.codeforcommunity.exceptions.EmailAlreadyInUseException;
import com.codeforcommunity.exceptions.ExpiredSecretKeyException;
import com.codeforcommunity.exceptions.InvalidSecretKeyException;
import com.codeforcommunity.exceptions.UsedSecretKeyException;
import com.codeforcommunity.exceptions.UserDoesNotExistException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.UsersRecord;
import org.jooq.generated.tables.records.VerificationKeysRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Contains tests for AuthDatabaseOperations.java
public class AuthDatabaseOperationsTest {
  private JooqMock myJooqMock;
  private AuthDatabaseOperations myAuthDatabaseOperations;

  // use UNIX time for ease of testing
  // 04/16/2020 @ 1:20am (UTC)
  private final int TIMESTAMP_TEST = 1587000000;

  // set up all the mocks
  @BeforeEach
  public void setup() {
    this.myJooqMock = new JooqMock();
    this.myAuthDatabaseOperations = new AuthDatabaseOperations(myJooqMock.getContext());
  }

  // proper exception is thrown when user doesn't exist in DB
  @Test
  public void testGetUserJWTData1() {
    String myEmail = "brandon@example.com";

    // no users in DB
    myJooqMock.addEmptyReturn(OperationType.SELECT);

    try {
      myAuthDatabaseOperations.getUserJWTData(myEmail);
      fail();
    } catch (UserDoesNotExistException e) {
      assertEquals(e.getIdentifierMessage(), "email = " + myEmail);
    }
  }

  // works as expected when user does indeed exist
  @Test
  public void testGetUserJWTData2() {
    String myEmail = "brandon@example.com";

    // one user in DB
    UsersRecord myUser = myJooqMock.getContext().newRecord(Tables.USERS);
    myUser.setEmail(myEmail);
    myUser.setId(1);
    myUser.setPrivilegeLevel(PrivilegeLevel.GP);
    myJooqMock.addReturn(OperationType.SELECT, myUser);

    JWTData userData = myAuthDatabaseOperations.getUserJWTData(myEmail);

    assertEquals(userData.getUserId(), myUser.getId());
    assertEquals(userData.getPrivilegeLevel(), myUser.getPrivilegeLevel());
  }

  // returns false for incorrect login
  @Test
  public void testIsValidLogin1() {
    String myEmail = "brandon@example.com";

    // one user in DB
    UsersRecord myUser = myJooqMock.getContext().newRecord(Tables.USERS);
    myUser.setEmail(myEmail);
    myUser.setPassHash(Passwords.createHash("letmein"));
    myUser.setId(1);
    myUser.setPrivilegeLevel(PrivilegeLevel.GP);
    myJooqMock.addReturn(OperationType.SELECT, myUser);

    assertFalse(myAuthDatabaseOperations.isValidLogin(myEmail, "letmeout"));
  }

  // returns true for correct login
  @Test
  public void testIsValidLogin2() {
    String myEmail = "brandon@example.com";

    // one user in DB
    UsersRecord myUser = myJooqMock.getContext().newRecord(Tables.USERS);
    myUser.setEmail(myEmail);
    myUser.setPassHash(Passwords.createHash("letmein"));
    myUser.setId(1);
    myUser.setPrivilegeLevel(PrivilegeLevel.GP);
    myJooqMock.addReturn(OperationType.SELECT, myUser);

    assertTrue(myAuthDatabaseOperations.isValidLogin(myEmail, "letmein"));
  }

  // creating a new user fails when the email is already in use
  @Test
  public void testCreateNewUser1() {
    String myEmail = "brandon@example.com";

    myJooqMock.addExistsReturn(true);

    NewUserRequest req =
        new NewUserRequest(
            myEmail, "letmeout", "Brandon", "Liang", null, null, null, "Brandon's referrer");

    try {
      myAuthDatabaseOperations.createNewUser(req);
      fail();
    } catch (EmailAlreadyInUseException e) {
      assertEquals(e.getEmail(), "brandon@example.com");
    }
  }

  // creating a new user succeeds when the email isn't already in use
  @Test
  public void testCreateNewUser2() {
    // no users in DB
    myJooqMock.addEmptyReturn(OperationType.INSERT);

    String sampleEmail = "conner@example.com";
    String samplePassword = "letmeout";
    String sampleFN = "Conner";
    String sampleLN = "Nilsen";
    AddressData sampleLocation = new AddressData("420 Hemenway Street", "Boston", "MA", "02115");
    String samplePN = "200-233-4334";
    String sampleAllergies = "Eggs/Fish";
    String sampleReferrer = "Conner's referrer";

    NewUserRequest req =
        new NewUserRequest(
            sampleEmail,
            samplePassword,
            sampleFN,
            sampleLN,
            sampleLocation,
            samplePN,
            sampleAllergies,
            sampleReferrer);

    myJooqMock.addEmptyReturn(OperationType.SELECT);
    myJooqMock.addEmptyReturn(OperationType.UPDATE);

    myAuthDatabaseOperations.createNewUser(req);

    List<Object[]> insertBindings = myJooqMock.getSqlOperationBindings().get(OperationType.INSERT);

    assertEquals(sampleFN, insertBindings.get(1)[5]);
    assertEquals(sampleLN, insertBindings.get(1)[6]);
    assertEquals(sampleEmail, insertBindings.get(1)[3]);
  }

  // test that adding to blacklist works without breaking
  @Test
  public void testAddToBlackList() {
    // set up mock DB for inserting blacklisted refreshes
    myJooqMock.addEmptyReturn(OperationType.INSERT);

    myAuthDatabaseOperations.addToBlackList("sample signature");

    List<Object[]> bindings = myJooqMock.getSqlOperationBindings().get(OperationType.INSERT);

    assertEquals("sample signature", bindings.get(0)[0]);
  }

  // test case where signature isn't on blacklist
  @Test
  public void testIsOnBlackList1() {
    // set up mock DB for selecting blacklisted refreshes
    myJooqMock.addEmptyReturn(OperationType.SELECT);

    assertFalse(myAuthDatabaseOperations.isOnBlackList("sample signature"));
  }

  // test case where signature is on the blacklist
  @Test
  public void testIsOnBlackList2() {
    // set up mock DB for selecting blacklisted refreshes
    myJooqMock.addExistsReturn(true);

    assertTrue(myAuthDatabaseOperations.isOnBlackList("sample signature"));
  }

  // validation responds correctly handles null verification key
  @Test
  public void testValidateSecretKey1() {
    // set up mock DB for selecting no verification keys
    myJooqMock.addEmptyReturn(OperationType.SELECT);

    try {
      myAuthDatabaseOperations.validateSecretKey(
          "my secret key", VerificationKeyType.FORGOT_PASSWORD);
      fail();
    } catch (InvalidSecretKeyException e) {
      assertEquals(VerificationKeyType.FORGOT_PASSWORD, e.getType());
    }
  }

  // validation responds correctly handles null verification key
  @Test
  public void testValidateSecretKey2() {
    // set up mock DB for selecting a used verification key
    VerificationKeysRecord myVerificationKey =
        new VerificationKeysRecord(
            "0", 0, true, new Timestamp(TIMESTAMP_TEST), VerificationKeyType.FORGOT_PASSWORD);

    myJooqMock.addReturn(OperationType.SELECT, myVerificationKey);

    try {
      myAuthDatabaseOperations.validateSecretKey(
          "my secret key", VerificationKeyType.FORGOT_PASSWORD);
      fail();
    } catch (UsedSecretKeyException e) {
      assertEquals(VerificationKeyType.FORGOT_PASSWORD, e.getType());
    }
  }

  // validation responds correctly handles expired verification key
  @Test
  public void testValidateSecretKey3() {
    // set up mock DB for selecting an expired verification key
    VerificationKeysRecord myVerificationKey =
        new VerificationKeysRecord(
            "0", 0, false, new Timestamp(TIMESTAMP_TEST), VerificationKeyType.FORGOT_PASSWORD);

    myJooqMock.addReturn(OperationType.SELECT, myVerificationKey);

    try {
      myAuthDatabaseOperations.validateSecretKey(
          "my secret key", VerificationKeyType.FORGOT_PASSWORD);
      fail();
    } catch (ExpiredSecretKeyException e) {
      assertEquals(VerificationKeyType.FORGOT_PASSWORD, e.getType());
    }
  }

  // case where validateSecretKey works correctly
  @Test
  public void testValidateSecretKey4() {
    // set up mock DB for selecting verification key and user records
    VerificationKeysRecord myVerificationKey =
        new VerificationKeysRecord(
            "0",
            1,
            false,
            new Timestamp(new Date().getTime() + 100000),
            VerificationKeyType.FORGOT_PASSWORD);
    myJooqMock.addReturn(OperationType.SELECT, myVerificationKey);

    UsersRecord myUserRecord = new UsersRecord();
    myUserRecord.setId(1);
    myJooqMock.addReturn(OperationType.SELECT, myUserRecord);

    UsersRecord usersRecordResponse =
        myAuthDatabaseOperations.validateSecretKey(
            "my secret key", VerificationKeyType.FORGOT_PASSWORD);

    assertEquals(myUserRecord.getId(), usersRecordResponse.getId());
  }

  // test that createSecretKey returns a token of correct length and the correct SQL bindings
  @Test
  public void testCreateSecretKey() {
    myJooqMock.addEmptyReturn(OperationType.UPDATE);
    myJooqMock.addEmptyReturn(OperationType.INSERT);

    String token = myAuthDatabaseOperations.createSecretKey(0, VerificationKeyType.FORGOT_PASSWORD);

    List<Object[]> updateBindings = myJooqMock.getSqlOperationBindings().get(OperationType.UPDATE);
    List<Object[]> insertBindings = myJooqMock.getSqlOperationBindings().get(OperationType.INSERT);

    // returns a token of correct length and the correct SQL bindings
    assertEquals(50, token.length());
    assertEquals(true, updateBindings.get(0)[0]);
    assertEquals(token, insertBindings.get(0)[0]);
    assertEquals(0, insertBindings.get(0)[1]);
    assertEquals(VerificationKeyType.FORGOT_PASSWORD.getVal(), insertBindings.get(0)[2]);
  }
}
