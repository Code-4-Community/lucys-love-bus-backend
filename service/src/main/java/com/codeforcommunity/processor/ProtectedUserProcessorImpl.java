package com.codeforcommunity.processor;

import com.codeforcommunity.api.IProtectedUserProcessor;
import com.codeforcommunity.auth.JWTData;
import org.jooq.DSLContext;

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
}
