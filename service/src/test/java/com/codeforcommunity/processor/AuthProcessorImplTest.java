package com.codeforcommunity.processor;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTCreator;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.auth.Passwords;
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

import com.codeforcommunity.exceptions.InvalidPasswordException;
import com.codeforcommunity.exceptions.UserDoesNotExistException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.BlacklistedRefreshesRecord;
import org.jooq.generated.tables.records.UsersRecord;
import org.jooq.generated.tables.records.VerificationKeysRecord;
import org.jooq.impl.UpdatableRecordImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Contains tests for AuthProcessorImpl.java in main
public class AuthProcessorImplTest {
    private JooqMock myJooqMock;
    private JWTCreator mockJWTCreator;
    private AuthProcessorImpl myAuthProcessorImpl;
    private final String REFRESH_TOKEN_EXAMPLE = "sample refresh token";
    private final String ACCESS_TOKEN_EXAMPLE = "sample access token";

    // set up all the mocks
    @BeforeEach
    public void setup() {
        this.myJooqMock = new JooqMock();
        this.mockJWTCreator = mock(JWTCreator.class);
        this.myAuthProcessorImpl = new AuthProcessorImpl(myJooqMock.getContext(), mockJWTCreator);
    }

    // test sign up where all the fields are null
    @Test
    public void testSignUp1() {
        List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
        myJooqMock.addReturn("SELECT", emptySelectStatement);

        // seed the db
        UsersRecord record = myJooqMock.getContext().newRecord(Tables.USERS);
        record.setId(0);
        record.setPrivilegeLevel(PrivilegeLevel.GP);
        myJooqMock.addReturn("INSERT", record);
        myJooqMock.addReturn("SELECT", record);

        when(mockJWTCreator.createNewRefreshToken(any(JWTData.class)))
                .thenReturn(REFRESH_TOKEN_EXAMPLE);

        Optional<String> accessToken = Optional.of(ACCESS_TOKEN_EXAMPLE);

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        NewUserRequest myNewUserRequest = new NewUserRequest();

        try {
            myAuthProcessorImpl.signUp(myNewUserRequest);
            fail();
        } catch (NullPointerException e) {
            // we're good
        }
    }

    // test sign up where all the fields are filled in
    @Test
    public void testSignUp2() {
        List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
        myJooqMock.addReturn("SELECT", emptySelectStatement);

        // seed the db
        UsersRecord record = myJooqMock.getContext().newRecord(Tables.USERS);
        record.setId(0);
        record.setPrivilegeLevel(PrivilegeLevel.GP);
        myJooqMock.addReturn("INSERT", record);
        myJooqMock.addReturn("SELECT", record);

        when(mockJWTCreator.createNewRefreshToken(any(JWTData.class)))
                .thenReturn(REFRESH_TOKEN_EXAMPLE);

        Optional<String> accessToken = Optional.of(ACCESS_TOKEN_EXAMPLE);

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        NewUserRequest myNewUserRequest = new NewUserRequest();
        myNewUserRequest.setEmail("hello@example.com");
        myNewUserRequest.setFirstName("Brandon");
        myNewUserRequest.setLastName("Liang");
        myNewUserRequest.setPassword("password");

        SessionResponse res = myAuthProcessorImpl.signUp(myNewUserRequest);

        assertEquals(res.getAccessToken(), ACCESS_TOKEN_EXAMPLE);
        assertEquals(res.getRefreshToken(), REFRESH_TOKEN_EXAMPLE);
    }

