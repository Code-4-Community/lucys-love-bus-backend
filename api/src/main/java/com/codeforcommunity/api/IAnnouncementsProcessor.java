package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementResponse;

public interface IAnnouncementsProcessor {

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
  PostAnnouncementResponse postAnnouncements(PostAnnouncementRequest request, JWTData userData);

}
