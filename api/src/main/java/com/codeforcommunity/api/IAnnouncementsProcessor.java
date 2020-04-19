package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.GetEventSpecificAnnouncementsRequest;
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
   * @return the created announcement
   */
  PostAnnouncementResponse postAnnouncement(PostAnnouncementRequest request, JWTData userData);

  /**
   * Gets all the announcements for a particular event.
   *
   * @param request DTO containing the event ID
   * @return a list of announcements for the specified event, wrapped in a DTO
   */
  GetAnnouncementsResponse getEventSpecificAnnouncements(
      GetEventSpecificAnnouncementsRequest request);

}
