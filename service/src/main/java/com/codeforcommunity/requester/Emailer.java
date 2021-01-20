package com.codeforcommunity.requester;

import static org.jooq.generated.Tables.CONTACTS;
import static org.jooq.generated.Tables.USERS;

import com.codeforcommunity.email.EmailOperations;
import com.codeforcommunity.enums.PrivilegeLevel;
import com.codeforcommunity.propertiesLoader.PropertiesLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record3;

public class Emailer {
  private final EmailOperations emailOperations;
  private final DSLContext db;
  private final String loginUrl;
  private final String passwordResetTemplate;
  private final String PF_REQUEST_URL;

  private final String subjectWelcome = PropertiesLoader.loadProperty("email_subject_welcome");
  private final String subjectEmailChange =
      PropertiesLoader.loadProperty("email_subject_email_change");
  private final String subjectPasswordResetRequest =
      PropertiesLoader.loadProperty("email_subject_password_reset_request");
  private final String subjectPasswordResetConfirm =
      PropertiesLoader.loadProperty("email_subject_password_reset_confirm");
  private final String subjectAccountDeleted =
      PropertiesLoader.loadProperty("email_subject_account_deleted");

  public Emailer(DSLContext db) {
    String senderName = PropertiesLoader.loadProperty("email_sender_name");
    String sendEmail = PropertiesLoader.loadProperty("email_send_email");
    String sendPassword = PropertiesLoader.loadProperty("email_send_password");
    String emailHost = PropertiesLoader.loadProperty("email_host");
    int emailPort = Integer.parseInt(PropertiesLoader.loadProperty("email_port"));
    boolean shouldSendEmails =
        Boolean.parseBoolean(PropertiesLoader.loadProperty("email_should_send"));

    this.PF_REQUEST_URL =
        String.format("%s%s", PropertiesLoader.loadProperty("frontend_base_url"), "/family-requests");

    this.emailOperations =
        new EmailOperations(
            shouldSendEmails, senderName, sendEmail, sendPassword, emailHost, emailPort);

    this.loginUrl = PropertiesLoader.loadProperty("frontend_base_url");
    this.passwordResetTemplate =
        this.loginUrl + PropertiesLoader.loadProperty("frontend_password_reset_route");
    this.db = db;
  }

  /**
   * Fetches a main contact for a user and provides their email and full name to the sender
   * function.
   */
  public void sendEmailToMainContact(int userId, BiConsumer<String, String> sender) {
    Record3<String, String, String> receiver =
        db.select(CONTACTS.EMAIL, CONTACTS.FIRST_NAME, CONTACTS.LAST_NAME)
            .from(CONTACTS)
            .where(CONTACTS.USER_ID.eq(userId))
            .and(CONTACTS.IS_MAIN_CONTACT.isTrue())
            .fetchOne();
    String sendToEmail = receiver.component1();
    String sendToName = String.format("%s %s", receiver.component2(), receiver.component3());
    sender.accept(sendToEmail, sendToName);
  }

  /**
   * Fetches all contacts that receive emails for a user and provides their emails and full names to
   * the sender function.
   */
  public void sendEmailToAllContacts(int userId, BiConsumer<String, String> sender) {
    List<Record3<String, String, String>> receivers =
        db.select(CONTACTS.EMAIL, CONTACTS.FIRST_NAME, CONTACTS.LAST_NAME)
            .from(CONTACTS)
            .where(CONTACTS.USER_ID.eq(userId))
            .and(CONTACTS.SHOULD_SEND_EMAILS.isTrue())
            .fetch();
    receivers.forEach(
        record -> {
          String sendToEmail = record.component1();
          String sendToName = String.format("%s %s", record.component2(), record.component3());
          sender.accept(sendToEmail, sendToName);
        });
  }

  public void sendEmailToAllAdministrators(Consumer<String> sender) {
    List<Record1<String>> receivers =
        db.select(USERS.EMAIL)
            .from(USERS)
            .where(USERS.PRIVILEGE_LEVEL.eq(PrivilegeLevel.ADMIN))
            .fetch();
    receivers.forEach(
        record -> {
          String sendToEmail = record.component1();
          sender.accept(sendToEmail);
        });
  }

  public void sendAccountDeactivated(String sendToEmail, String sendToName) {
    String filePath = "/emails/AccountDeactivated.html";
    String subjectLine = "Your Lucy's Love Bus Account has been Deactivated";

    Map<String, String> templateValues = new HashMap<>();
    templateValues.put("name", sendToName);
    Optional<String> emailBody = emailOperations.getTemplateString(filePath, templateValues);

    emailBody.ifPresent(s -> emailOperations.sendEmail(sendToName, sendToEmail, subjectLine, s));
  }

  public void sendEmailChange(String sendToEmail, String sendToName, String newEmail) {
    String filePath = "/emails/EmailChange.html";
    String subjectLine = "Your Lucy's Love Bus Email has been Changed";

    Map<String, String> templateValues = new HashMap<>();
    templateValues.put("name", sendToName);
    templateValues.put("link", this.loginUrl);
    templateValues.put("new_email", newEmail);
    Optional<String> emailBody = emailOperations.getTemplateString(filePath, templateValues);

    emailBody.ifPresent(s -> emailOperations.sendEmail(sendToName, sendToEmail, subjectLine, s));
  }

