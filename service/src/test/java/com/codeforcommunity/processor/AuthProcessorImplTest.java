package com.codeforcommunity.processor;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.*;
import com.codeforcommunity.enums.PrivilegeLevel;

import java.util.*;

import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.BlacklistedRefreshesRecord;
import org.jooq.generated.tables.records.UsersRecord;
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
    JooqMock myJooqMock;
    JWTCreator mockJWTCreator;
    AuthProcessorImpl myAuthProcessorImpl;

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
                .thenReturn("sample refresh token");

        Optional<String> accessToken = Optional.of("sample access token");

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
                .thenReturn("sample refresh token");

        Optional<String> accessToken = Optional.of("sample access token");

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        NewUserRequest myNewUserRequest = new NewUserRequest();
        myNewUserRequest.setEmail("hello@example.com");
        myNewUserRequest.setFirstName("Brandon");
        myNewUserRequest.setLastName("Liang");
        myNewUserRequest.setPassword("password");

        SessionResponse res = myAuthProcessorImpl.signUp(myNewUserRequest);

        assertEquals(res.getAccessToken(), "sample access token");
        assertEquals(res.getRefreshToken(), "sample refresh token");
    }

    // test log in where all the fields are null
    @Test(expected = AuthException.class)
    public void testLogin1() {
        List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
        myJooqMock.addReturn("SELECT", emptySelectStatement);

        LoginRequest myLoginRequest = new LoginRequest();

        when(myAuthProcessorImpl.login(myLoginRequest))
        .thenThrow(new AuthException("Could not validate username password combination"));
    }

    // test log in with user email not found
    @Test(expected = AuthException.class)
    public void testLogin2() {
        List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
        myJooqMock.addReturn("SELECT", emptySelectStatement);

        LoginRequest myLoginRequest = new LoginRequest();

        myLoginRequest.setEmail("incorrect@email.com");
        myLoginRequest.setPassword("incorrect");

        when(myAuthProcessorImpl.login(myLoginRequest))
        .thenThrow(new AuthException("Could not validate username password combination"));
    }

    // test log in with user incorrect password
    @Test(expected = AuthException.class)
    public void testLogin3() {
        // make a user record
        UsersRecord record = myJooqMock.getContext().newRecord(Tables.USERS);
        record.setId(1);
        record.setPrivilegeLevel(PrivilegeLevel.GP);
        record.setEmail("conner@example.com");
        record.setPassHash(Passwords.createHash("fundies"));
        myJooqMock.addReturn("SELECT", record);

        when(mockJWTCreator.createNewRefreshToken(any(JWTData.class)))
                .thenReturn("sample refresh token");

        Optional<String> accessToken = Optional.of("sample access token");

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        LoginRequest myLoginRequest = new LoginRequest();

        myLoginRequest.setEmail("conner@example.com");
        myLoginRequest.setPassword("incorrect");

        when(myAuthProcessorImpl.login(myLoginRequest))
                .thenThrow(new AuthException("Could not validate username password combination"));
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
                .thenReturn("sample refresh token");

        Optional<String> accessToken = Optional.of("sample access token");

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        LoginRequest myLoginRequest = new LoginRequest();

        myLoginRequest.setEmail("conner@example.com");
        myLoginRequest.setPassword("fundies");

        SessionResponse res = myAuthProcessorImpl.login(myLoginRequest);

        assertEquals(res.getAccessToken(), "sample access token");
        assertEquals(res.getRefreshToken(), "sample refresh token");
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
                .thenReturn("sample refresh token");

        Optional<String> accessToken = Optional.of("sample access token");

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        LoginRequest myLoginRequest = new LoginRequest();

        myLoginRequest.setEmail("brandon@example.com");
        myLoginRequest.setPassword("fundies");

        SessionResponse res = myAuthProcessorImpl.login(myLoginRequest);

        assertEquals(res.getAccessToken(), "sample access token");
        assertEquals(res.getRefreshToken(), "sample refresh token");
    }

    // test session refresh with correctly refreshed token
    @Test
    public void testRefreshSession1() {
        List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
        myJooqMock.addReturn("SELECT", emptySelectStatement);

        Optional<String> accessToken = Optional.of("sample access token");

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        RefreshSessionRequest myRefreshSessionRequest = new RefreshSessionRequest("valid. refresh. request");

        RefreshSessionResponse res = myAuthProcessorImpl.refreshSession(myRefreshSessionRequest);

        assertEquals(res.getFreshAccessToken(), "sample access token");
    }

    // test session refresh with invalid refresh token 
    @Test
    public void testRefreshSession2() {
        List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
        myJooqMock.addReturn("SELECT", emptySelectStatement);

        Optional<String> accessToken = Optional.empty();

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        RefreshSessionRequest invalid = new RefreshSessionRequest("invalid. refresh. request");

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
        record.setRefreshHash("sample access token");
        myJooqMock.addReturn("SELECT", record);

        List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
        myJooqMock.addReturn("SELECT", emptySelectStatement);

        Optional<String> accessToken = Optional.of("sample access token");

        when(mockJWTCreator.getNewAccessToken(anyString()))
                .thenReturn(accessToken);

        RefreshSessionRequest invalid = new RefreshSessionRequest("invalid. refresh. request");

        try {
            myAuthProcessorImpl.refreshSession(invalid);
            fail();
        } catch (AuthException e) {
            assertEquals(e.getMessage(), "The refresh token has been invalidated by a previous logout");
        }
    }
}
