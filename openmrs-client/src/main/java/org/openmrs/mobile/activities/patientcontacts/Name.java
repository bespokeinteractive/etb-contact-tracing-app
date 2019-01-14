package org.openmrs.mobile.activities.patientcontacts;

public class Name {
    private String given_name;
    private String middle_name;
    private String date_of_birth;
    private String address;
    private String mobile;
    private String location;
    private String proximity;
    private String gender;
    private String index_id;
    private String relationship;
    private String previous_treatment_tb_contact;
    private String chest_xray_result;
    private String lantent_infection_test;
    private String lbi_result;

    private int status;

    public Name(String given_name, String middle_name, String date_of_birth,String address, String mobile, String location ,String proximity, String gender , String index_id ,
                 String relationship,
                         String previous_treatment_tb_contact,
                         String chest_xray_result,
                         String lantent_infection_test,
                         String lbi_result, int status) {
        this.given_name = given_name;
        this.middle_name = middle_name;
        this.date_of_birth = date_of_birth;
        this.address = address;
        this.mobile = mobile;
        this.location = location;
        this.proximity = proximity;
        this.gender = gender;
        this.index_id = index_id;
        this.relationship = relationship;
        this.previous_treatment_tb_contact = previous_treatment_tb_contact;
        this.chest_xray_result = chest_xray_result;
        this.lantent_infection_test = lantent_infection_test;
        this.lbi_result = lbi_result;


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
    public String getMobile() {
        return mobile;
    }
    public String getLocation() {
        return location;
    }
    public String getProximity() {
        return proximity;
    }
    public String getGender() {
        return gender;
    }
    public String getIndexId() {
        return index_id;
    }
    public String getRelationship() {
        return relationship;
    }
    public String getPreviousTreatment() {
        return previous_treatment_tb_contact;
    }
    public String getChestXrayResults() {
        return chest_xray_result;
    }
    public String getLatentInfection() {
        return lantent_infection_test;
    }
    public String getLbiResults() {
        return lbi_result;
    }

    public int getStatus() {
        return status;
    }
}