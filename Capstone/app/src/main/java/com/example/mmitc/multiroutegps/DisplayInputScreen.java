package com.example.mmitc.multiroutegps;

import android.app.ActionBar;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.provider.Telephony;
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
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class DisplayInputScreen extends AppCompatActivity {

    private ConstraintLayout mLayout;
    private EditText mEditText;
    private Button mButton;
    private Button routeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_input_screen);

        mLayout = (ConstraintLayout) findViewById(R.id.linearLayout);
        mEditText = (EditText) findViewById(R.id.location_text);
        mButton = (Button) findViewById(R.id.location_button);
        routeButton = (Button) findViewById(R.id.route_button);

    }

    public void loadMap(View view) {
        /*
        EditText location_tf = (EditText) findViewById(R.id.location_text);
        String location = location_tf.getText().toString();
        List<Address> addressList = null;

        if(location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            addressLatLng = new LatLng(address.getLatitude(), address.getLongitude());
        }
*/
        Intent intent = new Intent(this, Map.class);
        startActivity(intent);
    }
}
