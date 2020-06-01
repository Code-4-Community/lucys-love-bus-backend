package com.codeforcommunity.dto.protected_user.components;

import com.codeforcommunity.api.ApiDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

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

  public Contact() {}

  public Contact(
      Integer id,
      String firstName,
      String lastName,
      Date dateOfBirth,
      String email,
      String phoneNumber,
      String allergies,
      String diagnosis,
      String medication,
      String notes,
      String pronouns,
      Boolean shouldSendEmails) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.dateOfBirth = dateOfBirth;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.allergies = allergies;
    this.diagnosis = diagnosis;
    this.medications = medication;
    this.notes = notes;
    this.pronouns = pronouns;
    this.shouldSendEmails = shouldSendEmails;
  }

  @Override
  public List<String> validateFields(String fieldPrefix) {
    List<String> fields = new ArrayList<>();
    if (emailInvalid(email)) {
      fields.add(fieldPrefix + "email");
    }
    if (isEmpty(firstName)) {
      fields.add(fieldPrefix + "first_name");
    }
    if (isEmpty(lastName)) {
      fields.add(fieldPrefix + "last_name");
    }
    if (shouldSendEmails == null) {
      fields.add(fieldPrefix + "should_send_emails");
    }
    if (dateOfBirth != null && dateOfBirth.after(new java.util.Date())) {
      fields.add(fieldPrefix + "date_of_birth");
    }
    return fields;
  }

  @Override
  public String fieldName() {
    return "contact.";
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
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

  public String getPronouns() {
    return pronouns;
  }

  public void setPronouns(String pronouns) {
    this.pronouns = pronouns;
  }

  public Boolean getShouldSendEmails() {
    return shouldSendEmails;
  }

  public void setShouldSendEmails(Boolean shouldSendEmails) {
    this.shouldSendEmails = shouldSendEmails;
  }
}
