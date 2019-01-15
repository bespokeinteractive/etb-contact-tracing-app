package org.openmrs.mobile.activities.patientdashboard.contacts;

/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

//Values captured in this activity then sent to main activity for verification
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.addeditpatient.AddEditPatientActivity;
import org.openmrs.mobile.activities.patientcontacts.MainActivity;
import org.openmrs.mobile.activities.patientcontacts.Name;
import org.openmrs.mobile.activities.patientcontacts.NameAdapter;
import org.openmrs.mobile.activities.patientcontacts.lists;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardActivity;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardContract;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardFragment;
import org.openmrs.mobile.activities.patientdashboard.details.PatientDashboardDetailsPresenter;
import org.openmrs.mobile.activities.patientdashboard.details.PatientPhotoActivity;
import org.openmrs.mobile.models.Patient;
import org.openmrs.mobile.utilities.ApplicationConstants;
import org.openmrs.mobile.utilities.FontsUtil;
import org.openmrs.mobile.utilities.StringUtils;
import org.openmrs.mobile.utilities.ToastUtil;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ContactPatientDetailsFragment extends PatientDashboardFragment implements PatientDashboardContract.ViewPatientDetails {

    private MainActivity mClass;


    private Button buttonOpen;
    private Button buttonSave;
    private View rootView;
    private EditText editTextName;
    private ListView listViewNames;
    private TextView patientDetailsNames;
    private EditText editTextmiddlename;
    private EditText editTextDob;
    private EditText editTextAddress;
    private EditText editTextMobile;
    private EditText editTextLocation;
    private EditText editTextProximity;
    private RadioButton radioBtngender;
    private RadioGroup radioSexGroup;
    private RadioButton radioBtnCough;
    private RadioGroup radiocoughGroup;
    private RadioButton radioBtnFever;
    private RadioGroup radioFeverGroup;
    private RadioButton radioBtnWeight_loss;
    private RadioGroup radioWeight_lossGroup;
    private RadioButton radioBtnNight_Sweats;
    private RadioGroup radioNight_sweatsGroup;
    private RadioButton radioBtnChest_xray;
    private RadioGroup radioChest_xrayGroup;
    private RadioButton radioBtnPreventivetherapy;
    private RadioGroup radioPreventive_therapyGroup;

    private Spinner mSpinner;
    private Spinner relationshispinner;
    private Spinner previousTreatmentspinner;
    private Spinner chestxrayspinner;
    private Spinner latenttestspinner;
    private Spinner resultlbispinner;
    String[] mLocations = {"Select", "Household", "Workplace", "Healthcare facility", "Prison" , "Educational institution"};
    String[] relationship = {"Select", " Spouse/partner", "Son/daughter", "Mother/Father", "Brother/Sister" , "Another relative in household" , "Unrelated within household" ,"Unrelated outside household"};
    String[] previousTreatments = {"Select", "Never", "Yes - treated for active TB", "Yes - with preventive therapy"};
    String[] chestxray = {"Select", "NA (X-ray not done)", "CXR not available (though requested)", "CXR normal", "CXR abnormal suggestive of TB" , "CXR abnormal not TB"};
    String[] latenttest = {"Select", "Not done", "Tuberculin skin test", "IGRA"};
    String[] resultlbi = {"Select", "NA (not done)","Negative","Indeterminate","positive"};


    private List<Name> names;

    private PatientDashboardActivity mPatientDashboardActivity;


    public static final String URL_SAVE_NAME = "http://192.168.1.247/etb/etb_contact.php";
    private org.openmrs.mobile.activities.patientcontacts.DatabaseHelper db;
    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;
    public static final String DATA_SAVED_BROADCAST = "net.simplifiedcoding.datasaved";
    private BroadcastReceiver broadcastReceiver;
    private NameAdapter nameAdapter;



    public static ContactPatientDetailsFragment newInstance() {
        return new org.openmrs.mobile.activities.patientdashboard.contacts.ContactPatientDetailsFragment();
    }

    @Override
    public void attachSnackbarToActivity() {
        Snackbar snackbar = Snackbar
                .make(mPatientDashboardActivity.findViewById(R.id.patientDashboardContentFrame), getString(R.string.snackbar_no_internet_connection), Snackbar.LENGTH_INDEFINITE);
        View view = snackbar.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPatientDashboardActivity = (PatientDashboardActivity) context;
    }

   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setPresenter(mPresenter);

        setHasOptionsMenu(true);

        mClass = new MainActivity();


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.patient_details_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.actionSynchronize:
                ((PatientDashboardDetailsPresenter) mPresenter).synchronizePatient();
                break;
            case R.id.actionUpdatePatient:
                startPatientUpdateActivity(mPresenter.getPatientId());
                break;
            default:
                // Do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_contacts_add, null, false);
//       pasted from here


        FontsUtil.setFont((ViewGroup) rootView);

        super.onCreate(savedInstanceState);
        buttonOpen = (Button) rootView.findViewById(R.id.buttonOpen);
        Spinner  mSpinner = (Spinner)rootView.findViewById(R.id.spinner);
        Spinner  relationshispinner = (Spinner)rootView.findViewById(R.id.relationshispinner);
        Spinner  previousTreatmentspinner = (Spinner)rootView.findViewById(R.id.previousTreatmentspinner);
        Spinner  chestxrayspinner = (Spinner)rootView.findViewById(R.id.chestxrayspinner);
        Spinner  latenttestspinner = (Spinner)rootView.findViewById(R.id.latenttestspinner);
        Spinner  resultlbispinner = (Spinner)rootView.findViewById(R.id.resultlbispinner);


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

        ArrayAdapter aa = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, mLocations);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(aa);

        ArrayAdapter bb = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, relationship);
        bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        relationshispinner.setAdapter(bb);

        ArrayAdapter cc = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, previousTreatments);
        cc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        previousTreatmentspinner.setAdapter(cc);

        ArrayAdapter dd = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, chestxray);
        dd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chestxrayspinner.setAdapter(dd);

        ArrayAdapter ee = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, latenttest);
        ee.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        latenttestspinner.setAdapter(ee);

        ArrayAdapter ff = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, resultlbi);
        ff.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resultlbispinner.setAdapter(ff);

        buttonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), lists.class);
                startActivity(intent);

            }
        });


        buttonSave = (Button) rootView.findViewById(R.id.buttonSave);

        buttonSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

             EditText  editname  = (EditText) rootView.findViewById(R.id.editTextName);
             EditText  editTextmiddlename  = (EditText) rootView.findViewById(R.id.editTextmiddlename);
             TextView  patientDetailsNames  = (TextView) rootView.findViewById(R.id.patientDetailsNames);
             EditText  editdob  = (EditText) rootView.findViewById(R.id.editTextDob);
             EditText  editaddress  = (EditText) rootView.findViewById(R.id.editTextAddress);
             EditText  editmobile  = (EditText) rootView.findViewById(R.id.editTextMobile);
             EditText  editlocation  = (EditText) rootView.findViewById(R.id.editTextLocation);
             EditText  editproximity  = (EditText) rootView.findViewById(R.id.editTextProximity);

             RadioGroup radioSexGroup = (RadioGroup) rootView.findViewById(R.id.radiogender);
             int selectedId = radioSexGroup.getCheckedRadioButtonId();
             radioBtngender = (RadioButton) rootView.findViewById(selectedId);

             RadioGroup radiocoughGroup = (RadioGroup) rootView.findViewById(R.id.radiocough);
             int selectedCoughId = radiocoughGroup.getCheckedRadioButtonId();
             radioBtnCough = (RadioButton) rootView.findViewById(selectedCoughId);

             RadioGroup radioFeverGroup = (RadioGroup) rootView.findViewById(R.id.radiofever);
             int selectedFeverId = radioFeverGroup.getCheckedRadioButtonId();
             radioBtnFever = (RadioButton) rootView.findViewById(selectedFeverId);

             RadioGroup radioWeight_lossGroup = (RadioGroup) rootView.findViewById(R.id.radioweightloss);
             int selectedWeight_lossId = radioWeight_lossGroup.getCheckedRadioButtonId();
             radioBtnWeight_loss = (RadioButton) rootView.findViewById(selectedWeight_lossId);

              RadioGroup radioNight_sweatsGroup = (RadioGroup) rootView.findViewById(R.id.radionightsweats);
              int selectedNight_sweatsId = radioNight_sweatsGroup.getCheckedRadioButtonId();
                radioBtnNight_Sweats = (RadioButton) rootView.findViewById(selectedNight_sweatsId);

              RadioGroup radioChest_xrayGroup = (RadioGroup) rootView.findViewById(R.id.radiochestxray);
              int selectedChest_xrayId = radioChest_xrayGroup.getCheckedRadioButtonId();
                radioBtnChest_xray = (RadioButton) rootView.findViewById(selectedChest_xrayId);

             RadioGroup radioPreventive_therapyGroup = (RadioGroup) rootView.findViewById(R.id.radio_offer_preventive_therapy);
             int selectedPreventive_therapyId = radioPreventive_therapyGroup.getCheckedRadioButtonId();
                radioBtnPreventivetherapy = (RadioButton) rootView.findViewById(selectedPreventive_therapyId);


             String text = editname.getText().toString();
             String text1 = patientDetailsNames.getText().toString();
             String middlenametxt = editTextmiddlename.getText().toString();
             String dobtxt = editdob.getText().toString();
             String addresstxt = editaddress.getText().toString();
             String editmobiletxt = editmobile.getText().toString();
             String editlocationtxt = editlocation.getText().toString();
             String editproximitytxt = editproximity.getText().toString();
             String editgendertxt = radioBtngender.getText().toString();
             String editlocationdrp = mSpinner.getSelectedItem().toString();
             String editrelationshispinnerdrp = relationshispinner.getSelectedItem().toString();
             String editpreviousTreatmentspinnerdrp = previousTreatmentspinner.getSelectedItem().toString();
             String editchestxrayspinnerdrp = chestxrayspinner.getSelectedItem().toString();
             String editlatenttestspinnerdrp = latenttestspinner.getSelectedItem().toString();
             String editresultlbispinnerdrp = resultlbispinner.getSelectedItem().toString();
             String editcoughtxt = radioBtnCough.getText().toString();
             String editfevertxt = radioBtnFever.getText().toString();
             String editweight_losstxt = radioBtnWeight_loss.getText().toString();
                String editnight_sweatstxt = radioBtnNight_Sweats.getText().toString();
                String editchest_xraytxt = radioBtnChest_xray.getText().toString();
                String editpreventive_therapytxt = radioBtnPreventivetherapy.getText().toString();

                Intent intent = new Intent(v.getContext(), MainActivity.class);
                intent.putExtra("mytext",text);
                intent.putExtra("idnt",text1);
                intent.putExtra("middlenametxt", middlenametxt );
                intent.putExtra("dobtxt", dobtxt );
                intent.putExtra("addresstxt", addresstxt );
                intent.putExtra("editmobiletxt", editmobiletxt );
                intent.putExtra("editlocationtxt", editlocationtxt );
                intent.putExtra("editproximitytxt", editproximitytxt );
                intent.putExtra("editgendertxt", editgendertxt );
                intent.putExtra("editlocationdrp", editlocationdrp );
                intent.putExtra("editrelationshispinnerdrp", editrelationshispinnerdrp );
                intent.putExtra("editpreviousTreatmentspinnerdrp", editpreviousTreatmentspinnerdrp );
                intent.putExtra("editchestxrayspinnerdrp", editchestxrayspinnerdrp );
                intent.putExtra("editlatenttestspinnerdrp", editlatenttestspinnerdrp );
                intent.putExtra("editresultlbispinnerdrp", editresultlbispinnerdrp );
                intent.putExtra("editcoughtxt", editcoughtxt );
                intent.putExtra("editfevertxt", editfevertxt );
                intent.putExtra("editweight_losstxt", editweight_losstxt );
                intent.putExtra("editnight_sweatstxt", editnight_sweatstxt );
                intent.putExtra("editchest_xraytxt", editchest_xraytxt );
                intent.putExtra("editpreventive_therapytxt", editpreventive_therapytxt );



                startActivity(intent);

