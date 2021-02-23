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
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record3;

public class Emailer {
  private final EmailOperations emailOperations;
  private final DSLContext db;
  private final String MAIN_PAGE_URL;
  private final String FORGOT_PASSWORD_URL_TEMPLATE;
  private final String VERIFY_EMAIL_URL_TEMPLATE;
  private final String PF_REQUEST_URL;

  public Emailer(DSLContext db) {
    Properties emailProperties = PropertiesLoader.getEmailerProperties();
    String senderName = emailProperties.getProperty("senderName");
    String sendEmail = emailProperties.getProperty("sendEmail");
    String sendPassword = emailProperties.getProperty("sendPassword");
    String emailHost = emailProperties.getProperty("emailHost");
    int emailPort = Integer.parseInt(emailProperties.getProperty("emailPort"));
    boolean shouldSendEmails =
        Boolean.parseBoolean(emailProperties.getProperty("shouldSendEmails", "false"));

    Properties frontendProperties = PropertiesLoader.getFrontendProperties();
    this.MAIN_PAGE_URL =
        String.format(
            "%s%s",
            frontendProperties.getProperty("domain"), frontendProperties.getProperty("main_route"));
    this.FORGOT_PASSWORD_URL_TEMPLATE =
        String.format(
            "%s%s",
            frontendProperties.getProperty("domain"),
            frontendProperties.getProperty("forgot_password_route"));

    this.VERIFY_EMAIL_URL_TEMPLATE =
        String.format(
            "%s%s",
            frontendProperties.getProperty("domain"),
            frontendProperties.getProperty("verify_email_route"));

    this.PF_REQUEST_URL =
        String.format("%s%s", frontendProperties.getProperty("domain"), "/family-requests");

    this.db = db;
    this.emailOperations =
        new EmailOperations(
            shouldSendEmails, senderName, sendEmail, sendPassword, emailHost, emailPort);
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
    templateValues.put("link", MAIN_PAGE_URL);
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
    templateValues.put("link", MAIN_PAGE_URL);
    templateValues.put("announcement_title", announcementTitle);
    templateValues.put("announcement_content", announcementContent);
    Optional<String> emailBody = emailOperations.getTemplateString(filePath, templateValues);

    emailBody.ifPresent(s -> emailOperations.sendEmail(sendToName, sendToEmail, subjectLine, s));
  }

  public void sendForgotPassword(String sendToEmail, String sendToName, String secretKey) {
    String filePath = "/emails/ForgotPassword.html";
    String subjectLine = "Reset your Lucy's Love Bus Password";

    String forgotPasswordLink = String.format(FORGOT_PASSWORD_URL_TEMPLATE, secretKey);
    Map<String, String> templateValues = new HashMap<>();
    templateValues.put("name", sendToName);
    templateValues.put("link", forgotPasswordLink);
    Optional<String> emailBody = emailOperations.getTemplateString(filePath, templateValues);

    emailBody.ifPresent(s -> emailOperations.sendEmail(sendToName, sendToEmail, subjectLine, s));
  }

  public void sendEmailVerification(String sendToEmail, String sendToName, String secretKey) {
    String filePath = "/emails/SignupVerification.html";
    String subjectLine = "Verify your email";

    String emailVerificationLink = String.format(VERIFY_EMAIL_URL_TEMPLATE, secretKey);
    Map<String, String> templateValues = new HashMap<>();
    templateValues.put("name", sendToName);
    templateValues.put("link", emailVerificationLink);
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
    templateValues.put("link", MAIN_PAGE_URL);
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
    templateValues.put("link", MAIN_PAGE_URL);
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
