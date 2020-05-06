package com.codeforcommunity.processor;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dataaccess.EventDatabaseOperations;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.dto.userEvents.components.Event;
import com.codeforcommunity.dto.userEvents.components.EventDetails;
import com.codeforcommunity.dto.userEvents.requests.CreateEventRequest;
import com.codeforcommunity.dto.userEvents.requests.GetUserEventsRequest;
import com.codeforcommunity.dto.userEvents.responses.GetEventsResponse;
import com.codeforcommunity.dto.userEvents.responses.SingleEventResponse;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.exceptions.AdminOnlyRouteException;
import org.jooq.*;
import com.codeforcommunity.exceptions.BadRequestException;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSeekStep1;
import org.jooq.SelectWhereStep;
import org.jooq.generated.tables.pojos.Events;
import org.jooq.generated.tables.records.EventsRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.Period;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.EVENT_REGISTRATIONS;
import static org.jooq.generated.Tables.USERS;
import static org.jooq.impl.DSL.sum;

public class EventsProcessorImpl implements IEventsProcessor {

  private final DSLContext db;
  private final EventDatabaseOperations eventDatabaseOperations;

  public EventsProcessorImpl(DSLContext db) {
    this.db = db;
    this.eventDatabaseOperations = new EventDatabaseOperations(db);
  }

  @Override
  public SingleEventResponse createEvent(CreateEventRequest request, JWTData userData) throws BadRequestException, IOException {
    if (userData.getPrivilegeLevel() != PrivilegeLevel.ADMIN) {
      throw new AdminOnlyRouteException();
    }

    String[] thumbSplit = request.getThumbnail().split(",", 2);
    if (thumbSplit.length < 2) {
      throw new BadRequestException();
    }

    String meta = thumbSplit[0];
    String[] metaSplit = meta.split(";", 2);
    if (metaSplit.length < 2 || !metaSplit[1].equals("base64")) {
      throw new BadRequestException();
    }

    String[] dataSplit = metaSplit[0].split(":", 2);
    if (dataSplit.length < 2) {
      throw new BadRequestException();
    }

    String[] data = dataSplit[1].split("/", 2);
    if (data.length < 2 || !data[0].equals("image")) {
      throw new BadRequestException();
    }

    String fileExtension = data[1];
    String encoding = thumbSplit[1];

    AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_2).build();
    String fileName = request.getTitle().replace(" ", "_").toLowerCase() + "_thumbnail." + fileExtension;

    byte[] imageData = Base64.getDecoder().decode(encoding);
    File tempFile = File.createTempFile(fileName, null, null);
    FileOutputStream fos = new FileOutputStream(tempFile);
    fos.write(imageData);
    fos.flush();
    fos.close();

    PutObjectRequest awsRequest = new PutObjectRequest("lucys-love-bus-public", "events/" + fileName, tempFile);
    awsRequest.setCannedAcl(CannedAccessControlList.PublicRead);

    ObjectMetadata awsObjectMetadata = new ObjectMetadata();
    awsObjectMetadata.setContentType("image/" + fileExtension);
    awsRequest.setMetadata(awsObjectMetadata);

    s3Client.putObject(awsRequest);
    tempFile.delete();

    String thumbnailUrl = "https://lucys-love-bus-public.s3.us-east-2.amazonaws.com/events/" + fileName;
    request.setThumbnail(thumbnailUrl);

