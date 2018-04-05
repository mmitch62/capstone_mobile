package com.example.mmitc.multiroutegps;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

/**
 * Created by mmitc on 3/14/2018.
 */

public class GetDirectionsData extends AsyncTask<Object, String, String> {

    GoogleMap mMap;
    String url;
    String googleDirectionsData;
    String duration, distance;
    LatLng latLng;

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        latLng = (LatLng)objects[2];

        DownloadUrl downloadUrl = new DownloadUrl();

        try {
            googleDirectionsData = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googleDirectionsData;
    }

    //@Override
    //CURRENTLY ATTEMPT TO PARSE DATA IN MAP.JAVA
    /*
    protected void onPostExecute(String s) {

        HashMap<String, String> distanceList = null;
        DataParser parser = new DataParser();

        distanceList = parser.parseDuration(s);

        duration = distanceList.get("duration");
        distance = distanceList.get("distance");

        //mMap.clear();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Duration = "+duration);
        markerOptions.snippet("Distance = "+distance);
        markerOptions.draggable(true);

        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));

        //displayDirections(directionsList);
    }
    */
    /*
    public void displayDirections(String[] directionsList)
    {
        int count = directionsList.length;
        for (int i = 0; i < count; i++)
        {
            PolylineOptions options = new PolylineOptions();
            options.color(Color.RED);
            options.width(10);
            options.addAll(PolyUtil.decode(directionsList[i]));

            mMap.addPolyline(options);
        }
    }
    */
}
