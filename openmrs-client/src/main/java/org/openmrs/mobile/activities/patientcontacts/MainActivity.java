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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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

    public static final String URL_SAVE_NAME = "http://192.168.0.102/SycData/saveName.php";
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
    public EditText editTextMobile;
    public EditText editTextLocation;
    public EditText editTextProximity;
    public RadioButton editRadioBtngender;
    public RadioGroup radioSexGroup;
    public RadioButton male;
    public RadioButton female;
    public RadioButton radioBtnCough;
    public RadioGroup radiocoughGroup;
    public RadioButton radioCoughno;
    public RadioButton radioCoughyes;
    public RadioButton radioBtnFever;
    private RadioGroup radioFeverGroup;
    public RadioButton radioFeveryes;
    public RadioButton radioFeverno;
    public RadioButton radioBtnWeight_loss;
    private RadioGroup radioWeight_lossGroup;
    public RadioButton radioWeightlossyes;
    public RadioButton radioWeightlossno;
    public RadioButton radioBtnNight_Sweats;
    private RadioGroup radioNight_sweatsGroup;
    public RadioButton radioNightsweatyes;
    public RadioButton radioNightsweatno;
    public RadioButton radioBtnChest_xray;
    private RadioGroup radioChest_xrayGroup;
    public RadioButton radioChestXraydone;
    public RadioButton radioChestXray_not_done;
    public RadioButton radioBtnPreventivetherapy;
    private RadioGroup radioPreventive_therapyGroup;
    public RadioButton radioPreventive_TherapyYes;
    public RadioButton radioPreventive_TherapyNo;

    public Spinner mSpinner;
    private Spinner relationshispinner;
    private Spinner previousTreatmentspinner;
    private Spinner chestxrayspinner;
    private Spinner latenttestspinner;
    private Spinner resultlbispinner;
    String[] mLocations = {"Select Location of contact with index case", "Household", "Workplace", "Healthcare facility", "Prison" , "Educational institution"};
    String[] relationship = {"Select Relationship With Patient", " Spouse/partner", "Son/daughter", "Mother/Father", "Brother/Sister" , "Another relative in household" , "Unrelated within household" ,"Unrelated outside household"};
    String[] previousTreatments = {"Previous TB Treatment For Contact", "Never", "Yes - treated for active TB", "Yes - with preventive therapy"};
    String[] chestxray = {"X Ray Result", "NA (X-ray not done)", "CXR not available (though requested)", "CXR normal", "CXR abnormal suggestive of TB" , "CXR abnormal not TB"};
    String[] latenttest = {"Test for latent TB infection?", "Not done", "Tuberculin skin test", "IGRA"};
    String[] resultlbi = {"Result of LTBI testing", "NA (not done)","Negative","Indeterminate","positive"};

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
        editTextMobile = (EditText) findViewById(R.id.editTextMobile);
        editTextLocation = (EditText) findViewById(R.id.editTextLocation);
        editTextProximity = (EditText) findViewById(R.id.editTextProximity);
        //getting view layouts of the radio buttons/groups and mapping them to their respective id's
        radioSexGroup = (RadioGroup) findViewById(R.id.radiogender);
        male = (RadioButton) findViewById(R.id.male);
        female = (RadioButton) findViewById(R.id.female);
        radiocoughGroup= (RadioGroup) findViewById(R.id.radiocough);
        radioCoughno = (RadioButton) findViewById(R.id.radioCoughno);
        radioCoughyes = (RadioButton) findViewById(R.id.radioCoughyes);
        radioFeverGroup= (RadioGroup) findViewById(R.id.radiofever);
        radioFeveryes = (RadioButton) findViewById(R.id.radioFeveryes);
        radioFeverno = (RadioButton) findViewById(R.id.radioFeverno);
        radioWeight_lossGroup= (RadioGroup) findViewById(R.id.radioweightloss);
        radioWeightlossyes = (RadioButton) findViewById(R.id.radioWeightlossyes);
        radioWeightlossno = (RadioButton) findViewById(R.id.radioWeightlossno);
        radioNight_sweatsGroup= (RadioGroup) findViewById(R.id.radionightsweats);
        radioNightsweatyes = (RadioButton) findViewById(R.id.radioNightsweatyes);
        radioNightsweatno = (RadioButton) findViewById(R.id.radioNightsweatno);
        radioChest_xrayGroup= (RadioGroup) findViewById(R.id.radiochestxray);
        radioChestXraydone = (RadioButton) findViewById(R.id.radioChestXraydone);
        radioChestXray_not_done = (RadioButton) findViewById(R.id.radioChestXray_not_done);
        radioPreventive_therapyGroup= (RadioGroup) findViewById(R.id.radio_offer_preventive_therapy);
        radioPreventive_TherapyYes = (RadioButton) findViewById(R.id.radioPreventive_TherapyYes);
        radioPreventive_TherapyNo = (RadioButton) findViewById(R.id.radioPreventive_TherapyNo);

        mSpinner = findViewById(R.id.spinner);

        Spinner  mSpinner = (Spinner)findViewById(R.id.spinner);
        Spinner  relationshispinner = (Spinner)findViewById(R.id.relationshispinner);
        Spinner  previousTreatmentspinner = (Spinner)findViewById(R.id.previousTreatmentspinner);
        Spinner  chestxrayspinner = (Spinner)findViewById(R.id.chestxrayspinner);
        Spinner  latenttestspinner = (Spinner)findViewById(R.id.latenttestspinner);
        Spinner  resultlbispinner = (Spinner)findViewById(R.id.resultlbispinner);

        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mLocations);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(aa);

        ArrayAdapter bb = new ArrayAdapter(this, android.R.layout.simple_spinner_item, relationship);
        bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        relationshispinner.setAdapter(bb);

        ArrayAdapter cc = new ArrayAdapter(this, android.R.layout.simple_spinner_item, previousTreatments);
        cc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        previousTreatmentspinner.setAdapter(cc);

        ArrayAdapter dd = new ArrayAdapter(this, android.R.layout.simple_spinner_item, chestxray);
        dd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chestxrayspinner.setAdapter(dd);

        ArrayAdapter ee = new ArrayAdapter(this, android.R.layout.simple_spinner_item, latenttest);
        ee.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        latenttestspinner.setAdapter(ee);

        ArrayAdapter ff = new ArrayAdapter(this, android.R.layout.simple_spinner_item, resultlbi);
        ff.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resultlbispinner.setAdapter(ff);


        patientDetailsNames = (TextView) findViewById(R.id.patientDetailsNames);


        int selectedId = radioSexGroup.getCheckedRadioButtonId();
        editRadioBtngender = (RadioButton) findViewById(selectedId);

        int selectedCoughId = radiocoughGroup.getCheckedRadioButtonId();
        radioBtnCough = (RadioButton) findViewById(selectedCoughId);

        int selectedFeverId = radioFeverGroup.getCheckedRadioButtonId();
        radioBtnFever = (RadioButton) findViewById(selectedFeverId);

        int selectedWeight_lossId = radioWeight_lossGroup.getCheckedRadioButtonId();
        radioBtnWeight_loss = (RadioButton) findViewById(selectedWeight_lossId);

        int selectedNight_sweatsId = radioNight_sweatsGroup.getCheckedRadioButtonId();
        radioBtnNight_Sweats = (RadioButton) findViewById(selectedNight_sweatsId);

        int selectedChest_xrayId = radioChest_xrayGroup.getCheckedRadioButtonId();
        radioBtnChest_xray = (RadioButton) findViewById(selectedChest_xrayId);

        int selectedPreventive_therapyId = radioPreventive_therapyGroup.getCheckedRadioButtonId();
        radioBtnPreventivetherapy = (RadioButton) findViewById(selectedPreventive_therapyId);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id ) {

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        relationshispinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id ) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        previousTreatmentspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id ) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        chestxrayspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id ) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        latenttestspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id ) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        resultlbispinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id ) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });





        editTextName.setText(getIntent().getStringExtra("mytext"));
        editTextmiddlename.setText(getIntent().getStringExtra("middlenametxt"));
        patientDetailsNames.setText(getIntent().getStringExtra("idnt"));
        editTextDob.setText(getIntent().getStringExtra("dobtxt"));
        editTextAddress.setText(getIntent().getStringExtra("addresstxt"));
        editTextMobile.setText(getIntent().getStringExtra("editmobiletxt"));
        editTextLocation.setText(getIntent().getStringExtra("editlocationtxt"));
        editTextProximity.setText(getIntent().getStringExtra("editproximitytxt"));
        String editRadioBtngender = getIntent().getStringExtra("editgendertxt");
        String radioBtnCough = getIntent().getStringExtra("radioBtnCough");
        String radioBtnFever = getIntent().getStringExtra("radioBtnFever");
        String radioBtnWeight_loss = getIntent().getStringExtra("radioBtnWeight_loss");
        String radioBtnNight_Sweats = getIntent().getStringExtra("radioBtnNight_Sweats");
        String radioBtnChest_xray = getIntent().getStringExtra("radioBtnChest_xray");
        String radioBtnPreventivetherapy = getIntent().getStringExtra("radioBtnPreventivetherapy");

        String text = mSpinner.getSelectedItem().toString();
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
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PREVENTIVETHERAPY)),

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
        final String mob = editTextMobile.getText().toString().trim();
