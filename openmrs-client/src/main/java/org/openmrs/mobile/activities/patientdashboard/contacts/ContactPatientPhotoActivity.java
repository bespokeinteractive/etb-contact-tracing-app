package org.openmrs.mobile.activities.patientdashboard.contacts;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import org.openmrs.mobile.R;

import java.io.ByteArrayInputStream;


public class ContactPatientPhotoActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_photo);
        byte[] photo = getIntent().getByteArrayExtra("photo");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(photo);
        Bitmap patientPhoto = BitmapFactory.decodeStream(inputStream);
        ImageView patientImageView = (ImageView) findViewById(R.id.patientPhoto);
        patientImageView.setImageBitmap(patientPhoto);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            String patientName = getIntent().getStringExtra("name");
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(patientName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
