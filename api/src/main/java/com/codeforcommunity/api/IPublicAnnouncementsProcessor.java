package com.codeforcommunity.api;

import com.codeforcommunity.dto.announcements.GetAnnouncementsRequest;
import com.codeforcommunity.dto.announcements.GetAnnouncementsResponse;

public interface IPublicAnnouncementsProcessor {

  /**
   * Gets all announcements.
   *
   * @param request DTO containing optional params, startDate, endDate, and count
   * @return an announcements response DTO
   */
  GetAnnouncementsResponse getAnnouncements(GetAnnouncementsRequest request);
}
