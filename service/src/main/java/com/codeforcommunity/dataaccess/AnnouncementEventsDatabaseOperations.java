package com.codeforcommunity.dataaccess;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.auth.Passwords;
import com.codeforcommunity.dto.announcement_event.Announcement;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.EmailAlreadyInUseException;
import com.codeforcommunity.exceptions.ExpiredEmailVerificationTokenException;
import com.codeforcommunity.exceptions.InvalidEmailVerificationTokenException;
import com.codeforcommunity.exceptions.UserDoesNotExistException;
import com.codeforcommunity.processor.AuthProcessorImpl;
import com.codeforcommunity.propertiesLoader.PropertiesLoader;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.pojos.Users;
import org.jooq.generated.tables.records.UsersRecord;
import org.jooq.generated.tables.records.VerificationKeysRecord;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.jooq.impl.DSL;

import static org.jooq.generated.Tables.ANNOUNCEMENTS;
import static org.jooq.generated.Tables.USERS;

/**
 * Encapsulates all the database operations that are required for {@link AuthProcessorImpl}.
 */
public class AnnouncementEventsDatabaseOperations {

  private final DSLContext db;

  public AnnouncementEventsDatabaseOperations(DSLContext db) {
    this.db = db;
  }

  /**
   * Gets announcements in the specified date range.
   *
   * @param start start timestamp
   * @param end end timestamp
   * @param count count
   * @return a list of announcements
   */
  public List<Announcement> getAnnouncements(Timestamp start, Timestamp end, int count) {
    List<Announcement> announcements = db.selectFrom(ANNOUNCEMENTS)
        .where(ANNOUNCEMENTS.CREATED.between(start, end))
        .orderBy(ANNOUNCEMENTS.CREATED.desc())
        .fetchInto(Announcement.class);
    if (count < announcements.size()) {
      return announcements.subList(0, count);
    }
    return announcements;
  }

  /**
   * Creates a new announcement. The timestamp is set to the current UNIX time and the ID is
   * set to the current max ID in the database plus 1.
   *
   * @param title the title of the announcement
   * @param description the description for the announcement
   */
  public void createNewAnnouncement(String title, String description) {
    int maxId = db.select(DSL.max(ANNOUNCEMENTS.ID))
        .from(ANNOUNCEMENTS).fetchOneInto(Integer.class);
    db.insertInto(ANNOUNCEMENTS,
        ANNOUNCEMENTS.ID, ANNOUNCEMENTS.TITLE, ANNOUNCEMENTS.DESCRIPTION, ANNOUNCEMENTS.CREATED)
        .values(maxId + 1, title, description, new Timestamp(System.currentTimeMillis()))
        .execute();
  }



  /**
   * Creates a JWTData object for the user with the given email if they exist.
   *
   * @throws UserDoesNotExistException if given email does not match a user.
   */
  public JWTData getUserJWTData(String email) {
    Optional<Users> maybeUser = Optional.ofNullable(db.selectFrom(USERS)
        .where(USERS.EMAIL.eq(email))
        .fetchOneInto(Users.class));

    if (maybeUser.isPresent()) {
      Users user = maybeUser.get();
      return new JWTData(user.getId(), user.getPrivilegeLevel());
    } else {
      throw new UserDoesNotExistException(email);
    }
  }

  /**
   * Returns true if the given username and password correspond to a user in the USER table and
   * false otherwise.
   */
  public boolean isValidLogin(String email, String pass) {
    Optional<Users> maybeUser = Optional.ofNullable(db
        .selectFrom(USERS)
        .where(USERS.EMAIL.eq(email))
        .fetchOneInto(Users.class));

    return maybeUser
        .filter(user -> Passwords.isExpectedPassword(pass, user.getPassHash()))
        .isPresent();
  }

  /**
   * TODO: Refactor this method to take in a DTO / POJO instance
   * Creates a new row in the USER table with the given values.
   *
   * @throws EmailAlreadyInUseException if the given username and email are already used in the USER table.
   */
  public void createNewUser(String email, String password, String firstName, String lastName) {
    boolean emailUsed = db.fetchExists(db.selectFrom(USERS).where(USERS.EMAIL.eq(email)));
    if (emailUsed) {
      throw new EmailAlreadyInUseException(email);
    }

    UsersRecord newUser = db.newRecord(USERS);
    newUser.setEmail(email);
    newUser.setPassHash(Passwords.createHash(password));
    newUser.setFirstName(firstName);
    newUser.setLastName(lastName);
    newUser.setPrivilegeLevel(PrivilegeLevel.GP);
    newUser.store();

    // TODO: Send verification email
  }

  /**
   * Given a JWT signature, store it in the BLACKLISTED_REFRESHES table.
   */
  public void addToBlackList(String signature) {
    Timestamp expirationTimestamp = Timestamp.from(Instant.now().plusMillis(MS_REFRESH_EXPIRATION));
    db.newRecord(Tables.BLACKLISTED_REFRESHES)
        .values(signature, expirationTimestamp)
        .store();
  }

  /**
   * Given a JWT signature return true if it is stored in the BLACKLISTED_REFRESHES table.
   */
  public boolean isOnBlackList(String signature) {
    return db.fetchExists(
        Tables.BLACKLISTED_REFRESHES
            .where(Tables.BLACKLISTED_REFRESHES.REFRESH_HASH.eq(signature)));
  }

  /**
   * Validates the email/secret key for the user it was created for.
   *
   * @throws InvalidEmailVerificationTokenException if the given token does not exist.
   * @throws ExpiredEmailVerificationTokenException if the given token is expired.
   */
  public void validateSecretKey(String secretKey) {
    VerificationKeysRecord verificationKey = db.selectFrom(Tables.VERIFICATION_KEYS)
        .where(Tables.VERIFICATION_KEYS.ID.eq(secretKey)
            .and(Tables.VERIFICATION_KEYS.USED.eq(false)))
        .fetchOneInto(VerificationKeysRecord.class);

    if (verificationKey == null) {
      throw new InvalidEmailVerificationTokenException();
    }

    if (!isTokenDateValid(verificationKey)) {
      throw new ExpiredEmailVerificationTokenException();
    }

    verificationKey.setUsed(true);
    verificationKey.store();
    db.update(USERS).set(USERS.VERIFIED, 1)
        .where(USERS.ID.eq(verificationKey.getUserId()));
  }

  /**
   * Given a userId and token, stores the token in the verification_keys table for the user.
   *
   * @throws UserDoesNotExistException if given userId does not match a user.
   */
  public String createSecretKey(int userId) {
    if (!db.fetchExists(USERS.where(USERS.ID.eq(userId)))) {
      throw new UserDoesNotExistException(userId);
    }

    String token = Passwords.generateRandomToken(50);

    VerificationKeysRecord keysRecord = db.newRecord(Tables.VERIFICATION_KEYS);
    keysRecord.setId(token);
    keysRecord.setUserId(userId);
    keysRecord.store();

    return token;
  }

  /**
   * Determines if given token date is still valid.
   *
   * @return true if it is within the time specified in the expiration.properties file.
   */
  private boolean isTokenDateValid(VerificationKeysRecord tokenResult) {
    Timestamp cutoffDate = Timestamp.from(Instant.now().minusSeconds(SECONDS_VERIFICATION_EMAIL_VALID));
    return tokenResult.getCreated().after(cutoffDate);
  }
}
