package org.openmrs.mobile.activities.patientcontacts;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.patientcontacts.DatabaseHelper;
import org.openmrs.mobile.activities.patientcontacts.Name;
import org.openmrs.mobile.activities.patientcontacts.NameAdapter;
import org.openmrs.mobile.activities.patientcontacts.NetworkStateChecker;
import org.openmrs.mobile.activities.patientcontacts.VolleySingleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class lists extends AppCompatActivity {


    public static final String URL_SAVE_NAME = "http://192.168.0.104/SyncData/saveName.php";

    //database helper object
    private org.openmrs.mobile.activities.patientcontacts.DatabaseHelper db;

    //View objects
    private Button buttonSave;
    private EditText editTextName;
    private ListView listViewNames;
    private EditText editTextmiddlename;
    private Button buttonOpen;
    public EditText editTextDob;
    public EditText editTextAddress;
    public EditText editTextMobile;
    public EditText editTextLocation;
    public EditText editTextProximity;
    public RadioGroup radioSexGroup;
    public RadioButton editRadioBtngender;
    public RadioButton male;
    public RadioButton female;
    private TextView patientDetailsNames;


    //List to store all the names
    private List<Name> names;

    //1 means data is synced and 0 means data is not synced
    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;

    //a broadcast to know weather the data is synced or not
    public static final String DATA_SAVED_BROADCAST = "net.simplifiedcoding.datasaved";

    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;

    //adapterobject for list view
    private NameAdapter nameAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lists);
        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //initializing views and objects
        db = new org.openmrs.mobile.activities.patientcontacts.DatabaseHelper(this);
        names = new ArrayList<>();

        buttonSave = (Button) findViewById(R.id.buttonSave);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextmiddlename = (EditText) findViewById(R.id.editTextmiddlename);
        editTextDob = (EditText) findViewById(R.id.editTextDob);
        editTextAddress = (EditText) findViewById(R.id.editTextAddress);
        editTextMobile = (EditText) findViewById(R.id.editTextMobile);
        editTextLocation = (EditText) findViewById(R.id.editTextLocation);
        editTextProximity = (EditText) findViewById(R.id.editTextProximity);
        radioSexGroup = (RadioGroup) findViewById(R.id.radiogender);
        male = (RadioButton) findViewById(R.id.male);
        female = (RadioButton) findViewById(R.id.female);

        int selectedId = radioSexGroup.getCheckedRadioButtonId();
        editRadioBtngender = (RadioButton) findViewById(selectedId);
        listViewNames = (ListView) findViewById(R.id.listViewNames);

        patientDetailsNames = (TextView) findViewById(R.id.patientDetailsNames);
        patientDetailsNames.setText(getIntent().getStringExtra("idnt"));


        buttonOpen = (Button) findViewById(R.id.buttonOpen);


        //calling the method to load all the stored names
        loadNames();

        //the broadcast receiver to update sync status
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //loading the names again
                loadNames();
            }
        };

        //registering the broadcast receiver to update sync status
        registerReceiver(broadcastReceiver, new IntentFilter(DATA_SAVED_BROADCAST));
    }

    /*
     * this method will
     * load the names from the database
     * with updated sync status
     * */
    private void loadNames() {
        names.clear();
        Cursor cursor = db.getNames();
        if (cursor.moveToFirst()) {
            do {
                Name name = new Name(
                        cursor.getString(cursor.getColumnIndex(org.openmrs.mobile.activities.patientcontacts.DatabaseHelper.COLUMN_GIVEN_NAME)),
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
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS))

                );
                names.add(name);
            } while (cursor.moveToNext());
        }

        nameAdapter = new NameAdapter(this, R.layout.names, names);
        listViewNames.setAdapter(nameAdapter);
    }

    /*
     * this method will simply refresh the list
     * */
    private void refreshList() {
        nameAdapter.notifyDataSetChanged();
    }

    /*
     * this method is saving the name to ther server
     * */
    private void saveNameToServer() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving Name...");
        progressDialog.show();

        final String name = editTextName.getText().toString().trim();
        final String midname = editTextmiddlename.getText().toString().trim();
        final String dob = editTextDob.getText().toString().trim();
        final String addr = editTextAddress.getText().toString().trim();
        final String mob = editTextMobile.getText().toString().trim();
        final String loc = editTextLocation.getText().toString().trim();
        final String prox = editTextProximity.getText().toString().trim();
        final String gend = editRadioBtngender.toString().trim();
        final String patid = editTextProximity.getText().toString().trim();
        final String rel = getIntent().getStringExtra("editrelationshispinnerdrp");
        final String prev = getIntent().getStringExtra("editpreviousTreatmentspinnerdrp");
        final String xry = getIntent().getStringExtra("editchestxrayspinnerdrp");
        final String lat = getIntent().getStringExtra("editlatenttestspinnerdrp");
        final String lbi = getIntent().getStringExtra("editresultlbispinnerdrp");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SAVE_NAME,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //if there is a success
                                //storing the name to sqlite with status synced
                                saveNameToLocalStorage(name, midname, dob, addr, mob,loc,prox,gend,patid,rel,prev,xry,lat,lbi, NAME_SYNCED_WITH_SERVER);

                            } else {
                                //if there is some error
                                //saving the name to sqlite with status unsynced
                                saveNameToLocalStorage(name, midname,dob,addr,mob,loc,prox,gend, patid,rel,prev,xry,lat,lbi, NAME_NOT_SYNCED_WITH_SERVER);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        //on error storing the name to sqlite with status unsynced
                        saveNameToLocalStorage(name, midname, dob, addr, mob,loc,prox,gend,patid,rel,prev,xry,lat,lbi, NAME_NOT_SYNCED_WITH_SERVER);
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
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    //saving the name to local storage
    private void saveNameToLocalStorage(String given_name, String middle_name, String date_of_birth, String address, String mobile, String location , String proximity, String gend , String index_id,
                                        String relationship ,String previous_treatment_tb_contact,
                                        String chest_xray_result , String lantent_infection_test ,
                                        String lbi_result ,  int status) {
        editTextName.setText("");
        editTextmiddlename.setText("");
        editTextAddress.setText("");
        editTextMobile.setText("");
        editTextLocation.setText("");
        editTextProximity.setText("");

        db.addName(given_name,middle_name, date_of_birth, address, mobile , location, proximity, gend, index_id, relationship ,previous_treatment_tb_contact, chest_xray_result , lantent_infection_test,
                lbi_result ,status);
        Name n = new Name(given_name, middle_name,date_of_birth , address, mobile , location, proximity,gend, relationship ,previous_treatment_tb_contact, chest_xray_result , lantent_infection_test,
                lbi_result, index_id ,status);
        names.add(n);
        refreshList();
    }

    public void openLists(){
        Intent intent = new Intent(this, org.openmrs.mobile.activities.patientcontacts.lists.class);
        startActivity(intent);
    }

}
