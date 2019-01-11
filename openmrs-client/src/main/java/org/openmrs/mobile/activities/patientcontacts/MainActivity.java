package org.openmrs.mobile.activities.patientcontacts;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.openmrs.mobile.R;

import org.openmrs.mobile.activities.ACBaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends ACBaseActivity implements View.OnClickListener {

    /*
     * this is the url to our webservice
     * make sure you are using the ip instead of localhost
     * it will not work if you are using localhost
     * */
    public static final String URL_SAVE_NAME = "http://192.168.0.100/SyncData/saveName.php";

    //database helper object
    private org.openmrs.mobile.activities.patientcontacts.DatabaseHelper db;

    //View objects
    private Button buttonSave;
    public EditText editTextName;
    private ListView listViewNames;
    public EditText editTextmiddlename;
    private Button buttonOpen;
    private TextView patientDetailsNames;
    public EditText editTextDob;
    public EditText editTextAddress;


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
        setContentView(R.layout.activity_main);
        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //initializing views and objects
        db = new org.openmrs.mobile.activities.patientcontacts.DatabaseHelper(this);
        names = new ArrayList<>();

        buttonSave = (Button) findViewById(R.id.buttonSave);

        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextmiddlename = (EditText) findViewById(R.id.editTextmiddlename);
        editTextDob = (EditText) findViewById(R.id.editTextDob);
        editTextAddress = (EditText) findViewById(R.id.editTextAddress);


        patientDetailsNames = (TextView) findViewById(R.id.patientDetailsNames);


        editTextName.setText(getIntent().getStringExtra("mytext"));
        editTextmiddlename.setText(getIntent().getStringExtra("middlenametxt"));
        patientDetailsNames.setText(getIntent().getStringExtra("idnt"));
        editTextDob.setText(getIntent().getStringExtra("dobtxt"));
        editTextAddress.setText(getIntent().getStringExtra("addresstxt"));




        listViewNames = (ListView) findViewById(R.id.listViewNames);

        buttonOpen = (Button) findViewById(R.id.buttonOpen);


        //adding click listener to button
        buttonSave.setOnClickListener(this);

        buttonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLists();
            }
        });


        //calling the method to load all the stored names
        loadNames();

        //the broadcast receiver to update sync status
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //loading the names again
                //loadNames();
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
    public void saveNameToServer() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving Name...");
        progressDialog.show();

        final String name = editTextName.getText().toString().trim();
        final String midname = editTextmiddlename.getText().toString().trim();
        final String dob = editTextDob.getText().toString().trim();
        final String addr = editTextAddress.getText().toString().trim();

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
                                saveNameToLocalStorage(name, midname,dob, addr,  NAME_SYNCED_WITH_SERVER);
                            } else {
                                //if there is some error
                                //saving the name to sqlite with status unsynced
                                saveNameToLocalStorage(name, midname,dob ,addr, NAME_NOT_SYNCED_WITH_SERVER);
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
                        saveNameToLocalStorage(name, midname,dob, addr, NAME_NOT_SYNCED_WITH_SERVER);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("given_name", name);
                params.put("middle_name", midname);
                params.put("date_of_birth", dob);
                params.put("address", addr);

                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    //saving the name to local storage
    private void saveNameToLocalStorage(String given_name, String middle_name, String date_of_birth, String address, int status) {
        editTextName.setText("");
        editTextmiddlename.setText("");
        editTextDob.setText("");
        editTextAddress.setText("");


        db.addName(given_name,middle_name, date_of_birth, address, status);
        Name n = new Name(given_name, middle_name,date_of_birth , address, status);
        names.add(n);
        refreshList();
    }

    @Override
    public void onClick(View view) {
        saveNameToServer();
    }

    public void openLists() {
        Intent intent = new Intent(this, lists.class);
        startActivity(intent);
    }
}

