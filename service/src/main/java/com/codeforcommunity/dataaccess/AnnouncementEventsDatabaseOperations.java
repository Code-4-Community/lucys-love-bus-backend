package com.codeforcommunity.dataaccess;

import static org.jooq.generated.Tables.ANNOUNCEMENTS;

import com.codeforcommunity.dto.announcement_event.Announcement;
import com.codeforcommunity.processor.AuthProcessorImpl;
import java.sql.Timestamp;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

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

}