//
//                MainActivity main = new MainActivity();
//                main.editTextName.setText(text);
//
//                main.saveNameToServer();

                final ProgressDialog progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage("Submitting Data for Verification");
                progressDialog.show();

            }
        });

        return rootView;
    }


    @Override
    public void resolvePatientDataDisplay(final Patient patient) {

    }

    @Override
    public void showDialog(int resId) {
        mPatientDashboardActivity.showProgressDialog(resId);
    }

    private void showAddressDetailsViewElement(View detailsLayout, int detailsViewId, String detailsText) {
        if (StringUtils.notNull(detailsText) && StringUtils.notEmpty(detailsText)) {
            ((TextView) detailsLayout.findViewById(detailsViewId)).setText(detailsText);
        } else {
            detailsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void dismissDialog() {
        mPatientDashboardActivity.dismissCustomFragmentDialog();
    }

    @Override
    public void showToast(int stringRes, boolean error) {
        ToastUtil.ToastType toastType = error ? ToastUtil.ToastType.ERROR : ToastUtil.ToastType.SUCCESS;
        ToastUtil.showShortToast(mPatientDashboardActivity, toastType, stringRes);
    }

    @Override
    public void setMenuTitle(String nameString, String identifier) {
        mPatientDashboardActivity.getSupportActionBar().setTitle(nameString);
        mPatientDashboardActivity.getSupportActionBar().setSubtitle("#" + identifier);
        ((TextView) rootView.findViewById(R.id.patientDetailsNames)).setText(identifier);
    }


    @Override
    public void startPatientUpdateActivity(long patientId) {
        Intent updatePatient = new Intent(mPatientDashboardActivity, AddEditPatientActivity.class);
        updatePatient.putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE,
                String.valueOf(patientId));
        startActivity(updatePatient);
    }

    public void showPatientPhoto(Bitmap photo, String patientName) {
        Intent intent = new Intent(getContext(), PatientPhotoActivity.class);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
        intent.putExtra("photo", byteArrayOutputStream.toByteArray());
        intent.putExtra("name", patientName);
        startActivity(intent);
    }

//    pasted from here





}






