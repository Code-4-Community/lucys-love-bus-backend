package com.codeforcommunity.processor;

import static org.jooq.generated.Tables.CHILDREN;
import static org.jooq.generated.Tables.CONTACTS;
import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;
import static org.jooq.generated.Tables.USERS;

import com.codeforcommunity.api.IProtectedEventsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import com.codeforcommunity.dto.userEvents.components.RSVP;
import com.codeforcommunity.dto.userEvents.components.Registration;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.requests.ModifyEventRequest;
import com.codeforcommunity.dto.userEvents.responses.EventRegistrations;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.*;
import com.codeforcommunity.requester.S3Requester;
import java.util.Comparator;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.generated.tables.pojos.Events;
import org.jooq.generated.tables.records.EventsRecord;

public class ProtectedEventsProcessorImpl extends EventsProcessorImpl
    implements IProtectedEventsProcessor {

  public ProtectedEventsProcessorImpl(DSLContext db) {
    super(db);
  }

  @Override
  public SingleEventResponse createEvent(CreateEventRequest request, JWTData userData)
      throws BadRequestImageException, S3FailedUploadException {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }

    String publicImageUrl =
        S3Requester.validateUploadImageToS3LucyEvents(request.getTitle(), request.getThumbnail());
    request.setThumbnail(
        publicImageUrl); // Update the request to contain the URL for the DB and JSON response OR
    // null if no image given

    EventsRecord newEventRecord = eventRequestToRecord(request);
    newEventRecord.store();
    return eventPojoToResponse(newEventRecord.into(Events.class), userData);
  }

  @Override
  public SingleEventResponse modifyEvent(
      int eventId, ModifyEventRequest request, JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }
    EventsRecord record = db.fetchOne(EVENTS, EVENTS.ID.eq(eventId));
    if (request.getTitle() != null) {
      record.setTitle(request.getTitle());
    }
    if (request.getSpotsAvailable() != null) {
      int currentRegistered = eventDatabaseOperations.getSumRegistrationRequests(eventId);
      if (currentRegistered > request.getSpotsAvailable()) {
        throw new InvalidEventCapacityException(request.getSpotsAvailable(), currentRegistered);
      }
      record.setCapacity(request.getSpotsAvailable());
    }
    if (request.getThumbnail() != null) {
      record.setThumbnail(request.getThumbnail());
    }
    if (request.getDetails() != null) {
      EventDetails details = request.getDetails();
      if (details.getDescription() != null) {
        record.setDescription(details.getDescription());
      }
      if (details.getLocation() != null) {
        record.setLocation(details.getLocation());
      }
      if (details.getStart() != null) {
        record.setStartTime(details.getStart());
      }
      if (details.getEnd() != null) {
        record.setEndTime(details.getEnd());
      }
    }
    if (request.getPrice() != null) {
      record.setPrice(request.getPrice());
    }
    record.store();

    return getSingleEvent(eventId, userData);
  }

  @Override
  public void deleteEvent(int eventId, JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }
    db.delete(EVENTS).where(EVENTS.ID.eq(eventId)).execute();
  }

  @Override
  public EventRegistrations getEventRegisteredUsers(int eventId, JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }

    if (!db.fetchExists(EVENTS.where(EVENTS.ID.eq(eventId)))) {
      throw new EventDoesNotExistException(eventId);
    }

    List<Registration> regs =
        db.select(
                CONTACTS.FIRST_NAME,
                CONTACTS.LAST_NAME,
                CONTACTS.EMAIL,
                EVENT_REGISTRATIONS.TICKET_QUANTITY)
            .from(EVENT_REGISTRATIONS)
            .join(CONTACTS)
            .on(EVENT_REGISTRATIONS.USER_ID.eq(CONTACTS.USER_ID))
            .where(EVENT_REGISTRATIONS.EVENT_ID.eq(eventId))
            .and(CONTACTS.IS_MAIN_CONTACT.isTrue())
            .fetchInto(Registration.class);

    return new EventRegistrations(regs);
  }

  /**
   * Returns a String that contains the CSV data for the given event's RSVPs.
   *
   * @param eventId The event to get the RSVPs of.
   * @param userData The user.
   * @return The CSV of RSVPs.
   */
  @Override
  public String getEventRSVPs(int eventId, JWTData userData) {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }

    if (!db.fetchExists(EVENTS.where(EVENTS.ID.eq(eventId)))) {
      throw new EventDoesNotExistException(eventId);
    }

    List<RSVP> rsvpUsers =
        db.select(
                USERS.ID,
                EVENT_REGISTRATIONS.TICKET_QUANTITY,
                USERS.EMAIL,
                USERS.PRIVILEGE_LEVEL,
                USERS.ADDRESS,
                USERS.CITY,
                USERS.STATE,
                USERS.ZIPCODE)
            .from(EVENT_REGISTRATIONS)
            .join(USERS)
            .on(EVENT_REGISTRATIONS.USER_ID.eq(USERS.ID))
            .where(EVENT_REGISTRATIONS.EVENT_ID.eq(eventId))
            .fetchInto(RSVP.class);

    List<RSVP> rsvpContacts =
        db.select(
                EVENT_REGISTRATIONS.USER_ID,
                CONTACTS.EMAIL,
                CONTACTS.FIRST_NAME,
                CONTACTS.LAST_NAME,
                CONTACTS.IS_MAIN_CONTACT,
                CONTACTS.DATE_OF_BIRTH,
                CONTACTS.PHONE_NUMBER,
                CONTACTS.PRONOUNS,
                CONTACTS.ALLERGIES,
                CONTACTS.DIAGNOSIS,
                CONTACTS.MEDICATIONS,
                CONTACTS.NOTES)
            .from(EVENT_REGISTRATIONS)
            .join(CONTACTS)
            .on(EVENT_REGISTRATIONS.USER_ID.eq(CONTACTS.USER_ID))
            .where(EVENT_REGISTRATIONS.EVENT_ID.eq(eventId))
            .fetchInto(RSVP.class);

    List<RSVP> rsvpChildren =
        db.select(
                EVENT_REGISTRATIONS.USER_ID,
                CHILDREN.FIRST_NAME,
                CHILDREN.LAST_NAME,
                CHILDREN.DATE_OF_BIRTH,
                CHILDREN.PRONOUNS,
                CHILDREN.ALLERGIES,
                CHILDREN.DIAGNOSIS,
                CHILDREN.MEDICATIONS,
                CHILDREN.NOTES,
                CHILDREN.SCHOOL_YEAR,
                CHILDREN.SCHOOL)
            .from(EVENT_REGISTRATIONS)
            .join(CHILDREN)
            .on(EVENT_REGISTRATIONS.USER_ID.eq(CHILDREN.USER_ID))
            .where(EVENT_REGISTRATIONS.EVENT_ID.eq(eventId))
            .fetchInto(RSVP.class);

    rsvpUsers.addAll(rsvpContacts);
    rsvpUsers.addAll(rsvpChildren);
    rsvpUsers.sort(Comparator.comparing(RSVP::getUserId));

    StringBuilder builder = new StringBuilder();
    builder.append(RSVP.toHeaderCSV());
    for (RSVP rsvp : rsvpUsers) {
      builder.append(rsvp.toRowCSV());
    }

    return builder.toString();
  }
}
