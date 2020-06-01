package com.codeforcommunity.dto.protected_user.components;

import com.codeforcommunity.api.ApiDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

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

  public Child() {}

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
      String notes) {
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

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Date getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(Date dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getPronouns() {
    return pronouns;
  }

  public void setPronouns(String pronouns) {
    this.pronouns = pronouns;
  }

  public String getSchoolYear() {
    return schoolYear;
  }

  public void setSchoolYear(String schoolYear) {
    this.schoolYear = schoolYear;
  }

  public String getSchool() {
    return school;
  }

  public void setSchool(String school) {
    this.school = school;
  }

  public String getAllergies() {
    return allergies;
  }

  public void setAllergies(String allergies) {
    this.allergies = allergies;
  }

  public String getDiagnosis() {
    return diagnosis;
  }

  public void setDiagnosis(String diagnosis) {
    this.diagnosis = diagnosis;
  }

  public String getMedications() {
    return medications;
  }

  public void setMedications(String medications) {
    this.medications = medications;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
