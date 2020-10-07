package com.codeforcommunity.api;

import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;
import com.codeforcommunity.dto.announcements.GetEventSpecificAnnouncementsRequest;

public interface IAnnouncementsProcessor {

  /**
   * Gets all announcements.
   *
   * @param request DTO containing optional params, startDate, endDate, and count
   * @return an announcements response DTO
   */
  GetAnnouncementsResponse getAnnouncements(GetAnnouncementsRequest request);

  /**
   * Gets all the announcements for a particular event.
   *
   * @param request DTO containing the event ID
   * @return a list of announcements for the specified event, wrapped in a DTO
   */
  GetAnnouncementsResponse getEventSpecificAnnouncements(
      GetEventSpecificAnnouncementsRequest request);
}
