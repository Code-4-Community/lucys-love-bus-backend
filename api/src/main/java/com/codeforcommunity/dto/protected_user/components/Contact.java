package com.codeforcommunity.dto.protected_user.components;

import com.codeforcommunity.dto.ApiDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/** Representation of a contact. */
public class Contact extends ApiDto {

  /** Used on PUT requests, ignored on POST */
  private Integer id;

  private String firstName;
  private String lastName;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date dateOfBirth;

  private String email;
  private String phoneNumber;
  private String allergies;
  private String diagnosis;
  private String medications;
  private String notes;
  private String pronouns;
  private Boolean shouldSendEmails;
  private String referrer;
  private String profilePicture;

  /** Creates a contact with no information (all fields null). */
  public Contact() {}

  /**
   * Creates a contact with the given information.
   *
   * @param id this contact's unique ID
   * @param firstName this contact's first name
   * @param lastName this contact's last name
   * @param dateOfBirth this contact's DOB
   * @param email this contact's email
   * @param phoneNumber this contact's phone number
   * @param allergies this contact's allergies
   * @param diagnosis this contact's diagnosis
   * @param medications this contact's medications
   * @param notes notes about this contact's
   * @param pronouns this contact's pronouns
   * @param shouldSendEmails boolean for if contact wants to be sent emails
   * @param profilePicture base64 encoded string representing the this contact's profile picture
   */
  public Contact(
      Integer id,
      String firstName,
      String lastName,
      Date dateOfBirth,
      String email,
      String phoneNumber,
      String allergies,
      String diagnosis,
      String medications,
      String notes,
      String pronouns,
      Boolean shouldSendEmails,
      String referrer,
      String profilePicture) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.dateOfBirth = dateOfBirth;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.allergies = allergies;
    this.diagnosis = diagnosis;
    this.medications = medications;
    this.notes = notes;
    this.pronouns = pronouns;
    this.shouldSendEmails = shouldSendEmails;
    this.referrer = referrer;
    this.profilePicture = profilePicture;
  }

  /**
   * Verify this Contact is valid by validating each of its fields.
   *
   * @param fieldPrefix A string to prefix each field with (for use if this is a sub-field). Should
   *     be of the form "OBJECT.".
   * @return a list of strings with each string indicating one field that is invalid
   */
  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "contact.";
    List<String> fields = new ArrayList<>();
    if (emailInvalid(email)) {
      fields.add(fieldName + "email");
    }
    if (isEmpty(firstName)) {
      fields.add(fieldName + "first_name");
    }
    if (isEmpty(lastName)) {
      fields.add(fieldName + "last_name");
    }
    if (dateOfBirth != null && dateOfBirth.after(new java.util.Date())) {
      fields.add(fieldName + "date_of_birth");
    }
    return fields;
  }

  /**
   * Returns this contact's ID.
   *
   * @return this contact's ID
   */
  public Integer getId() {
    return id;
  }

  /**
   * Updates this contact's ID
   *
   * @param id the new ID
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Returns this contact's first name
   *
   * @return this contact's first name
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Updates this contact's first name
   *
   * @param firstName the new first name
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Returns this contact's last name
   *
   * @return this contact's last name
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Updates this contact's last name
   *
   * @param lastName the new last name
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Returns this contact's DOB
   *
   * @return this contact's DOB
   */
  public Date getDateOfBirth() {
    return dateOfBirth;
  }

  /**
   * Updates this contact's DOB
   *
   * @param dateOfBirth the new DOB
   */
  public void setDateOfBirth(Date dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  /**
   * Returns this contact's email
   *
   * @return this contact's email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Updates this contact's email
   *
   * @param email the new email
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Returns this contact's phone number
   *
   * @return this contact's phone number
   */
  public String getPhoneNumber() {
    return phoneNumber;
  }

  /**
   * Updates this contact's phone number
   *
   * @param phoneNumber the new phone number
   */
  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  /**
   * Returns this contact's allergies
   *
   * @return this contact's allergies
   */
  public String getAllergies() {
    return allergies;
  }

  /**
   * Updates this contact's allergies
   *
   * @param allergies the new allergies
   */
  public void setAllergies(String allergies) {
    this.allergies = allergies;
  }

  /**
   * Returns this contact's diagnosis
   *
   * @return this contact's diagnosis
   */
  public String getDiagnosis() {
    return diagnosis;
  }

  /**
   * Updates this contact's diagnosis
   *
   * @param diagnosis the new diagnosis
   */
  public void setDiagnosis(String diagnosis) {
    this.diagnosis = diagnosis;
  }

  /**
   * Returns this contact's medications
   *
   * @return this contact's medications
   */
  public String getMedications() {
    return this.medications;
  }

  /**
   * Updates this contact's medications
   *
   * @param medications the new medications
   */
  public void setMedications(String medications) {
    this.medications = medications;
  }

  /**
   * Returns the notes about this contact
   *
   * @return the notes about this contact
   */
  public String getNotes() {
    return notes;
  }

  /**
   * Updates the notes about this contact
   *
   * @param notes the new notes
   */
  public void setNotes(String notes) {
    this.notes = notes;
  }

  /**
   * Returns this contact's pronouns
   *
   * @return this contact's pronouns
   */
  public String getPronouns() {
    return pronouns;
  }

  /**
   * Updates this contact's pronouns
   *
   * @param pronouns the new pronouns
   */
  public void setPronouns(String pronouns) {
    this.pronouns = pronouns;
  }

  /**
   * Returns whether this contact should receive emails
   *
   * @return whether this contact should receive emails
   */
  public Boolean getShouldSendEmails() {
    return shouldSendEmails;
  }

  /**
   * Update whether this contact should receive emails
   *
   * @param shouldSendEmails boolean representing the above
   */
  public void setShouldSendEmails(Boolean shouldSendEmails) {
    this.shouldSendEmails = shouldSendEmails;
  }

  /**
   * Returns this Contact's referrer.
   *
   * @return this Contact's referrer.
   */
  public String getReferrer() {
    return this.referrer;
  }

  /**
   * Sets this Contact's referrer to the given one.
   *
   * @param referrer the new referrer.
   */
  public void setReferrer(String referrer) {
    this.referrer = referrer;
  }

  /**
   * Returns this Contact's profilePictureBLOB.
   *
   * @return this Contact's profilePictureBLOB.
   */
  public String getProfilePicture() {
    return this.profilePicture;
  }

  /**
   * Sets this Contact's profilePictureBLOB to the given one.
   *
   * @param profilePictureBLOB the new profilePictureBLOB.
   */
  public void setProfilePicture(String profilePictureBLOB) {
    this.profilePicture = profilePictureBLOB;
  }
}