//        final String loc = editTextLocation.getText().toString().trim();
        final String prox = editTextProximity.getText().toString().trim();
        final String gend = getIntent().getStringExtra("editgendertxt");
        final String patid = patientDetailsNames.getText().toString().trim();
        final String loc = getIntent().getStringExtra("editlocationdrp");
        final String rel = getIntent().getStringExtra("editrelationshispinnerdrp");
        final String prev = getIntent().getStringExtra("editpreviousTreatmentspinnerdrp");
        final String xry = getIntent().getStringExtra("editchestxrayspinnerdrp");
        final String lat = getIntent().getStringExtra("editlatenttestspinnerdrp");
        final String lbi = getIntent().getStringExtra("editresultlbispinnerdrp");
        final String cou = getIntent().getStringExtra("editcoughtxt");
        final String fev = getIntent().getStringExtra("editfevertxt");
        final String weight = getIntent().getStringExtra("editweight_losstxt");
        final String n_sweats = getIntent().getStringExtra("editnight_sweatstxt");
        final String c_xray = getIntent().getStringExtra("editchest_xraytxt");
        final String p_therapy = getIntent().getStringExtra("editpreventive_therapytxt");





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
                                saveNameToLocalStorage(name, midname,dob, addr, mob,loc,prox, gend, patid,rel,prev,xry,lat,lbi,cou,fev,weight,n_sweats,c_xray,p_therapy, NAME_SYNCED_WITH_SERVER);
                            } else {
                                //if there is some error
                                //saving the name to sqlite with status unsynced
                                saveNameToLocalStorage(name, midname,dob ,addr,mob ,loc, prox, gend,patid,rel,prev,xry,lat,lbi,cou,fev,weight,n_sweats,c_xray,p_therapy, NAME_NOT_SYNCED_WITH_SERVER);
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
                        saveNameToLocalStorage(name, midname,dob, addr,mob,loc,prox, gend, patid ,rel,prev,xry,lat,lbi,cou,fev,weight,n_sweats,c_xray,p_therapy, NAME_NOT_SYNCED_WITH_SERVER);
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

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    //saving the name to local storage
    private void saveNameToLocalStorage(String given_name, String middle_name, String date_of_birth, String address, String mobile , String location, String proximity, String gend , String index_id ,
                                        String relationship , String previous_treatment_tb_contact,
                                        String chest_xray_result , String lantent_infection_test ,
                                        String lbi_result, String cough, String fever, String weight_loss, String night_sweats, String chest_xray, String preventive_therapy, int status) {
        editTextName.setText("");
        editTextmiddlename.setText("");
        editTextDob.setText("");
        editTextAddress.setText("");
        editTextMobile.setText("");
        editTextLocation.setText("");
        editTextProximity.setText("");


        db.addName(given_name,middle_name, date_of_birth, address, mobile , location, proximity, gend, index_id, relationship ,previous_treatment_tb_contact, chest_xray_result , lantent_infection_test,
                lbi_result ,cough, fever, weight_loss, night_sweats, chest_xray, preventive_therapy, status);
        Name n = new Name(given_name, middle_name,date_of_birth , address, mobile , location, proximity,gend, relationship ,previous_treatment_tb_contact, chest_xray_result , lantent_infection_test,
                lbi_result, index_id,cough, fever, weight_loss, night_sweats, chest_xray, preventive_therapy, status);
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
