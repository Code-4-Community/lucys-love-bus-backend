package com.codeforcommunity.dataaccess;

import com.codeforcommunity.JooqMock;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.userEvents.components.*;
import com.codeforcommunity.dto.userEvents.requests.*;
import com.codeforcommunity.dto.userEvents.responses.*;

import com.codeforcommunity.enums.PrivilegeLevel;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.records.BlacklistedRefreshesRecord;
import org.jooq.generated.tables.records.EventsRecord;
import org.jooq.impl.UpdatableRecordImpl;
import org.junit.Before;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

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

  // TODO
  @Test
    public void testGetUserJWTData() {
      fail();
    }

    // TODO
    @Test
    public void testIsValidLogin() {
      fail();
    }

    // TODO
    @Test
    public void testCreateNewUser() {
      fail();
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
