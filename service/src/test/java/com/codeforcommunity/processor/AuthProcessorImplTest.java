package com.codeforcommunity.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.JooqMock.OperationType;
import com.codeforcommunity.auth.JWTCreator;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.auth.Passwords;
import com.codeforcommunity.dto.auth.AddressData;
import com.codeforcommunity.dto.auth.ForgotPasswordRequest;
import com.codeforcommunity.dto.auth.LoginRequest;
import com.codeforcommunity.dto.auth.NewUserRequest;
import com.codeforcommunity.dto.auth.RefreshSessionRequest;
import com.codeforcommunity.dto.auth.RefreshSessionResponse;
import com.codeforcommunity.dto.auth.ResetPasswordRequest;
import com.codeforcommunity.dto.auth.SessionResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.enums.VerificationKeyType;
import com.codeforcommunity.exceptions.AuthException;
import com.codeforcommunity.exceptions.TokenInvalidException;
import com.codeforcommunity.requester.Emailer;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.UsersRecord;
import org.jooq.generated.tables.records.VerificationKeysRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Contains tests for AuthProcessorImpl.java in main
public class AuthProcessorImplTest {

  private JooqMock myJooqMock;
  private JWTCreator mockJWTCreator;
  private AuthProcessorImpl myAuthProcessorImpl;
  private Emailer mockEmailer;
  private final String REFRESH_TOKEN_EXAMPLE = "sample refresh token";
  private final String ACCESS_TOKEN_EXAMPLE = "sample access token";

  // set up all the mocks
  @BeforeEach
  public void setup() {
    this.myJooqMock = new JooqMock();
    this.mockJWTCreator = mock(JWTCreator.class);
    this.mockEmailer = mock(Emailer.class);
    this.myAuthProcessorImpl =
        new AuthProcessorImpl(myJooqMock.getContext(), this.mockEmailer, mockJWTCreator);
  }

  // test sign up where all the fields are filled in
  @Test
  public void testSignUp() {
    // seed the db
    UsersRecord record = myJooqMock.getContext().newRecord(Tables.USERS);
    record.setId(0);
    record.setPrivilegeLevel(PrivilegeLevel.STANDARD);
    myJooqMock.addReturn(OperationType.INSERT, record);
    myJooqMock.addExistsReturn(false);
    myJooqMock.addReturn(OperationType.SELECT, record);
    myJooqMock.addEmptyReturn(OperationType.UPDATE);

    when(mockJWTCreator.createNewRefreshToken(any(JWTData.class)))
        .thenReturn(REFRESH_TOKEN_EXAMPLE);

    Optional<String> accessToken = Optional.of(ACCESS_TOKEN_EXAMPLE);

    when(mockJWTCreator.getNewAccessToken(anyString())).thenReturn(accessToken);

    NewUserRequest req =
        new NewUserRequest(
            "hello@example.com",
            "password",
            "Brandon",
            "Liang",
            new AddressData("West 5th Street", "New York", "NY", "10002"),
            "555-555-5555",
            "Peanuts",
            "Brandon's referrer",
            false,
            null,
            null,
            null,
            null,
            null, null);

    SessionResponse res = myAuthProcessorImpl.signUp(req);

    assertEquals(res.getAccessToken(), ACCESS_TOKEN_EXAMPLE);
    assertEquals(res.getRefreshToken(), REFRESH_TOKEN_EXAMPLE);
  }

  // test log in where all the fields are null
  @Test
  public void testLogin1() {
    myJooqMock.addEmptyReturn(OperationType.SELECT);

    LoginRequest myLoginRequest = new LoginRequest();

    try {
      myAuthProcessorImpl.login(myLoginRequest);
      fail();
    } catch (AuthException e) {
      assertEquals(e.getMessage(), "Could not validate username password combination");
    }
  }

  // test log in with user email not found
  @Test
  public void testLogin2() {
    String incorrectEmail = "incorrect@email.com";
    String incorrectPass = "incorrect";

    myJooqMock.addEmptyReturn(OperationType.SELECT);

    LoginRequest myLoginRequest = new LoginRequest();

    myLoginRequest.setEmail(incorrectEmail);
    myLoginRequest.setPassword(incorrectPass);

    try {
      myAuthProcessorImpl.login(myLoginRequest);
      fail();
    } catch (AuthException e) {
      assertEquals(e.getMessage(), "Could not validate username password combination");
    }
  }

