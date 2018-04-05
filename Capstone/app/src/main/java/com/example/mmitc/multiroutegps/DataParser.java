package com.example.mmitc.multiroutegps;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by mmitc on 3/13/2018.
 */

public class DataParser {

    private HashMap<String, String> getDuration(JSONArray googleDirectionsJson)
    {
        HashMap<String, String> googleDirectionsMap = new HashMap<>();
        String duration = "";
        String distance = "";

        Log.d("json response", googleDirectionsJson.toString());
        try {
            duration = googleDirectionsJson.getJSONObject(0).getJSONObject("duration").getString("value");
            distance = googleDirectionsJson.getJSONObject(0).getJSONObject("distance").getString("value");

            googleDirectionsMap.put("duration", duration);
            googleDirectionsMap.put("distance", distance);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return googleDirectionsMap;
    }
/*
    private HashMap<String, String> getPlace(JSONObject googlePlaceJson)
    {
        HashMap<String, String> googlePlacesMap = new HashMap<>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";

        try {
            if(!googlePlaceJson.isNull("name"))
            {
                placeName = googlePlaceJson.getString("name");
            }
            if(!googlePlaceJson.isNull("vicinity"))
            {
                vicinity = googlePlaceJson.getString("vicinity");
            }
            latitude = googlePlaceJson.getJSONObject("geometry")
                    .getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry")
                    .getJSONObject("location").getString("lng");

            reference = googlePlaceJson.getString("reference");

            googlePlacesMap.put("place_name", placeName);
            googlePlacesMap.put("vicinity", vicinity);
            googlePlacesMap.put("lat", latitude);
            googlePlacesMap.put("lng", longitude);
            googlePlacesMap.put("reference", reference);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return googlePlacesMap;
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray)
    {
        int count = jsonArray.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> placeMap = null;

        for (int i = 0; i < count; ++i)
        {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placesList.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;
    }

    public List<HashMap<String, String>> parse(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPlaces(jsonArray);
    }
*/

    //Get the distance and duration to a searched route
    public HashMap<String, String> parseDuration(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getDuration(jsonArray);
    }

    //Get a polyline direction list to the searched route
    public String[] parseDirections(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                    .getJSONObject(0).getJSONArray("steps");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPaths(jsonArray);
    }

    public String[] getPaths(JSONArray googleStepsJson)
    {
        int count = googleStepsJson.length();
        String[] polylines = new String[count];

        for (int i = 0; i < count; i++)
        {
            try {
                polylines[i] = getPath(googleStepsJson.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return polylines;
    }

    public String getPath(JSONObject googlePathJson)
    {
        String polyline = "";
        try {
            polyline = googlePathJson.getJSONObject("polyline").getString("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polyline;
    }
}
