package com.codeforcommunity.processor;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.*;
import com.codeforcommunity.enums.PrivilegeLevel;

import java.util.*;
import java.sql.Timestamp;

import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.BlacklistedRefreshesRecord;
import org.jooq.generated.tables.records.UsersRecord;
import org.jooq.generated.tables.records.VerificationKeysRecord;
import org.jooq.impl.UpdatableRecordImpl;
import org.junit.Before;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;
import org.junit.Test;

import com.codeforcommunity.dto.auth.*;
import com.codeforcommunity.exceptions.*;

// Contains tests for AuthProcessorImpl.java in main
public class AuthProcessorImplTest {
    private JooqMock myJooqMock;
    private JWTCreator mockJWTCreator;
    private AuthProcessorImpl myAuthProcessorImpl;
    private final String REFRESH_TOKEN_EXAMPLE = "sample refresh token";
    private final String ACCESS_TOKEN_EXAMPLE = "sample access token";

    // set up all the mocks
    @Before
    public void setup() {
        this.myJooqMock = new JooqMock();
        this.mockJWTCreator = Mockito.mock(JWTCreator.class);
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

        List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
        myJooqMock.addReturn("SELECT", emptySelectStatement);

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

    // test that logout adds token to blacklist correctly
    @Test
    public void testLogout() {
        // mock the blacklisted refresh token table
        myJooqMock.addReturn("INSERT", new ArrayList<BlacklistedRefreshesRecord>());
        myAuthProcessorImpl.logout("sample.refresh.token");

        // is the binding correct
        assertEquals("token", myJooqMock.getSqlBindings().get("INSERT").get(0)[0]);
    }

    // test if validateSecretKey constructs the correct SQL bindings
    @Test
    public void testValidateSecretKey() {
        // mock the verification keys table
        VerificationKeysRecord myVerificationKey = new VerificationKeysRecord();

        // 10 seconds should be far enough in the future not to expire
        Timestamp future = new Timestamp(new Date().getTime() + 10000);
        myVerificationKey.setCreated(future);
        myVerificationKey.setUserId(0);
        myJooqMock.addReturn("SELECT", myVerificationKey);
        myJooqMock.addReturn("INSERT", myVerificationKey);

        myAuthProcessorImpl.validateSecretKey("secret key");

        // is the binding correct for secret key
        // TODO: dev team should review, not sure if this is what I really need to test
        assertEquals("secret key", myJooqMock.getSqlBindings().get("SELECT").get(0)[0]);
    }
}