    EventsRecord newEventRecord = eventRequestToRecord(request);
    newEventRecord.store();
    return eventPojoToResponse(newEventRecord.into(Events.class));
  }

  @Override
  public SingleEventResponse getSingleEvent(int eventId) {
    Events event = db.selectFrom(EVENTS)
        .where(EVENTS.ID.eq(eventId))
        .fetchOneInto(Events.class);
    return eventPojoToResponse(event);
  }

  @Override
  public GetEventsResponse getEvents(List<Integer> eventIds) {
    List<Events> e = db.selectFrom(EVENTS).where(EVENTS.ID.in(eventIds)).fetchInto(Events.class);
    return new GetEventsResponse(listOfEventsToListOfEvent(e), e.size());
  }

  @Override
  public GetEventsResponse getEventsSignedUp(GetUserEventsRequest request, JWTData userData) {

    SelectConditionStep q = db.select(EVENTS.fields())
        .from(USERS
            .join(EVENT_REGISTRATIONS).onKey(EVENT_REGISTRATIONS.USER_ID)
            .join(EVENTS).onKey(EVENT_REGISTRATIONS.EVENT_ID))
        .where(USERS.ID.eq(userData.getUserId()));

    SelectConditionStep afterDateFilter = q;

    if (request.getEndDate().isPresent()) {
      if (request.getStartDate().isPresent()) {
        afterDateFilter = q.and(EVENTS.START_TIME.between(request.getStartDate().get(), request.getEndDate().get()));
      } else {
        afterDateFilter = q.and(EVENTS.START_TIME.lessOrEqual(request.getEndDate().get()));
      }
    } else {
      if (request.getStartDate().isPresent()) {
        afterDateFilter = q.and(EVENTS.START_TIME.greaterOrEqual(request.getStartDate().get()));
      }
    }

    SelectSeekStep1 s = afterDateFilter.orderBy(EVENTS.START_TIME.asc());
    List<Events> eventPojos;

    if (request.getCount().isPresent()) {
      eventPojos = s.limit(request.getCount().get()).fetchInto(Events.class);
    } else {
      eventPojos = s.fetchInto(Events.class);
    }

    List<Event> res = listOfEventsToListOfEvent(eventPojos);
    return new GetEventsResponse(res, res.size());
  }

  @Override
  public GetEventsResponse getEventsQualified(JWTData userData) {

    Timestamp startDate = Timestamp.from(Instant.now());
    Timestamp fiveDays = Timestamp.from(Instant.now().plus(Period.ofDays(5)));
    boolean limitedToFiveDays = userData.getPrivilegeLevel().equals(PrivilegeLevel.GP);

    SelectWhereStep select = db.selectFrom(EVENTS);
    SelectConditionStep afterDateFilter;

    if (limitedToFiveDays) {
      afterDateFilter = select.where(EVENTS.START_TIME.between(startDate, fiveDays));
    } else {
      afterDateFilter = select.where(EVENTS.START_TIME.greaterOrEqual(startDate));
    }

    List<Event> res = listOfEventsToListOfEvent(afterDateFilter.fetchInto(Events.class));

    return new GetEventsResponse(res, res.size());
  }

  /**
   * Turns a list of jOOq Events DTO into one of our Event DTO.
   * @param events jOOq data objects.
   * @return List of our Event data object.
   */
  private List<Event> listOfEventsToListOfEvent(List<Events> events) {

    return events.stream().map(event -> {
      EventDetails details = new EventDetails(event.getDescription(), event.getLocation(), event.getStartTime(),
              event.getEndTime());
      Event e = new Event(event.getId(), event.getTitle(),
              eventDatabaseOperations.getSpotsLeft(event.getId()), event.getThumbnail(),
              details);
      return e;
    }).collect(Collectors.toList());

  }

  /**
   * Takes a database representation of a single event and returns the dto representation.
   */
  private SingleEventResponse eventPojoToResponse(Events event) {
    EventDetails details = new EventDetails(event.getDescription(), event.getLocation(),
        event.getStartTime(), event.getEndTime());
    return new SingleEventResponse(event.getId(), event.getTitle(),
        eventDatabaseOperations.getSpotsLeft(event.getId()), event.getCapacity(), event.getThumbnail(), details);
  }

  /**
   * Takes a dto representation of an event and returns the database record representation.
   */
  private EventsRecord eventRequestToRecord(CreateEventRequest request) {
    EventsRecord newRecord = db.newRecord(EVENTS);
    newRecord.setTitle(request.getTitle());
    newRecord.setDescription(request.getDetails().getDescription());
    newRecord.setThumbnail(request.getThumbnail());
    newRecord.setCapacity(request.getSpotsAvailable());
    newRecord.setLocation(request.getDetails().getLocation());
    newRecord.setStartTime(request.getDetails().getStart());
    newRecord.setEndTime(request.getDetails().getEnd());
    return newRecord;
  }
}