  // test log in with user incorrect password
  @Test
  public void testLogin3() {
    String loginEmail = "conner@example.com";
    String loginPass = "fundies";

    int recordId = 1;
    PrivilegeLevel recordPL = PrivilegeLevel.STANDARD;

    // make a user record
    UsersRecord record = myJooqMock.getContext().newRecord(Tables.USERS);
    record.setId(recordId);
    record.setPrivilegeLevel(recordPL);
    record.setEmail(loginEmail);
    record.setPassHash(Passwords.createHash(loginPass));
    myJooqMock.addReturn(OperationType.SELECT, record);

    when(mockJWTCreator.createNewRefreshToken(any(JWTData.class)))
        .thenReturn(REFRESH_TOKEN_EXAMPLE);

    Optional<String> accessToken = Optional.of(ACCESS_TOKEN_EXAMPLE);

    when(mockJWTCreator.getNewAccessToken(anyString())).thenReturn(accessToken);

    LoginRequest myLoginRequest = new LoginRequest();

    myLoginRequest.setEmail(loginEmail);
    myLoginRequest.setPassword("making " + loginPass + " incorrect");

    try {
      myAuthProcessorImpl.login(myLoginRequest);
      fail();
    } catch (AuthException e) {
      assertEquals(e.getMessage(), "Could not validate username password combination");
    }
  }

  // test log in with correct credentials 1
  @Test
  public void testLogin4() {
    String loginEmail = "conner@example.com";
    String loginPass = "fundies";

    int recordId = 1;
    PrivilegeLevel recordPL = PrivilegeLevel.STANDARD;

    // make a user record
    UsersRecord record = myJooqMock.getContext().newRecord(Tables.USERS);
    record.setId(recordId);
    record.setPrivilegeLevel(recordPL);
    record.setEmail(loginEmail);
    record.setPassHash(Passwords.createHash(loginPass));
    myJooqMock.addReturn(OperationType.SELECT, record);

    when(mockJWTCreator.createNewRefreshToken(any(JWTData.class)))
        .thenReturn(REFRESH_TOKEN_EXAMPLE);

    Optional<String> accessToken = Optional.of(ACCESS_TOKEN_EXAMPLE);

    when(mockJWTCreator.getNewAccessToken(anyString())).thenReturn(accessToken);

    LoginRequest myLoginRequest = new LoginRequest();

    myLoginRequest.setEmail(loginEmail);
    myLoginRequest.setPassword(loginPass);

    SessionResponse res = myAuthProcessorImpl.login(myLoginRequest);

    assertEquals(res.getAccessToken(), ACCESS_TOKEN_EXAMPLE);
    assertEquals(res.getRefreshToken(), REFRESH_TOKEN_EXAMPLE);
  }

  // test log in with correct credentials 2
  @Test
  public void testLogin5() {
    String loginEmail = "brandon@example.com";
    String loginPass = "fundies";

    int recordId = 1;
    PrivilegeLevel recordPL = PrivilegeLevel.STANDARD;

    // make a user record
    UsersRecord recordCopy = myJooqMock.getContext().newRecord(Tables.USERS);
    recordCopy.setId(recordId);
    recordCopy.setPrivilegeLevel(recordPL);
    recordCopy.setEmail(loginEmail);
    recordCopy.setPassHash(Passwords.createHash(loginPass));
    myJooqMock.addReturn(OperationType.SELECT, recordCopy);

    when(mockJWTCreator.createNewRefreshToken(any(JWTData.class)))
        .thenReturn(REFRESH_TOKEN_EXAMPLE);

    Optional<String> accessToken = Optional.of(ACCESS_TOKEN_EXAMPLE);

    when(mockJWTCreator.getNewAccessToken(anyString())).thenReturn(accessToken);

    LoginRequest myLoginRequest = new LoginRequest();

    myLoginRequest.setEmail(loginEmail);
    myLoginRequest.setPassword(loginPass);

    SessionResponse res = myAuthProcessorImpl.login(myLoginRequest);

    assertEquals(res.getAccessToken(), ACCESS_TOKEN_EXAMPLE);
    assertEquals(res.getRefreshToken(), REFRESH_TOKEN_EXAMPLE);
  }

  // test that logout adds token to blacklist correctly
  @Test
  public void testLogout() {
    // mock the blacklisted refresh token table
    myJooqMock.addEmptyReturn(OperationType.INSERT);
    myAuthProcessorImpl.logout("sample.refresh.token");

    // is the binding correct
    assertEquals("token", myJooqMock.getSqlOperationBindings().get(OperationType.INSERT).get(0)[0]);
  }

  // test session refresh with correctly refreshed token
  @Test
  public void testRefreshSession1() {
    myJooqMock.addEmptyReturn(OperationType.SELECT);

    Optional<String> accessToken = Optional.of(ACCESS_TOKEN_EXAMPLE);

    when(mockJWTCreator.getNewAccessToken(anyString())).thenReturn(accessToken);

    RefreshSessionRequest myRefreshSessionRequest =
        new RefreshSessionRequest("valid.refresh.request");

    RefreshSessionResponse res = myAuthProcessorImpl.refreshSession(myRefreshSessionRequest);

    assertEquals(res.getFreshAccessToken(), ACCESS_TOKEN_EXAMPLE);
  }

  // test session refresh with invalid refresh token
  @Test
  public void testRefreshSession2() {
    myJooqMock.addEmptyReturn(OperationType.SELECT);

    Optional<String> accessToken = Optional.empty();

    when(mockJWTCreator.getNewAccessToken(anyString())).thenReturn(accessToken);

    RefreshSessionRequest invalid = new RefreshSessionRequest("invalid.refresh.request");

    try {
      myAuthProcessorImpl.refreshSession(invalid);
      fail();
    } catch (TokenInvalidException e) {
      assertEquals(e.getTokenType(), "refresh");
    }
  }

