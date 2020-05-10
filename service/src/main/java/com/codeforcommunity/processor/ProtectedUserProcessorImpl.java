package com.codeforcommunity.processor;

import com.codeforcommunity.api.IProtectedUserProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.auth.Passwords;
import com.codeforcommunity.dto.user.ChangePasswordRequest;
import com.codeforcommunity.exceptions.UserDoesNotExistException;
import com.codeforcommunity.exceptions.WrongPasswordException;
import org.jooq.DSLContext;
import org.jooq.generated.tables.records.UsersRecord;

import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;
import static org.jooq.generated.Tables.PF_REQUESTS;
import static org.jooq.generated.Tables.USERS;
import static org.jooq.generated.Tables.VERIFICATION_KEYS;

public class ProtectedUserProcessorImpl implements IProtectedUserProcessor {

  private final DSLContext db;

  public ProtectedUserProcessorImpl(DSLContext db) {
    this.db = db;
  }

  @Override
  public void deleteUser(JWTData userData) {
    int userId = userData.getUserId();

    db.deleteFrom(EVENT_REGISTRATIONS)
        .where(EVENT_REGISTRATIONS.USER_ID.eq(userId))
        .executeAsync();

    db.deleteFrom(VERIFICATION_KEYS)
        .where(VERIFICATION_KEYS.USER_ID.eq(userId))
        .executeAsync();

    db.deleteFrom(PF_REQUESTS)
        .where(PF_REQUESTS.USER_ID.eq(userId))
        .executeAsync();

    db.deleteFrom(USERS)
        .where(USERS.ID.eq(userId))
        .executeAsync();
  }

  @Override
  public void changePassword(JWTData userData, ChangePasswordRequest changePasswordRequest) {
    UsersRecord user = db.selectFrom(USERS)
        .where(USERS.ID.eq(userData.getUserId()))
        .fetchOne();

    if (user == null) {
      throw new UserDoesNotExistException(userData.getUserId());
    }

    if (Passwords.isExpectedPassword(changePasswordRequest.getCurrentPassword(), user.getPassHash())) {
      user.setPassHash(Passwords.createHash(changePasswordRequest.getNewPassword()));
      user.store();
    } else {
      throw new WrongPasswordException();
    }
  }
}
