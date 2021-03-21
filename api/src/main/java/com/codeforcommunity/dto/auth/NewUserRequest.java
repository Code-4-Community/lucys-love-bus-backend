package com.codeforcommunity.dto.auth;

import com.codeforcommunity.dto.ApiDto;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class NewUserRequest extends ApiDto {

  private String email;
  private String password;
  private String firstName;
  private String lastName;
  private AddressData location;
  private String phoneNumber;
  private String allergies;
  private String referrer;
  private Boolean photoRelease;
  private String medication;
  private String notes;
  private String pronouns;
  private String profilePicture;
  private Date dateOfBirth;

  public NewUserRequest(
      String email,
      String password,
      String firstName,
      String lastName,
      AddressData location,
      String phoneNumber,
      String allergies,
      String referrer,
      Boolean photoRelease,
      String medication,
      String notes,
      String pronouns,
      String profilePicture,
      Date dateOfBirth) {
    this.email = email;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
    this.location = location;
    this.phoneNumber = phoneNumber;
    this.allergies = allergies;
    this.referrer = referrer;
    this.photoRelease = photoRelease;
    this.medication = medication;
    this.notes = notes;
    this.pronouns = pronouns;
    this.profilePicture = profilePicture;
    this.dateOfBirth = dateOfBirth;
  }

  private NewUserRequest() {}

  @Override
  public List<String> validateFields(String fieldPrefix) {
    String fieldName = fieldPrefix + "new_user_request.";
    List<String> fields = new ArrayList<>();
    if (emailInvalid(email)) {
      fields.add(fieldName + "email");
    }
    if (passwordInvalid(password)) {
      fields.add(fieldName + "password");
    }
    if (isEmpty(firstName)) {
      fields.add(fieldName + "first_name");
    }
    if (isEmpty(lastName)) {
      fields.add(fieldName + "last_name");
    }
    if (photoRelease == null) {
      fields.add(fieldName + "photoRelease");
    }
    if (location == null) {
      fields.add(fieldName + "location");
    } else {
      fields.addAll(location.validateFields(fieldName));
    }
    return fields;
  }

  /**
   * Retrieves the email field.
   *
   * @return the String email field.
   */
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
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

  /**
   * Retrieves the location field.
   *
   * @return the AddressData location field.
   */
  public AddressData getLocation() {
    return location;
  }

  /**
   * Sets this NewUserRequest's location field to the provided location.
   *
   * @param location the value to set this location field to.
   */
  public void setLocation(AddressData location) {
    this.location = location;
  }

  /**
   * Retrieves the phoneNumber field.
   *
   * @return the String phoneNumber field.
   */
  public String getPhoneNumber() {
    return phoneNumber;
  }

  /**
   * Sets this NewUserRequest's phoneNumber field to the provided phoneNumber.
   *
   * @param phoneNumber the value to set this phoneNumber field to.
   */
  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  /**
   * Retrieves the allergies field.
   *
   * @return the String allergies field.
   */
  public String getAllergies() {
    return allergies;
  }

  /**
   * Sets this NewUserRequest's allergies field to the provided allergies.
   *
   * @param allergies the value to set this allergies field to.
   */
  public void setAllergies(String allergies) {
    this.allergies = allergies;
  }

  /**
   * Retrieves the referrer field.
   *
   * @return the String referrer field.
   */
  public String getReferrer() {
    return this.referrer;
  }

  /**
   * Sets this NewUserRequest's referrer field to the provided referrer.
   *
   * @param referrer the value to set this referrer field to.
   */
  public void setReferrer(String referrer) {
    this.referrer = referrer;
  }

  public boolean getPhotoRelease() {
    return this.photoRelease;
  }

  public void setPhotoRelease(Boolean photoRelease) {
    this.photoRelease = photoRelease;
  }

  public String getMedication() {
    return medication;
  }

  public void setMedication(String medication) {
    this.medication = medication;
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

  public String getProfilePicture() {
    return profilePicture;
  }

  public void setProfilePicture(String profilePicture) {
    this.profilePicture = profilePicture;
  }

  public Date getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(Date dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }
}
