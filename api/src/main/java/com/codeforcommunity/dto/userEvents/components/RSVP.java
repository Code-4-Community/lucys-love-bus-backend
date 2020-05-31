package com.codeforcommunity.dto.userEvents.components;

import java.sql.Timestamp;

public class RSVP {
  private final String userId;
  private final String ticketQuantity;
  private final String email;
  private final String firstName;
  private final String lastName;
  private final String privilegeLevel;
  private final String address;
  private final String city;
  private final String state;
  private final String zipcode;
  private final String isChild;
  private final String isMainContact;
  private final Timestamp dateOfBirth;
  private final String phoneNumber;
  private final String pronouns;
  private final String allergies;
  private final String diagnosis;
  private final String medications;
  private final String notes;
  private final String schoolYear;
  private final String school;

  /** RSVP instance for USERS. */
  public RSVP(
      int userId,
      int ticketQuantity,
      String email,
      String privilegeLevel,
      String address,
      String city,
      String state,
      String zipcode) {
    this.userId = String.valueOf(userId);
    this.ticketQuantity = String.valueOf(ticketQuantity);
    this.email = email;
    this.firstName = "";
    this.lastName = "";
    this.privilegeLevel = privilegeLevel != null ? privilegeLevel : "";
    this.address = address != null ? address : "";
    this.city = city != null ? city : "";
    this.state = state != null ? state : "";
    this.zipcode = zipcode != null ? zipcode : "";
    this.isChild = "";
    this.isMainContact = "";
    this.dateOfBirth = null;
    this.phoneNumber = "";
    this.pronouns = "";
    this.allergies = "";
    this.diagnosis = "";
    this.medications = "";
    this.notes = "";
    this.schoolYear = "";
    this.school = "";
  }

  /** RSVP instance for CONTACTS. */
  public RSVP(
      int userId,
      String email,
      String firstName,
      String lastName,
      boolean isMainContact,
      Timestamp dateOfBirth,
      String phoneNumber,
      String pronouns,
      String allergies,
      String diagnosis,
      String medications,
      String notes) {
    this.userId = String.valueOf(userId);
    this.ticketQuantity = "";
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.privilegeLevel = "";
    this.address = "";
    this.city = "";
    this.state = "";
    this.zipcode = "";
    this.isChild = String.valueOf(false);
    this.isMainContact = String.valueOf(isMainContact);
    this.dateOfBirth = dateOfBirth;
    this.phoneNumber = phoneNumber != null ? phoneNumber : "";
    this.pronouns = pronouns != null ? pronouns : "";
    this.allergies = allergies != null ? allergies : "";
    this.diagnosis = diagnosis != null ? diagnosis : "";
    this.medications = medications != null ? medications : "";
    this.notes = notes != null ? notes : "";
    this.schoolYear = "";
    this.school = "";
  }

  /** RSVP instance for CHILDREN. */
  public RSVP(
      int userId,
      String firstName,
      String lastName,
      Timestamp dateOfBirth,
      String pronouns,
      String allergies,
      String diagnosis,
      String medications,
      String notes,
      String schoolYear,
      String school) {
    this.userId = String.valueOf(userId);
    this.ticketQuantity = "";
    this.email = "";
    this.firstName = firstName;
    this.lastName = lastName;
    this.privilegeLevel = "";
    this.address = "";
    this.city = "";
    this.state = "";
    this.zipcode = "";
    this.isChild = String.valueOf(true);
    this.isMainContact = "";
    this.dateOfBirth = dateOfBirth;
    this.phoneNumber = "";
    this.pronouns = pronouns != null ? pronouns : "";
    this.allergies = allergies != null ? allergies : "";
    this.diagnosis = diagnosis != null ? diagnosis : "";
    this.medications = medications != null ? medications : "";
    this.notes = notes != null ? notes : "";
    this.schoolYear = schoolYear != null ? schoolYear : "";
    this.school = school != null ? school : "";
  }

  public String getUserId() {
    return userId;
  }

  public String getTicketQuantity() {
    return ticketQuantity;
  }

  public String getEmail() {
    return email;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getPrivilegeLevel() {
    return privilegeLevel;
  }

  public String getAddress() {
    return address;
  }

  public String getCity() {
    return city;
  }

  public String getState() {
    return state;
  }

  public String getZipcode() {
    return zipcode;
  }

  public String getIsChild() {
    return isChild;
  }

  public String getIsMainContact() {
    return isMainContact;
  }

  public Timestamp getDateOfBirth() {
    return dateOfBirth;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getPronouns() {
    return pronouns;
  }

  public String getAllergies() {
    return allergies;
  }

  public String getDiagnosis() {
    return diagnosis;
  }

  public String getMedications() {
    return medications;
  }

  public String getNotes() {
    return notes;
  }

  public String getSchoolYear() {
    return schoolYear;
  }

  public String getSchool() {
    return school;
  }

  public static String toHeaderCSV() {
    return "User ID,"
        + "Ticket Quantity,"
        + "Privilege Level,"
        + "Is Main Contact,"
        + "Is Child,"
        + "Email,"
        + "First Name,"
        + "Last Name,"
        + "Address,"
        + "City,"
        + "State,"
        + "Zip Code,"
        + "Date of Birth,"
        + "Phone Number,"
        + "Pronouns,"
        + "Allergies,"
        + "Diagnosis,"
        + "Medications,"
        + "Notes,"
        + "School Year,"
        + "School\n";
  }

  public String toRowCSV() {
    return (this.privilegeLevel.isEmpty() ? "" : this.userId)
        + ","
        + this.ticketQuantity
        + ","
        + this.privilegeLevel
        + ","
        + this.isMainContact
        + ","
        + this.isChild
        + ","
        + this.email
        + ","
        + this.firstName
        + ","
        + this.lastName
        + ","
        + this.address
        + ","
        + this.city
        + ","
        + this.state
        + ","
        + this.zipcode
        + ","
        + (this.dateOfBirth != null ? this.dateOfBirth : "")
        + ","
        + this.phoneNumber
        + ","
        + this.pronouns
        + ","
        + this.allergies
        + ","
        + this.diagnosis
        + ","
        + this.medications
        + ","
        + this.notes
        + ","
        + this.schoolYear
        + ","
        + this.school
        + "\n";
  }
}
