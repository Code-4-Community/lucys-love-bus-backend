package com.codeforcommunity.dto.protected_user.components;

import java.sql.Timestamp;

public class Child {

    private String firstName;
    private String lastName;
    private Timestamp dob;
    private String pronouns;
    private String schoolYear;
    private String school;
    private String allergies;
    private String diagnosis;
    private String medications;
    private String notes;

    public Child() {}

    public Child(String firstName, String lastName, Timestamp dob, String pronouns, String schoolYear, String school, String allergies, String diagnosis, String medications, String notes) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.pronouns = pronouns;
        this.schoolYear = schoolYear;
        this.school = school;
        this.allergies = allergies;
        this.diagnosis = diagnosis;
        this.medications = medications;
        this.notes = notes;
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