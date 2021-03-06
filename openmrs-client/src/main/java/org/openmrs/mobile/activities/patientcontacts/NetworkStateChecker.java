package org.openmrs.mobile.activities.patientcontacts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.openmrs.mobile.activities.patientcontacts.DatabaseHelper;
import org.openmrs.mobile.activities.patientcontacts.MainActivity;
import org.openmrs.mobile.activities.patientcontacts.VolleySingleton;

import java.util.HashMap;
import java.util.Map;

public class NetworkStateChecker extends BroadcastReceiver {

    //context and database helper object
    private Context context;
    private org.openmrs.mobile.activities.patientcontacts.DatabaseHelper db;


    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        db = new org.openmrs.mobile.activities.patientcontacts.DatabaseHelper(context);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        //if there is a network
        if (activeNetwork != null) {
            //if connected to wifi or mobile data plan
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {

                //getting all the unsynced names
                Cursor cursor = db.getUnsyncedNames();
                if (cursor.moveToFirst()) {
                    do {
                        //calling the method to save the unsynced name to MySQL
                        saveName(
                                cursor.getInt(cursor.getColumnIndex(org.openmrs.mobile.activities.patientcontacts.DatabaseHelper.COLUMN_ID)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_GIVEN_NAME)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_MIDDLE_NAME)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOB)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_MOBILE)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PROXIMITY)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_GENDER)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PATIENT_INDEX_ID)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_RELATIONSHIP)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PREVIOUS_TB_TREATMENT)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CHEST_XRAY_RESULTS)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LATENT_INFECTION_RESULTS)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LBI_RESULTS)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_COUGH)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FEVER)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_WEIGHTLOSS)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NIGHTSWEATS)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CHESTXRAY)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PREVENTIVETHERAPY))
                        );
                    } while (cursor.moveToNext());
                }
            }
        }
    }
  /*
     * method taking two arguments
     * name that is to be saved and id of the name from SQLite
     * if the name is successfully sentnal
     * we will update the status as synced in SQLite
     * */
    private void saveName(final int id, final String name , final String midname ,final String dob , final String addr , final String mob, final String loc , final String gend, final String patid, final String prox,
    final String rel, final String prev,final String xry,final String lat,final String lbi, final String cou, final String fev, final String weight, final String n_sweats, final String c_xray, final String p_therapy ) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, org.openmrs.mobile.activities.patientcontacts.MainActivity.URL_SAVE_NAME,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //updating the status in sqlite
                                db.updateNameStatus(id, org.openmrs.mobile.activities.patientcontacts.MainActivity.NAME_SYNCED_WITH_SERVER);

                                //sending the broadcast to refresh the list
                                context.sendBroadcast(new Intent(MainActivity.DATA_SAVED_BROADCAST));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("given_name", name);
                params.put("middle_name", midname);
                params.put("date_of_birth", dob);
                params.put("address", addr);
                params.put("mobile", mob);
                params.put("location", loc);
                params.put("proximity", prox);
                params.put("gender", gend);
                params.put("index_id", patid);
                params.put("relationship", rel);
                params.put("previous_treatment_tb_contact", prev);
                params.put("chest_xray_result", xry);
                params.put("lantent_infection_test", lat);
                params.put("lbi_result", lbi);
                params.put("cough", cou);
                params.put("fever", fev);
                params.put("weight_loss", weight);
                params.put("night_sweats", n_sweats);
                params.put("chest_xray", c_xray);
                params.put("preventive_therapy", p_therapy);

                return params;
            }
        };

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }
}
