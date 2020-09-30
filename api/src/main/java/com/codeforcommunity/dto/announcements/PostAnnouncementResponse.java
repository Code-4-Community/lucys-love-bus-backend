package com.codeforcommunity.dto.announcements;

/**
 * Represents an object containing the information for a response to a post announcement, which is
 * just the announcement.
 */
public class PostAnnouncementResponse {

  private Announcement announcement;

  private PostAnnouncementResponse() {}

  /**
   * Constructs a PostAnnouncementResponse object containing the given announcement.
   *
   * @param announcement the announcement to be contained in this response object
   */
  public PostAnnouncementResponse(Announcement announcement) {
    this.announcement = announcement;
  }

  /**
   * Gets the announcement stored in this response object.
   *
   * @return the announcement stored in this response object
   */
  public Announcement getAnnouncement() {
    return announcement;
  }
}
