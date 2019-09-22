package com.example.carwashplaces;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BackgroundTask {
    private Context context;
    private ArrayList<ModelLocation> locationArrayList = new ArrayList<>();
    String json_url = "http://histogenetic-exhaus.000webhostapp.com/location.php";

    public BackgroundTask(Context context) {
        this.context = context;
    }

    public ArrayList<ModelLocation> getLocationArrayList(final LocationCallback locationCallback){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest (
                Request.Method.POST,
                json_url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                       int count = 0;
                       while (count<response.length()){

                           try {
                               JSONObject jsonObject = response.getJSONObject(count);
                               ModelLocation modelLocation = new ModelLocation(
                                       jsonObject.getString("id"),
                                       jsonObject.getString("name"),
                                       jsonObject.getString("latitude"),
                                       jsonObject.getString("longitude"),
                                       jsonObject.getString("staff"),
                                       jsonObject.getString("comment"),
                                       jsonObject.getString("phone"));
                               locationArrayList.add(modelLocation);
                               count++;

                           } catch (JSONException e) {
                               e.printStackTrace();
                           }
                       }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(context, "Error fetching car wash places...  ", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });

        MySingleton.getInstance(context).addToRequestque(jsonArrayRequest);

        ArrayList<ModelLocation> locations = locationArrayList;
        locationCallback.onSuccess(locations);
        return locations;
    }
}