  // test session refresh with token invalidated by a previous logout
  @Test
  public void testRefreshSession3() {
    myJooqMock.addExistsReturn(true);

    myJooqMock.addEmptyReturn(OperationType.SELECT);

    Optional<String> accessToken = Optional.of(ACCESS_TOKEN_EXAMPLE);

    when(mockJWTCreator.getNewAccessToken(anyString())).thenReturn(accessToken);

    RefreshSessionRequest invalid = new RefreshSessionRequest("invalid.refresh.request");

    try {
      myAuthProcessorImpl.refreshSession(invalid);
      fail();
    } catch (AuthException e) {
      assertEquals(e.getMessage(), "The refresh token has been invalidated by a previous logout");
    }
  }

  // test that requestPasswordReset exits gracefully if user doesn't exist
  @Test
  public void testRequestPasswordReset1() {
    String userEmail = "brandon@example.com";

    ForgotPasswordRequest mockReq = mock(ForgotPasswordRequest.class);
    when(mockReq.getEmail()).thenReturn(userEmail);

    myJooqMock.addEmptyReturn(OperationType.SELECT);

    myAuthProcessorImpl.requestPasswordReset(mockReq);
  }

  // test that requestPasswordReset succeeds under normal circumstances
  @Test
  public void testRequestPasswordReset2() {
    String userEmail = "brandon@example.com";

    ForgotPasswordRequest mockReq = mock(ForgotPasswordRequest.class);
    when(mockReq.getEmail()).thenReturn(userEmail);

    // mock the DB for the user
    UsersRecord myUsersRecord = new UsersRecord();
    myUsersRecord.setId(0);
    myUsersRecord.setEmail(userEmail);
    myJooqMock.addReturn(OperationType.SELECT, myUsersRecord);

    // so that JooqMock doesn't give us warnings
    myJooqMock.addEmptyReturn(OperationType.INSERT);
    myJooqMock.addEmptyReturn(OperationType.UPDATE);

    myAuthProcessorImpl.requestPasswordReset(mockReq);

    verify(mockReq, times(1)).getEmail();
  }

  // test that resetting the password fails if it's too short
  @Test
  public void testResetPassword1() {
    String sk = "secret key";
    String goodPassword = "good-password";

    ResetPasswordRequest req = new ResetPasswordRequest(sk, goodPassword);

    // mock the DB so that it contains the correct verification key
    VerificationKeysRecord vkRecord = new VerificationKeysRecord();
    vkRecord.setId(sk);
    vkRecord.setUserId(0);
    vkRecord.setType(VerificationKeyType.FORGOT_PASSWORD);
    vkRecord.setUsed(false);
    vkRecord.setCreated(new Timestamp(new Date().getTime()));
    myJooqMock.addReturn(OperationType.SELECT, vkRecord);
    myJooqMock.addReturn(OperationType.UPDATE, vkRecord);

    // mock the DB so that it contains an actual user
    UsersRecord userRecord = new UsersRecord();
    userRecord.setId(0);
    myJooqMock.addReturn(OperationType.SELECT, userRecord);
    myJooqMock.addReturn(OperationType.UPDATE, userRecord);

    myAuthProcessorImpl.resetPassword(req);

    // test if the correct items are being updated
    List<Object[]> updateBindings = myJooqMock.getSqlOperationBindings().get(OperationType.UPDATE);
    assertEquals(sk, updateBindings.get(0)[1]);

    // hashes are of equal length
    assertEquals(
        Passwords.createHash(goodPassword).length, ((byte[]) (updateBindings.get(1)[0])).length);
  }

  // test that verifying an email works properly
  @Test
  public void testVerifyEmail() {
    String sk = "secret key";

    // mock the DB so that it contains the correct verification key
    VerificationKeysRecord vkRecord = new VerificationKeysRecord();
    vkRecord.setId(sk);
    vkRecord.setUserId(0);
    vkRecord.setType(VerificationKeyType.VERIFY_EMAIL);
    vkRecord.setUsed(false);
    vkRecord.setCreated(new Timestamp(new Date().getTime()));
    myJooqMock.addReturn(OperationType.SELECT, vkRecord);
    myJooqMock.addReturn(OperationType.UPDATE, vkRecord);

    // mock the DB so that it contains an actual user
    UsersRecord userRecord = new UsersRecord();
    userRecord.setId(0);
    myJooqMock.addReturn(OperationType.SELECT, userRecord);
    myJooqMock.addReturn(OperationType.UPDATE, userRecord);

    myAuthProcessorImpl.verifyEmail(sk);

    List<Object[]> updateBindings = myJooqMock.getSqlOperationBindings().get(OperationType.UPDATE);

    // test if the correct items are being updated
    assertEquals(sk, updateBindings.get(0)[1]);
    assertEquals(true, updateBindings.get(1)[0]);
  }
}
