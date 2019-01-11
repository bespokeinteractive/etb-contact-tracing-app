package org.openmrs.mobile.activities.patientcontacts;

public class Name {
    private String given_name;
    private String middle_name;
    private String date_of_birth;
    private String address;

    private int status;

    public Name(String given_name, String middle_name, String date_of_birth,String address, int status) {
        this.given_name = given_name;
        this.middle_name = middle_name;
        this.date_of_birth = date_of_birth;
        this.address = address;

        this.status = status;
    }

    public String getName() {
        return given_name;
    }

    public String getMiddleName() {
        return middle_name;
    }
    public String getDateOfBirth() {
        return date_of_birth;
    }
    public String getAddress() {
        return address;
    }

    public int getStatus() {
        return status;
    }
}