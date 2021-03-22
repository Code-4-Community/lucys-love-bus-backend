package com.codeforcommunity.dto.protected_user.components;

import com.codeforcommunity.dto.ApiDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/** Representation of a child in the LLB Program. */
public class Child extends ApiDto {

  /** Used on PUT requests, ignored on POST */
  private Integer id;

  private String firstName;
  private String lastName;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date dateOfBirth;

  private String pronouns;
  private String schoolYear;
  private String school;
  private String allergies;
  private String diagnosis;
  private String medications;
  private String notes;
  private String profilePicture;

  /** Creates a child with no information (all fields set to null). */
  public Child() {}

  /**
   * Creates a child with the given information.
   *
   * @param id this child's unique ID
   * @param firstName this child's first name
   * @param lastName this child's last name
   * @param dateOfBirth this child's DOB
   * @param pronouns this child's pronouns
   * @param schoolYear what grade this child is in (please update this if incorrect)
   * @param school the name of this child's school
   * @param allergies this child's allergies
   * @param diagnosis this child's diagnosis
   * @param medications medications this child is on
   * @param notes notes about this child
   * @param profilePicture base64 encoded string representing the this contact's profile picture
   */
  public Child(
      Integer id,
      String firstName,
      String lastName,
      Date dateOfBirth,
      String pronouns,
      String schoolYear,
      String school,
      String allergies,
      String diagnosis,
      String medications,
      String notes,
      String profilePicture) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.dateOfBirth = dateOfBirth;
    this.pronouns = pronouns;
    this.schoolYear = schoolYear;
    this.school = school;
    this.allergies = allergies;
    this.diagnosis = diagnosis;
    this.medications = medications;
    this.notes = notes;
    this.profilePicture = profilePicture;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "child.";
    List<String> fields = new ArrayList<>();
    if (isEmpty(firstName)) {
      fields.add(fieldName + "first_name");
    }
    if (isEmpty(lastName)) {
      fields.add(fieldName + "last_name");
    }
    if (dateOfBirth == null || dateOfBirth.after(new java.util.Date())) {
      fields.add(fieldName + "date_of_birth");
    }
    if (isEmpty(pronouns)) {
      fields.add(fieldName + "pronouns");
    }
    if (isEmpty(schoolYear)) {
      fields.add(fieldName + "school_year");
    }
    if (isEmpty(school)) {
      fields.add(fieldName + "school");
    }
    return fields;
  }

  /**
   * Returns this child's ID
   *
   * @return the above
   */
  public Integer getId() {
    return id;
  }

  /**
   * Updates this child's ID
   *
   * @param id the new ID number
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Returns this child's ID
   *
   * @return the above
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Updates this child's first name
   *
   * @param firstName the new first name
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Gets this child's last name
   *
   * @return the above
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Updates this child's last name
   *
   * @param lastName the new last name
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Returns this child's DOB
   *
   * @return the above
   */
  public Date getDateOfBirth() {
    return dateOfBirth;
  }

  /**
   * Updates this child's DOB
   *
   * @param dateOfBirth the new DOB
   */
  public void setDateOfBirth(Date dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  /**
   * Returns this child's pronouns
   *
   * @return the above
   */
  public String getPronouns() {
    return pronouns;
  }

  /**
   * Updates this child's pronouns
   *
   * @param pronouns the new pronouns
   */
  public void setPronouns(String pronouns) {
    this.pronouns = pronouns;
  }

  /**
   * Returns this child's school year
   *
   * @return the above
   */
  public String getSchoolYear() {
    return schoolYear;
  }

  /**
   * Updates this child's school year
   *
   * @param schoolYear the new school year
   */
  public void setSchoolYear(String schoolYear) {
    this.schoolYear = schoolYear;
  }

  /**
   * Returns the name of this child's school
   *
   * @return the above
   */
  public String getSchool() {
    return school;
  }

  /**
   * Updates the name of this child's school
   *
   * @param school the school's new name
   */
  public void setSchool(String school) {
    this.school = school;
  }

  /**
   * Returns this child's allergies
   *
   * @return the above
   */
  public String getAllergies() {
    return allergies;
  }

  /**
   * Updates this child's allergies
   *
   * @param allergies the new allergies
   */
  public void setAllergies(String allergies) {
    this.allergies = allergies;
  }

  /**
   * Returns this child's diagnosis
   *
   * @return the above
   */
  public String getDiagnosis() {
    return diagnosis;
  }

  /**
   * Updates this child's diagnosis
   *
   * @param diagnosis the new diagnosis
   */
  public void setDiagnosis(String diagnosis) {
    this.diagnosis = diagnosis;
  }

  /**
   * Returns this child's medications
   *
   * @return the above
   */
  public String getMedications() {
    return medications;
  }

  /**
   * Updates this child's medications
   *
   * @param medications the new medications
   */
  public void setMedications(String medications) {
    this.medications = medications;
  }

  /**
   * Returns the notes about this child
   *
   * @return the above
   */
  public String getNotes() {
    return notes;
  }

  /**
   * Updates the notes about this child
   *
   * @param notes the new notes
   */
  public void setNotes(String notes) {
    this.notes = notes;
  }

  /**
   * Returns this Contact's profilePicture.
   *
   * @return this Contact's profilePicture.
   */
  public String getProfilePicture() {
    return profilePicture;
  }

  /**
   * Sets this Contact's profilePicture to the given one.
   *
   * @param profilePicture the new profilePicture.
   */
  public void setProfilePicture(String profilePicture) {
    this.profilePicture = profilePicture;
  }
}
