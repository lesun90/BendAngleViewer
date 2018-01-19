package com.bendangle.bluefruit.le.connect.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.EditText;

import com.bendangle.bluefruit.le.connect.R;


public class CommonHelpActivity extends AppCompatActivity {

    private EditText serialNumberText;
    private EditText pipelengthText;
    private EditText pipeNumberText;
    private EditText heatNumberText;
    private EditText latitudeText;
    private EditText longitudeText;
    private EditText chainAgeText;
    private EditText ovalityText;
    private EditText temperatureText;
    private EditText noteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commonhelp);

        // Title
        String title = "Information";
        getSupportActionBar().setTitle(title);

        serialNumberText = (EditText) findViewById(R.id.serialNumber);
        pipelengthText   = (EditText) findViewById(R.id.pipelength);
        pipeNumberText   = (EditText) findViewById(R.id.pipeNumber);
        heatNumberText   = (EditText) findViewById(R.id.heatNumber);
        latitudeText     = (EditText) findViewById(R.id.latitude);
        longitudeText    = (EditText) findViewById(R.id.longitude);
        chainAgeText     = (EditText) findViewById(R.id.chainAge);
        ovalityText      = (EditText) findViewById(R.id.ovality);
        temperatureText  = (EditText) findViewById(R.id.temperature);
        noteText         = (EditText) findViewById(R.id.note);

        Intent intentExtras = getIntent();
        Bundle extrasBundle = intentExtras.getExtras();
        if (!extrasBundle.isEmpty())
        {
            serialNumberText.setText((String) extrasBundle.get("serialNumber"));
            pipelengthText.setText((String) extrasBundle.get("pipelength"));
            pipeNumberText.setText((String) extrasBundle.get("pipeNumber"));
            heatNumberText.setText((String) extrasBundle.get("heatNumber"));
            latitudeText.setText((String) extrasBundle.get("latitude"));
            longitudeText.setText((String) extrasBundle.get("longitude"));
            chainAgeText.setText((String) extrasBundle.get("chainAge"));
            ovalityText.setText((String) extrasBundle.get("ovality"));
            temperatureText.setText((String) extrasBundle.get("temperature"));
            noteText.setText((String) extrasBundle.get("note"));

        }

    }

    @Override
    public void onBackPressed(){
        Intent data = new Intent();
        data.putExtra("serialNumber", serialNumberText.getText().toString());
        data.putExtra("pipelength",   pipelengthText.getText().toString());
        data.putExtra("pipeNumber",   pipeNumberText.getText().toString());
        data.putExtra("heatNumber",   heatNumberText.getText().toString());
        data.putExtra("latitude",     latitudeText.getText().toString());
        data.putExtra("longitude",    longitudeText.getText().toString());
        data.putExtra("chainAge",     chainAgeText.getText().toString());
        data.putExtra("ovality",      ovalityText.getText().toString());
        data.putExtra("temperature",  temperatureText.getText().toString());
        data.putExtra("note",         noteText.getText().toString());
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent data = new Intent();
        data.putExtra("serialNumber", serialNumberText.getText().toString());
        data.putExtra("pipelength",   pipelengthText.getText().toString());
        data.putExtra("pipeNumber",   pipeNumberText.getText().toString());
        data.putExtra("heatNumber",   heatNumberText.getText().toString());
        data.putExtra("latitude",     latitudeText.getText().toString());
        data.putExtra("longitude",    longitudeText.getText().toString());
        data.putExtra("chainAge",     chainAgeText.getText().toString());
        data.putExtra("ovality",      ovalityText.getText().toString());
        data.putExtra("temperature",  temperatureText.getText().toString());
        data.putExtra("note",         noteText.getText().toString());
        setResult(RESULT_OK, data);

        return super.onOptionsItemSelected(item);
    }

}
