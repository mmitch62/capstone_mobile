package com.example.mmitc.multiroutegps;

import android.app.ActionBar;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class DisplayInputScreen extends AppCompatActivity {

    private ConstraintLayout mLayout;
    private EditText mEditText;
    private Button mButton;
    private Button routeButton;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_input_screen);

        mLayout = (ConstraintLayout) findViewById(R.id.linearLayout);
        mEditText = (EditText) findViewById(R.id.location_text);
        mButton = (Button) findViewById(R.id.location_button);
        routeButton = (Button) findViewById(R.id.route_button);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public void loadMap(View view) {
        Intent intent = new Intent(this, Map.class);
        startActivity(intent);
    }
}
