package org.openmrs.mobile.activities.patientcontacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    //Constants for Database name, table name, and column names
    public static final String DB_NAME = "patient_contacts";
    public static final String TABLE_NAME = "names";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_GIVEN_NAME = "given_name";
    public static final String COLUMN_MIDDLE_NAME = "middle_name";
    public static final String COLUMN_DOB = "date_of_birth";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_MOBILE = "mobile";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_PROXIMITY = "proximity";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_PATIENT_INDEX_ID = "index_id";
    public static final String COLUMN_RELATIONSHIP = "relationship";
    public static final String COLUMN_PREVIOUS_TB_TREATMENT = "previous_treatment_tb_contact";
    public static final String COLUMN_CHEST_XRAY_RESULTS = "chest_xray_result";
    public static final String COLUMN_LATENT_INFECTION_RESULTS = "lantent_infection_test";
    public static final String COLUMN_LBI_RESULTS = "lbi_result";

    public static final String COLUMN_COUGH = "cough";
    public static final String COLUMN_FEVER = "fever";
    public static final String COLUMN_WEIGHTLOSS = "weight_loss";
    public static final String COLUMN_NIGHTSWEATS = "night_sweats";
    public static final String COLUMN_CHESTXRAY = "chest_xray";
    public static final String COLUMN_PREVENTIVETHERAPY = "preventive_therapy";



    public static final String COLUMN_STATUS = "status";


    //database version
    private static final int DB_VERSION = 1;

    //Constructor
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //creating the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME
                + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_GIVEN_NAME + " VARCHAR, " +
                        COLUMN_MIDDLE_NAME + "VARCHAR," +
                        COLUMN_DOB + "VARCHAR," +
                        COLUMN_ADDRESS + "VARCHAR," +
                        COLUMN_MOBILE + "VARCHAR," +
                        COLUMN_LOCATION + "VARCHAR," +
                        COLUMN_PROXIMITY + "VARCHAR," +
                        COLUMN_GENDER + "VARCHAR," +
                        COLUMN_PATIENT_INDEX_ID + "VARCHAR," +
                        COLUMN_RELATIONSHIP+ "VARCHAR," +
                        COLUMN_PREVIOUS_TB_TREATMENT + "VARCHAR," +
                        COLUMN_CHEST_XRAY_RESULTS + "VARCHAR," +
                        COLUMN_LATENT_INFECTION_RESULTS + "VARCHAR," +
                        COLUMN_LBI_RESULTS + "VARCHAR," +
                        COLUMN_COUGH + "VARCHAR," +
                        COLUMN_FEVER + "VARCHAR," +
                        COLUMN_WEIGHTLOSS + "VARCHAR," +
                COLUMN_NIGHTSWEATS + "VARCHAR," +
                COLUMN_CHESTXRAY + "VARCHAR," +
                COLUMN_PREVENTIVETHERAPY + "VARCHAR," +

                        COLUMN_STATUS + " TINYINT);";
                        //COLUMN_COUGH + " VARCHAR);";

        db.execSQL(sql);
    }

    //upgrading the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS Persons";
        db.execSQL(sql);
        onCreate(db);
    }

    /*
     * This method is taking two arguments
     * first one is the s that is to be saved
     * second one is the status
     * 0 means the s is synced with the server
     * 1 means the s is not synced with the server
     * */
    public boolean addName(String given_name, String middle_name, String date_of_birth, String address, String mobile, String location, String proximity, String gender , String index_id ,
                           String relationship,String previous_treatment_tb_contact, String chest_xray_result ,
                           String lantent_infection_test ,String lbi_result, String cough, String fever, String weight_loss, String night_sweats, String chest_xray, String preventive_therapy, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_GIVEN_NAME, given_name);
        contentValues.put(COLUMN_MIDDLE_NAME, middle_name);
        contentValues.put(COLUMN_DOB, date_of_birth);
        contentValues.put(COLUMN_ADDRESS, address);
        contentValues.put(COLUMN_MOBILE, mobile);
        contentValues.put(COLUMN_LOCATION, location);
        contentValues.put(COLUMN_PROXIMITY, proximity);
        contentValues.put(COLUMN_GENDER, gender);
        contentValues.put(COLUMN_PATIENT_INDEX_ID, index_id);
        contentValues.put(COLUMN_RELATIONSHIP, relationship);
        contentValues.put(COLUMN_PREVIOUS_TB_TREATMENT, previous_treatment_tb_contact);
        contentValues.put(COLUMN_CHEST_XRAY_RESULTS, chest_xray_result);
        contentValues.put(COLUMN_LATENT_INFECTION_RESULTS, lantent_infection_test);
        contentValues.put(COLUMN_LBI_RESULTS, lbi_result);
        contentValues.put(COLUMN_COUGH, cough);
        contentValues.put(COLUMN_FEVER, fever);
        contentValues.put(COLUMN_WEIGHTLOSS, weight_loss);
        contentValues.put(COLUMN_NIGHTSWEATS, night_sweats);
        contentValues.put(COLUMN_CHESTXRAY, chest_xray);
        contentValues.put(COLUMN_PREVENTIVETHERAPY, preventive_therapy);

        contentValues.put(COLUMN_STATUS, status);



        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }

    /*
     * This method taking two arguments
     * first one is the id of the name for which
     * we have to update the sync status
     * and the second one is the status that will be changed
     * */
    public boolean updateNameStatus(int id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_STATUS, status);
        db.update(TABLE_NAME, contentValues, COLUMN_ID + "=" + id, null);
        db.close();
        return true;
    }

    /*
     * this method will give us all the name stored in sqlite
     * */
    public Cursor getNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " ASC;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    /*
     * this method is for getting all the unsynced name
     * so that we can sync it with database
     * */
    public Cursor getUnsyncedNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_STATUS + " = 0;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }
}