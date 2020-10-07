package com.codeforcommunity.api;

import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.announcements.PostAnnouncementRequest;
import com.codeforcommunity.dto.announcements.PostAnnouncementResponse;

public interface IProtectedAnnouncementsProcessor extends IAnnouncementsProcessor {

    /**
     * Creates a new announcement.
     *
     * @param request DTO containing the data for the announcement
     * @param userData the JWT data for the user making the request
     * @return the created announcement
     */
    PostAnnouncementResponse postAnnouncement(PostAnnouncementRequest request, JWTData userData);

    /**
     * Creates a new event-specific announcement.
     *
     * @param request DTO containing the data for the announcement
     * @param userData the JWT data for the user making the request
     * @param eventId the ID of the event
     * @return the created announcement
     */
    PostAnnouncementResponse postEventSpecificAnnouncement(
            PostAnnouncementRequest request, JWTData userData, int eventId);

    /**
     * Deletes an announcement.
     *
     * @param announcementId the ID of the announcement
     * @param userData the JWT data for the user making the request
     */
    void deleteAnnouncement(int announcementId, JWTData userData);
}
