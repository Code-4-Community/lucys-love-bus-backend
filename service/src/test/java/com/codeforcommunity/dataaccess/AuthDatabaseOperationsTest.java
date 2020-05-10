package com.codeforcommunity.dataaccess;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.auth.Passwords;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.EmailAlreadyInUseException;
import com.codeforcommunity.exceptions.UserDoesNotExistException;

import java.util.ArrayList;

import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.UsersRecord;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// Contains tests for AuthDatabaseOperations.java
public class AuthDatabaseOperationsTest {
    JooqMock myJooqMock;
    AuthDatabaseOperations myAuthDatabaseOperations;

    // set up all the mocks
    @Before
    public void setup() {
        this.myJooqMock = new JooqMock();
        this.myAuthDatabaseOperations = new AuthDatabaseOperations(myJooqMock.getContext());
    }

    // proper exception is thrown when user doesn't exist in DB
    @Test
    public void testGetUserJWTData1() {
        String myEmail = "brandon@example.com";

        // no users in DB
        myJooqMock.addReturn("SELECT", new ArrayList<UsersRecord>());

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
        myJooqMock.addReturn("SELECT", myUser);

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
        myJooqMock.addReturn("SELECT", myUser);

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
        myJooqMock.addReturn("SELECT", myUser);

        assertTrue(myAuthDatabaseOperations.isValidLogin(myEmail, "letmein"));
    }

    // creating a new user fails when the email is already in use
    @Test
    public void testCreateNewUser1() {
        String myEmail = "brandon@example.com";

        // one user in DB
        UsersRecord myUser = myJooqMock.getContext().newRecord(Tables.USERS);
        myUser.setEmail(myEmail);
        myUser.setPassHash(Passwords.createHash("letmein"));
        myUser.setId(1);
        myUser.setPrivilegeLevel(PrivilegeLevel.GP);
        myJooqMock.addReturn("SELECT", myUser);

        try {
            myAuthDatabaseOperations.createNewUser(myEmail, "letmeout", "Brandon", "Liang");
            fail();
        } catch (EmailAlreadyInUseException e) {
            assertEquals(e.getEmail(), "brandon@example.com");
        }
    }

    // creating a new user succeeds when the email isn't already in use
    @Test
    public void testCreateNewUser2() {
        // no users in DB
        myJooqMock.addReturn("SELECT", new ArrayList<UsersRecord>());

        myAuthDatabaseOperations.createNewUser("conner@example.com", "letmeout", "Conner", "Nilsen");
    }

    // TODO
    @Test
    public void testAddToBlackList() {
        fail();
    }

    // TODO
    @Test
    public void testIsOnBlackList() {
        fail();
    }

    // TODO
    @Test
    public void testValidateSecretKey() {
        fail();
    }

    // TODO
    @Test
    public void testCreateSecretKey() {
        fail();
    }
}
