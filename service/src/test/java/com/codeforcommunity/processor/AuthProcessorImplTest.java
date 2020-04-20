package com.codeforcommunity.processor;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.auth.JWTCreator;

import java.util.*;

import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.UsersRecord;
import org.jooq.impl.UpdatableRecordImpl;
import org.junit.Before;
import org.mockito.Mockito;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Before
    public void setup() {
        this.myJooqMock = new JooqMock();
        this.mockJWTCreator = Mockito.mock(JWTCreator.class);
        this.myAuthProcessorImpl = new AuthProcessorImpl(myJooqMock.getContext(), mockJWTCreator);
    }

    // test sign up where all the fields are null
    @Test
    public void testSignUp1() {
        UsersRecord record = myJooqMock.getContext().newRecord(Tables.USERS);
        record.setId(0);

        myJooqMock.addReturn("INSERT", record);

        List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
        myJooqMock.addReturn("SELECT", emptySelectStatement);

        UsersRecord recordCopy = myJooqMock.getContext().newRecord(Tables.USERS);
        recordCopy.setId(1);
        recordCopy.setPrivilegeLevel(PrivilegeLevel.GP);
        myJooqMock.addReturn("SELECT", recordCopy);

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
        UsersRecord record = myJooqMock.getContext().newRecord(Tables.USERS);
        record.setId(0);

        myJooqMock.addReturn("INSERT", record);

        List<UpdatableRecordImpl> emptySelectStatement = new ArrayList<UpdatableRecordImpl>();
        myJooqMock.addReturn("SELECT", emptySelectStatement);

        UsersRecord recordCopy = myJooqMock.getContext().newRecord(Tables.USERS);
        recordCopy.setId(1);
        recordCopy.setPrivilegeLevel(PrivilegeLevel.GP);
        myJooqMock.addReturn("SELECT", recordCopy);

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

    /*
    // test log in where all the fields are null
    @Test(expected = AuthException.class)
    public void testLogin1() {
        LoginRequest myLoginRequest = new LoginRequest();

        when(myAuthProcessorImpl.login(myLoginRequest))
        .thenThrow(new AuthException("Could not validate username password combination"));
    }

    // test log in with incorrect credentials
    @Test(expected = AuthException.class)
    public void testLogin2() {
        LoginRequest myLoginRequest = new LoginRequest();

        myLoginRequest.setEmail("incorrect@email.com");
        myLoginRequest.setPassword("incorrect");

        when(myAuthProcessorImpl.login(myLoginRequest))
        .thenThrow(new AuthException("Could not validate username password combination"));
    }

    // test log in with correct credentials 1
    @Test
    public void testLogin3() {
        LoginRequest myLoginRequest = new LoginRequest();

        myLoginRequest.setEmail("correct1@email.com");
        myLoginRequest.setPassword("correct1");

        SessionResponse res = myAuthProcessorImpl.login(myLoginRequest);

        assertEquals(res.getAccessToken(), "something");
        assertEquals(res.getRefreshToken(), "something");
    }

    // test log in with correct credentials 2
    @Test
    public void testLogin4() {
        LoginRequest myLoginRequest = new LoginRequest();

        myLoginRequest.setEmail("correct2@email.com");
        myLoginRequest.setPassword("correct2");

        SessionResponse res = myAuthProcessorImpl.login(myLoginRequest);

        assertEquals(res.getAccessToken(), "something");
        assertEquals(res.getRefreshToken(), "something");
    }

    // test session refresh with refresh token invalidated by a previous logout
    @Test(expected = AuthException.class)
    public void testRefreshSession1() {
        RefreshSessionRequest previouslyInvalidated = new RefreshSessionRequest("invalidated by a previous logout");

        when(myAuthProcessorImpl.refreshSession(previouslyInvalidated))
        .thenThrow(new AuthException("The refresh token has been invalidated by a previous logout"));
    }

    // test session refresh with invalid refresh token 
    @Test(expected = AuthException.class)
    public void testRefreshSession2() {
        RefreshSessionRequest invalid = new RefreshSessionRequest("invalid refresh token");

        when(myAuthProcessorImpl.refreshSession(invalid))
        .thenThrow(new AuthException("The given refresh token is invalid"));
    }

    // test session refresh with correctly refreshed token
    @Test
    public void testRefreshSession3() {
        JWTCreator jwtCreator = Mockito.mock(JWTCreator.class);
        String correctToken = "this one works";
        RefreshSessionRequest valid = new RefreshSessionRequest(correctToken);

        RefreshSessionResponse res = new RefreshSessionResponse() {{
            setFreshAccessToken(jwtCreator.getNewAccessToken(correctToken).get());
        }};

        assertEquals(myAuthProcessorImpl.refreshSession(valid), res);
    }
    */
}
