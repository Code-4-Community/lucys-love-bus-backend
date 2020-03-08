package com.codeforcommunity.api;

import com.codeforcommunity.dto.announcement_event.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcement_event.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcement_event.PostAnnouncementsRequest;

public interface IAnnouncementEventsProcessor {

  /**
   * Gets all announcements.
   *
   * @param request DTO containing optional params, startDate, endDate, and count
   * @return an announcements response DTO
   */
  GetAnnouncementsResponse getAllAnnouncements(GetAnnouncementsRequest request);

  /**
   * Creates a new announcement.
   *
   * @param request DTO containing the data for the announcement
   */
  void postAllAnnouncements(PostAnnouncementsRequest request);

}