    // test log in where all the fields are null
    @Test
    public void testLogin1() {
        List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
        myJooqMock.addReturn("SELECT", emptySelectStatement);

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
        List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
        myJooqMock.addReturn("SELECT", emptySelectStatement);

        LoginRequest myLoginRequest = new LoginRequest();

        myLoginRequest.setEmail("incorrect@email.com");
        myLoginRequest.setPassword("incorrect");

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
        // make a user record
        UsersRecord record = myJooqMock.getContext().newRecord(Tables.USERS);
        record.setId(1);
        record.setPrivilegeLevel(PrivilegeLevel.GP);
        record.setEmail("conner@example.com");
        record.setPassHash(Passwords.createHash("fundies"));
        myJooqMock.addReturn("SELECT", record);

        when(mockJWTCreator.createNewRefreshToken(any(JWTData.class)))
                .thenReturn(REFRESH_TOKEN_EXAMPLE);

        Optional<String> accessToken = Optional.of(ACCESS_TOKEN_EXAMPLE);

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        LoginRequest myLoginRequest = new LoginRequest();

        myLoginRequest.setEmail("conner@example.com");
        myLoginRequest.setPassword("incorrect");

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
        // make a user record
        UsersRecord record = myJooqMock.getContext().newRecord(Tables.USERS);
        record.setId(1);
        record.setPrivilegeLevel(PrivilegeLevel.GP);
        record.setEmail("conner@example.com");
        record.setPassHash(Passwords.createHash("fundies"));
        myJooqMock.addReturn("SELECT", record);

        when(mockJWTCreator.createNewRefreshToken(any(JWTData.class)))
                .thenReturn(REFRESH_TOKEN_EXAMPLE);

        Optional<String> accessToken = Optional.of(ACCESS_TOKEN_EXAMPLE);

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        LoginRequest myLoginRequest = new LoginRequest();

        myLoginRequest.setEmail("conner@example.com");
        myLoginRequest.setPassword("fundies");

        SessionResponse res = myAuthProcessorImpl.login(myLoginRequest);

        assertEquals(res.getAccessToken(), ACCESS_TOKEN_EXAMPLE);
        assertEquals(res.getRefreshToken(), REFRESH_TOKEN_EXAMPLE);
    }

    // test log in with correct credentials 2
    @Test
    public void testLogin5() {
        // make a user record
        UsersRecord recordCopy = myJooqMock.getContext().newRecord(Tables.USERS);
        recordCopy.setId(1);
        recordCopy.setPrivilegeLevel(PrivilegeLevel.GP);
        recordCopy.setEmail("brandon@example.com");
        recordCopy.setPassHash(Passwords.createHash("fundies"));
        myJooqMock.addReturn("SELECT", recordCopy);

        when(mockJWTCreator.createNewRefreshToken(any(JWTData.class)))
                .thenReturn(REFRESH_TOKEN_EXAMPLE);

        Optional<String> accessToken = Optional.of(ACCESS_TOKEN_EXAMPLE);

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        LoginRequest myLoginRequest = new LoginRequest();

        myLoginRequest.setEmail("brandon@example.com");
        myLoginRequest.setPassword("fundies");

        SessionResponse res = myAuthProcessorImpl.login(myLoginRequest);

        assertEquals(res.getAccessToken(), ACCESS_TOKEN_EXAMPLE);
        assertEquals(res.getRefreshToken(), REFRESH_TOKEN_EXAMPLE);
    }

    // test that logout adds token to blacklist correctly
    @Test
    public void testLogout() {
        // mock the blacklisted refresh token table
        myJooqMock.addReturn("INSERT", new ArrayList<BlacklistedRefreshesRecord>());
        myAuthProcessorImpl.logout("sample.refresh.token");

        // is the binding correct
        assertEquals("token", myJooqMock.getSqlBindings().get("INSERT").get(0)[0]);
    }

    // test session refresh with correctly refreshed token
    @Test
    public void testRefreshSession1() {
        List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
        myJooqMock.addReturn("SELECT", emptySelectStatement);

        Optional<String> accessToken = Optional.of(ACCESS_TOKEN_EXAMPLE);

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        RefreshSessionRequest myRefreshSessionRequest = new RefreshSessionRequest("valid.refresh.request");

        RefreshSessionResponse res = myAuthProcessorImpl.refreshSession(myRefreshSessionRequest);

        assertEquals(res.getFreshAccessToken(), ACCESS_TOKEN_EXAMPLE);
    }

    // test session refresh with invalid refresh token 
    @Test
    public void testRefreshSession2() {
        List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
        myJooqMock.addReturn("SELECT", emptySelectStatement);

        Optional<String> accessToken = Optional.empty();

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        RefreshSessionRequest invalid = new RefreshSessionRequest("invalid.refresh.request");

        try {
            myAuthProcessorImpl.refreshSession(invalid);
            fail();
        } catch (AuthException e) {
            assertEquals(e.getMessage(), "The given refresh token is invalid");
        }
    }

