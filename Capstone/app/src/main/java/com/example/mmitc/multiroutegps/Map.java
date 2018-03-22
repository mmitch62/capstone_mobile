package com.example.mmitc.multiroutegps;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Iterator;

public class Map extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = Map.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    GoogleApiClient mGoogleApiClient;

    // Entry point to the location provider
    FusedLocationProviderClient mFusedLocationClient;

    // Entry points to the Places API
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    private final LatLng mDefaultLocation = new LatLng(0, 0);
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 14;
    private boolean mLocationPermissionGranted;

    // Last known location retrieved by the Fused Location Provider
    Location mLastKnownLocation;
    LatLng mLatLngLocation;
    CharSequence mPlaceName;
    HashMap<CharSequence, LatLng> mLocationList;
    double current_latitude, current_longitude;
    double end_latitude, end_longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        //Construct a FusedLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //Construct a GeoDataClient
        mGeoDataClient = Places.getGeoDataClient(this, null);
        //Construct a PlaceDetectionClient
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();

        mLocationList = new HashMap<>();
        mLastKnownLocation = new Location("");

        updateLocationUI();

        getDeviceLocation();

        //Add the devices current location to the first pair in hashmap
        //mLocationList.put(mLastKnownLocation.toString(), new LatLng(current_latitude, current_longitude));

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                mLatLngLocation = place.getLatLng();
                mPlaceName = place.getName();
                end_latitude = place.getLatLng().latitude;
                end_longitude = place.getLatLng().longitude;
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Gets current location of device and positions the map's camera
     */
    private void getDeviceLocation() {
       try {
           if (mLocationPermissionGranted) {
               Task<Location> locationResult = mFusedLocationClient.getLastLocation(); //THIS ALWAYS RETURNS NULL
               //mFusedLocationClient.requestLocationUpdates(provider etc);
               locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                   @Override
                   public void onComplete(@NonNull Task<Location> task) {
                      if (task.isSuccessful()) {
                          // Set the map's camera to current location
                          mLastKnownLocation = task.getResult();
                          //mLastKnownLocation = new Location(task.getResult());
                          current_latitude = mLastKnownLocation.getLatitude();
                          current_longitude = mLastKnownLocation.getLongitude();

                          mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                  new LatLng(current_latitude, current_longitude), DEFAULT_ZOOM));
                          mMap.addMarker(new MarkerOptions().position(new LatLng(current_latitude, current_longitude))
                                  .title("Current location"));
                      } else {
                          Log.d(TAG, "Current location is null. Using defaults.");
                          Log.e(TAG, "Exception: %s", task.getException());
                          mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                          mMap.getUiSettings().setMyLocationButtonEnabled(false);
                      }
                   }
               });
           }
       } catch (SecurityException e) {
           Log.e("Exception: %s", e.getMessage());
       }
        //return mLastKnownLocation;
    }
    /**
     * Prompts user for permission to use the device location
     */
    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, result arrays are empty
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    public void onAdd(View view) {

        mLocationList.put(mPlaceName, mLatLngLocation);
        /*
        for(LatLng value : mLocationList.values())
        {

        }
        */
        Object dataTransfer[] = new Object[3];

        String url = getDirectionsUrl();
        GetDirectionsData getDirectionsData = new GetDirectionsData();
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;
        dataTransfer[2] = new LatLng(mLatLngLocation.latitude, mLatLngLocation.longitude);

        getDirectionsData.execute(dataTransfer);

    }

    public void onRoute(View view) {

        //mMap.clear();
        //mMap.addMarker(new MarkerOptions().position(new LatLng(current_latitude, current_longitude))
                //.title("Current location"));

        Iterator it = mLocationList.entrySet().iterator();
        while(it.hasNext())
        {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());

            LatLng value = mLocationList.get(pair.getKey());

            Object dataTransfer[] = new Object[3];

            String url = getDirectionsUrl();
            DisplayDirections displayDirections = new DisplayDirections();
            //GetDirectionsData getDirectionsData = new GetDirectionsData();
            dataTransfer[0] = mMap;
            dataTransfer[1] = url;
            dataTransfer[2] = new LatLng(value.latitude, value.longitude);

            //getDirectionsData.execute(dataTransfer);
            displayDirections.execute(dataTransfer);
        }
    }

    private String getDirectionsUrl()
    {
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        //EDIT THESE FOR GETTING INFORMATION BETWEEN TWO POINTS
        googleDirectionsUrl.append("origin="+current_latitude+","+current_longitude);
        googleDirectionsUrl.append("&destination="+end_latitude+","+end_longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyC02s-qxo9KeghTmfs6ELy-x5oKfrCh8Ss");

        return googleDirectionsUrl.toString();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}