  public void sendEventSpecificAnnouncement(
      String sendToEmail,
      String sendToName,
      String eventName,
      String announcementTitle,
      String announcementContent) {
    String filePath = "/emails/EventSpecificAnnouncement.html";
    String subjectLine = String.format("Announcement for %s", eventName);

    Map<String, String> templateValues = new HashMap<>();
    templateValues.put("name", sendToName);
    templateValues.put("link", this.loginUrl);
    templateValues.put("announcement_title", announcementTitle);
    templateValues.put("announcement_content", announcementContent);
    Optional<String> emailBody = emailOperations.getTemplateString(filePath, templateValues);

    emailBody.ifPresent(s -> emailOperations.sendEmail(sendToName, sendToEmail, subjectLine, s));
  }

  public void sendForgotPassword(String sendToEmail, String sendToName, String secretKey) {
    String filePath = "/emails/ForgotPassword.html";
    String subjectLine = "Reset your Lucy's Love Bus Password";

    String forgotPasswordLink = String.format(this.passwordResetTemplate, secretKey);
    Map<String, String> templateValues = new HashMap<>();
    templateValues.put("name", sendToName);
    templateValues.put("link", forgotPasswordLink);
    Optional<String> emailBody = emailOperations.getTemplateString(filePath, templateValues);

    emailBody.ifPresent(s -> emailOperations.sendEmail(sendToName, sendToEmail, subjectLine, s));
  }

  public void sendPasswordChange(String sendToEmail, String sendToName) {
    String filePath = "/emails/PasswordChange.html";
    String subjectLine = "Your Lucy's Love Bus Password has Changed";

    Map<String, String> templateValues = new HashMap<>();
    templateValues.put("name", sendToName);
    Optional<String> emailBody = emailOperations.getTemplateString(filePath, templateValues);

    emailBody.ifPresent(s -> emailOperations.sendEmail(sendToName, sendToEmail, subjectLine, s));
  }

  public void sendRegistrationConfirmation(
      String sendToEmail,
      String sendToName,
      String ticketNumber,
      String eventName,
      String eventDate,
      String eventTime) {
    String filePath = "/emails/RegistrationConfirmation.html";
    String subjectLine = String.format("You're Registered for %s!", eventName);

    Map<String, String> templateValues = new HashMap<>();
    templateValues.put("name", sendToName);
    templateValues.put("ticket_number", ticketNumber);
    templateValues.put("event_name", eventName);
    templateValues.put("date", eventDate);
    templateValues.put("time", eventTime);
    Optional<String> emailBody = emailOperations.getTemplateString(filePath, templateValues);

    emailBody.ifPresent(s -> emailOperations.sendEmail(sendToName, sendToEmail, subjectLine, s));
  }

  public void sendRequestApproved(String sendToEmail, String sendToName) {
    String filePath = "/emails/RequestApproved.html";
    String subjectLine = "Your Lucy's Love Bus Request has been Approved!";

    Map<String, String> templateValues = new HashMap<>();
    templateValues.put("name", sendToName);
    templateValues.put("link", this.loginUrl);
    Optional<String> emailBody = emailOperations.getTemplateString(filePath, templateValues);

    emailBody.ifPresent(s -> emailOperations.sendEmail(sendToName, sendToEmail, subjectLine, s));
  }

  public void sendRequestDenied(String sendToEmail, String sendToName) {
    String filePath = "/emails/RequestDenied.html";
    String subjectLine = "Your Lucy's Love Bus Request has been Denied";

    Map<String, String> templateValues = new HashMap<>();
    templateValues.put("name", sendToName);
    Optional<String> emailBody = emailOperations.getTemplateString(filePath, templateValues);

    emailBody.ifPresent(s -> emailOperations.sendEmail(sendToName, sendToEmail, subjectLine, s));
  }

  /**
   * TODO: This is still to be implemented with an option for an admin to send this email. Normally
   * we do not want to send an email for every site wide announcement
   */
  public void sendSiteWideAnnouncement(
      String sendToEmail, String sendToName, String announcementTitle, String announcementContent) {
    String filePath = "/emails/SiteWideAnnouncement.html";
    String subjectLine = String.format("Lucy's Love Bus: %s", announcementTitle);

    Map<String, String> templateValues = new HashMap<>();
    templateValues.put("name", sendToName);
    templateValues.put("link", this.loginUrl);
    templateValues.put("announcement_title", announcementTitle);
    templateValues.put("announcement_content", announcementContent);
    Optional<String> emailBody = emailOperations.getTemplateString(filePath, templateValues);

    emailBody.ifPresent(s -> emailOperations.sendEmail(sendToName, sendToEmail, subjectLine, s));
  }

  public void sendParticipatingFamilyRequestNotification(String sendToEmail) {
    String filePath = "/emails/RequestSubmitted.html";
    String sendToName = "Administrator";
    String subjectLine = "New Request Submitted";

    Map<String, String> templateValues = new HashMap<>();
    templateValues.put("link", this.PF_REQUEST_URL);
    Optional<String> emailBody = emailOperations.getTemplateString(filePath, templateValues);

    emailBody.ifPresent(s -> emailOperations.sendEmail(sendToName, sendToEmail, subjectLine, s));
  }
}
