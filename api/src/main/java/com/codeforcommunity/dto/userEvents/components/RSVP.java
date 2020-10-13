package com.codeforcommunity.dto.userEvents.components;

import java.sql.Timestamp;

/** A class to represent an event RSVP */
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

  /**
   * Gets the user id associated with the RSVP
   *
   * @return the user id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Gets the ticket quantity of the RSVP
   *
   * @return the ticket quantity
   */
  public String getTicketQuantity() {
    return ticketQuantity;
  }

  /**
   * Gets the email of the RSVP
   *
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Gets the first name associated with the RSVP
   *
   * @return the first name
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Gets the last name associated with the RSVP
   *
   * @return the last name
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Gets the privilege level of the RSVP
   *
   * @return the privilege level
   */
  public String getPrivilegeLevel() {
    return privilegeLevel;
  }

  /**
   * Gets the street address of the RSVP
   *
   * @return the address
   */
  public String getAddress() {
    return address;
  }

  /**
   * Gets the city of the RSVP
   *
   * @return the city
   */
  public String getCity() {
    return city;
  }

  /**
   * Gets the state of the RSVP
   *
   * @return the state
   */
  public String getState() {
    return state;
  }

  /**
   * Gets the zipcode of the RSVP
   *
   * @return the zipcode
   */
  public String getZipcode() {
    return zipcode;
  }

  /**
   * Gets the string value of whether the RSVP is of a child
   *
   * @return "true" if child, "false" if not
   */
  public String getIsChild() {
    return isChild;
  }

  /**
   * Gets the string value of whether the RSVP is the main contact if existing contact
   *
   * @return "true" if main contact, "false" if not
   */
  public String getIsMainContact() {
    return isMainContact;
  }

  /**
   * Gets the date of birth of the RSVP
   *
   * @return the DOB
   */
  public Timestamp getDateOfBirth() {
    return dateOfBirth;
  }

  /**
   * Gets the phone number of the RSVP
   *
   * @return the phone number as a string
   */
  public String getPhoneNumber() {
    return phoneNumber;
  }

  /**
   * Gets the pronouns of the RSVP
   *
   * @return the pronouns
   */
  public String getPronouns() {
    return pronouns;
  }

  /**
   * Gets the allergies of the RSVP
   *
   * @return the allergies
   */
  public String getAllergies() {
    return allergies;
  }

  /**
   * Gets the diagnosis of the RSVP
   *
   * @return the diagnosis
   */
  public String getDiagnosis() {
    return diagnosis;
  }

  /**
   * Gets the medication of the RSVP
   *
   * @return the medication
   */
  public String getMedications() {
    return medications;
  }

  /**
   * Gets the notes from the RSVP
   *
   * @return the notes
   */
  public String getNotes() {
    return notes;
  }

  /**
   * Gets the school year of the child in the RSVP
   *
   * @return the school year
   */
  public String getSchoolYear() {
    return schoolYear;
  }

  /**
   * Gets the school that the child attends from the RSVP
   *
   * @return the school name
   */
  public String getSchool() {
    return school;
  }

  /**
   * Converts the fields into a string to be used as a header with all field names
   *
   * @return the string of fields
   */
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

  /**
   * Converts the information from the RSVP into a row entry
   *
   * @return the string of RSVP information as a rows
   */
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