    // test session refresh with token invalidated by a previous logout
    @Test
    public void testRefreshSession3() {
        BlacklistedRefreshesRecord record = myJooqMock.getContext().newRecord(Tables.BLACKLISTED_REFRESHES);
        record.setRefreshHash(ACCESS_TOKEN_EXAMPLE);
        myJooqMock.addReturn("SELECT", record);

        myJooqMock.addEmptyReturn("SELECT");

        Optional<String> accessToken = Optional.of(ACCESS_TOKEN_EXAMPLE);

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        RefreshSessionRequest invalid = new RefreshSessionRequest("invalid.refresh.request");

        try {
            myAuthProcessorImpl.refreshSession(invalid);
            fail();
        } catch (AuthException e) {
            assertEquals(e.getMessage(), "The refresh token has been invalidated by a previous logout");
        }
    }

    // test that requestPasswordReset throws proper exception if user doesn't exist
    @Test
    public void testRequestPasswordReset1() {
        String userEmail = "brandon@example.com";

        ForgotPasswordRequest mockReq = mock(ForgotPasswordRequest.class);
        when(mockReq.getEmail()).thenReturn(userEmail);

        myJooqMock.addEmptyReturn("SELECT");

        try {
            myAuthProcessorImpl.requestPasswordReset(mockReq);
            fail();
        } catch (UserDoesNotExistException e) {
            assertEquals("email = " + userEmail, e.getIdentifierMessage());
        }

        verify(mockReq, times(1)).getEmail();
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
        myJooqMock.addReturn("SELECT", myUsersRecord);

        // so that JooqMock doesn't give us warnings
        myJooqMock.addEmptyReturn("INSERT");
        myJooqMock.addEmptyReturn("UPDATE");

        myAuthProcessorImpl.requestPasswordReset(mockReq);

        verify(mockReq, times(1)).getEmail();
    }

    // test that resetting the password fails if it's too short
    @Test
    public void testResetPassword1() {
        String sk = "secret key";
        String badPassword1 = "bad";
        String badPassword2 = "poor";

        ResetPasswordRequest req1 = new ResetPasswordRequest(sk, badPassword1);
        ResetPasswordRequest req2 = new ResetPasswordRequest(sk, badPassword2);

        try {
            myAuthProcessorImpl.resetPassword(req1);
            fail();
        } catch (InvalidPasswordException e) {
            // we're good
        }

        try {
            myAuthProcessorImpl.resetPassword(req2);
            fail();
        } catch (InvalidPasswordException e) {
            // we're good
        }
    }

    // test that resetting the password fails if it's too short
    @Test
    public void testResetPassword2() {
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
        myJooqMock.addReturn("SELECT", vkRecord);
        myJooqMock.addReturn("UPDATE", vkRecord);

        // mock the DB so that it contains an actual user
        UsersRecord userRecord = new UsersRecord();
        userRecord.setId(0);
        myJooqMock.addReturn("SELECT", userRecord);
        myJooqMock.addReturn("UPDATE", userRecord);

        myAuthProcessorImpl.resetPassword(req);

        // test if the correct items are being updated
        List<Object[]> updateBindings = myJooqMock.getSqlBindings().get("UPDATE");
        assertEquals(sk, updateBindings.get(0)[1]);

        // hashes are of equal length
        assertEquals(Passwords.createHash(goodPassword).length, ((byte[])(updateBindings.get(1)[0])).length);
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
        myJooqMock.addReturn("SELECT", vkRecord);
        myJooqMock.addReturn("UPDATE", vkRecord);

        // mock the DB so that it contains an actual user
        UsersRecord userRecord = new UsersRecord();
        userRecord.setId(0);
        myJooqMock.addReturn("SELECT", userRecord);
        myJooqMock.addReturn("UPDATE", userRecord);

        myAuthProcessorImpl.verifyEmail(sk);

        List<Object[]> updateBindings = myJooqMock.getSqlBindings().get("UPDATE");

        // test if the correct items are being updated
        assertEquals(sk, updateBindings.get(0)[1]);
        assertEquals(true, updateBindings.get(1)[0]);
    }
}
