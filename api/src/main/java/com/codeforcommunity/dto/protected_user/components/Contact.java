package com.codeforcommunity.dto.protected_user.components;

import java.sql.Timestamp;

public class Contact {

    private String firstName;
    private String lastName;
    private Timestamp dob;
    private String email;
    private String phoneNumber;
    private String allergies;
    private String diagnosis;
    private String medications;
    private String notes;
    private String pronouns;
    private boolean shouldSendEmail;

    public Contact() {}

    public Contact(String firstName, String lastName, Timestamp dob, String email, String phoneNumber,
                   String allergies, String diagnosis, String medication, String notes, String pronouns, boolean shouldSendEmail) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.allergies = allergies;
        this.diagnosis = diagnosis;
        this.medications = medication;
        this.notes = notes;
        this.pronouns = pronouns;
        this.shouldSendEmail = shouldSendEmail;
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

    public Timestamp getDob() {
        return dob;
    }

    public void setDob(Timestamp dob) {
        this.dob = dob;
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

    public boolean getShouldSendEmail() {
        return shouldSendEmail;
    }

    public void setShouldSendEmail(boolean shouldSendEmail) {
        this.shouldSendEmail = shouldSendEmail;
    }
}
