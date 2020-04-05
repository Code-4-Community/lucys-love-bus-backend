package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcement_event.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcement_event.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcement_event.PostAnnouncementsRequest;
import com.codeforcommunity.dto.announcement_event.PostAnnouncementsResponse;

public interface IAnnouncementEventsProcessor {

  /**
   * Gets all announcements.
   *
   * @param request DTO containing optional params, startDate, endDate, and count
   * @return an announcements response DTO
   */
  GetAnnouncementsResponse getAnnouncements(GetAnnouncementsRequest request);

  /**
   * Creates a new announcement.
   *
   * @param request DTO containing the data for the announcement
   * @param userData the JWT data for the user making the request
   */
  PostAnnouncementsResponse postAnnouncements(PostAnnouncementsRequest request, JWTData userData);

}
