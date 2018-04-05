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
import java.util.concurrent.ExecutionException;

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
    double start_latitude, start_longitude;
    double end_latitude, end_longitude;

    String directionsData;
    HashMap<String, String> distanceList;
    double[][] distanceMatrix;

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
               //mFusedLocationClient.requestLocationUpdates(provider etc) MUST BE USED;

               locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                   @Override
                   public void onComplete(@NonNull Task<Location> task) {
                      if (task.isSuccessful()) {
                          // Set the map's camera to current location
                          mLastKnownLocation = task.getResult();
                          //mLastKnownLocation = new Location(task.getResult());
                          current_latitude = mLastKnownLocation.getLatitude();
                          current_longitude = mLastKnownLocation.getLongitude();

                          //TEMPORARY HARD-CODED METHOD TO ADD DEVICE LOCATION TO HASHMAP
                          LatLng deviceLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                          CharSequence tempCurrent = "Current location";
                          mLocationList.put(tempCurrent, deviceLocation);

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

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLatLngLocation.latitude, mLatLngLocation.longitude), DEFAULT_ZOOM));
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mLatLngLocation.latitude, mLatLngLocation.longitude))
                .title(mPlaceName.toString()));

        /*
        for(LatLng value : mLocationList.values())
        {

        }
        */
    }

    public void onRoute(View view) {

        //mMap.clear();
        //mMap.addMarker(new MarkerOptions().position(new LatLng(current_latitude, current_longitude))
                //.title("Current location"));
        System.out.println("Before call to create distance matrix");
        createDistanceMatrix(mLocationList);

        Iterator it = mLocationList.entrySet().iterator();
        while(it.hasNext())
        {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }

        //PRINT OUT DISTANCE MATRIX TO SHOW IT WORKS
        for (int i = 0; i < mLocationList.size(); ++i)
        {
            System.out.print("| ");
            for (int j = 0; j < mLocationList.size(); ++j)
            {
                System.out.print(distanceMatrix[i][j] + " ");
            }
            System.out.println(" | Location " + i);
        }
    }

    public void createDistanceMatrix(HashMap<CharSequence, LatLng> locationList)
    {
        System.out.println("Begin create distance matrix");
        distanceMatrix = new double[locationList.size()][locationList.size()];

        Iterator it1 = locationList.entrySet().iterator();
        int it1MatrixSpot = 0;

        while (it1.hasNext())
        {
            HashMap.Entry pair1 = (HashMap.Entry)it1.next();

            LatLng value1 = locationList.get(pair1.getKey());
            start_latitude = value1.latitude;
            start_longitude = value1.longitude;

            Iterator it2 = locationList.entrySet().iterator();

            int it2MatrixSpot = 0;
            while (it2.hasNext())
            {
                HashMap.Entry pair2 = (HashMap.Entry)it2.next();

                LatLng value2 = locationList.get(pair2.getKey());
                end_latitude = value2.latitude;
                end_longitude = value2.longitude;

                Object dataTransfer[] = new Object[3];

                String url = getDirectionsUrl();

                GetDirectionsData getDirectionsData = new GetDirectionsData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = new LatLng(end_latitude, end_longitude);

                //This method will use the url to return a jsonfile as a string, which the data parser can then parse to get the appropriate data
                try {
                    directionsData = getDirectionsData.execute(dataTransfer).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                distanceList = null;
                DataParser parser = new DataParser();
                distanceList = parser.parseDuration(directionsData);

                //String duration = distanceList.get("duration");
                String distance = distanceList.get("distance");
                double distanceValue = Double.parseDouble(distance);
                distanceMatrix[it1MatrixSpot][it2MatrixSpot] = distanceValue;

                it2MatrixSpot++;
            }

            it1MatrixSpot++;
        }
    }


    private String getDirectionsUrl()
    {
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        //EDIT THESE FOR GETTING INFORMATION BETWEEN TWO POINTS
        googleDirectionsUrl.append("origin="+start_latitude+","+start_longitude);
        googleDirectionsUrl.append("&destination="+end_latitude+","+end_longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyC02s-qxo9KeghTmfs6ELy-x5oKfrCh8Ss");

        return googleDirectionsUrl.toString();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}


