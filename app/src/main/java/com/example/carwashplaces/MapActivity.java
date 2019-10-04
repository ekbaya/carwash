package com.example.carwashplaces;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.skyfishjy.library.RippleBackground;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private GeoApiContext geoApiContext = null;
    private Polyline polyline;
    private Marker textBubble;
    private Location mLastKnownLocation;

    private List<AutocompletePrediction> predictionList;
    private ArrayList<ModelLocation> locations;
    private BackgroundTask backgroundTask;


    private LocationCallback locationCallback;

    private MaterialSearchBar materialSearchBar;

    //views
    private View mapView;
    private Button btnFind, directionBtn, navigationBtn, callBtn;
    private ImageButton closeBtn;
    private TextView nameTv, staffNameTv, descriptionTv;
    private LinearLayout infoBottomSheet;

    private ProgressDialog progressDialog;




    private RippleBackground rippleBg;

    private final float DEFAULT_ZOOM = 18;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //init views
        materialSearchBar = findViewById(R.id.searchBar);
        btnFind = findViewById(R.id.btnFind);
        directionBtn = findViewById(R.id.directionBtn);
        navigationBtn = findViewById(R.id.navigationBtn);
        callBtn = findViewById(R.id.callBtn);
        closeBtn = findViewById(R.id.closeBtn);
        nameTv = findViewById(R.id.nameTv);
        staffNameTv = findViewById(R.id.staffNameTv);
        descriptionTv = findViewById(R.id.descriptionTv);
        infoBottomSheet = findViewById(R.id.infoBottomSheet);
        rippleBg = findViewById(R.id.ripple_bg);

        progressDialog = new ProgressDialog(this);

        //Hide information window
        infoBottomSheet.setVisibility(View.GONE);

        //Fetching locations array
        locations = new ArrayList<>();
        backgroundTask = new BackgroundTask(MapActivity.this);
        backgroundTask.getLocationArrayList(fetchedLocations -> {
            locations = fetchedLocations;


            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(MapActivity.this);
            mapView = mapFragment.getView();

            if (geoApiContext == null){
                geoApiContext = new GeoApiContext.Builder()
                        .apiKey(getString(R.string.google_maps_api_key))
                        .build();
            }

            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
            Places.initialize(MapActivity.this, getString(R.string.google_maps_api_key));
            placesClient = Places.createClient(MapActivity.this);
            polyline = null;
            textBubble = null;
        });

        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION){
                    //opening or closing a  navigation drawer
                }else if (buttonCode == MaterialSearchBar.BUTTON_BACK){
                    materialSearchBar.disableSearch();
                }
            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("ke")
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                if (placesClient != null){
                    placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            //predictions found, find out what suggestions we have from google
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null){
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                //converting predictionList into a list of string
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++){
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                //pass suggestion list to our MaterialSearchBar
                                materialSearchBar.updateLastSuggestions(suggestionsList);
                                if (!materialSearchBar.isSuggestionsVisible()){
                                    materialSearchBar.showSuggestionsList();
                                }
                            }
                        }
                        else {
                            //some error
                            Log.i("mytag", "prediction fetching task failed");
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBar.setSuggstionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                // seek the longitude and latitude of that suggestion
                if (position >= predictionList.size()){
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(() -> materialSearchBar.clearSuggestions(),1000);

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null){
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);// get latitude and longitude of a place

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(fetchPlaceResponse -> {
                    // place found, update camera Factory
                    Place place = fetchPlaceResponse.getPlace();
                    Log.i("mytag", "place found: "+place.getName());
                    LatLng latLngOfPlace = place.getLatLng();
                    if (latLngOfPlace != null){
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM));
                    }
                }).addOnFailureListener(e -> {
                    if (e instanceof ApiException){
                        ApiException apiException = (ApiException) e;
                        apiException.printStackTrace();
                        int statusCode = apiException.getStatusCode();
                        Log.i("mytag", "place not found: " +e.getMessage());
                        Log.i("mytag", "status code: "+statusCode);
                    }
                });

            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

        btnFind.setOnClickListener(v -> {
            if (mMap != null){
                //get the current location of the marker
                LatLng currentMarkerLocation = mMap.getCameraPosition().target;
                rippleBg.startRippleAnimation();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rippleBg.stopRippleAnimation();
                        getNearestMarkerFromCurrentLocation(locations, currentMarkerLocation);

                    }
                },3000);
            }
        });

    }

    private void getNearestMarkerFromCurrentLocation(ArrayList<ModelLocation> places, LatLng currentMarkerLocation){
        Marker mClosestMarker = null;
        float minDist = 0;

        // Looping through the location arrayList
        for (int i = 0; i < places.size(); i++) {
            // Create a marker for each location in the arrayList data.
            double lat = Double.parseDouble(places.get(i).getLatitude());
            double lon = Double.parseDouble(places.get(i).getLongitude());
            Marker currentMarker = mMap.addMarker(new MarkerOptions()
                    .title(places.get(i).getName())
                    .snippet(places.get(i).getComment())
                    .position(new LatLng(
                            lat,
                            lon
                    ))
            );

            float[] distance = new float[1];
            Location.distanceBetween(currentMarkerLocation.latitude, currentMarkerLocation.longitude, lat, lon, distance);
            if(i==0) {
                minDist = distance[0];
            } else if (minDist > distance[0]) {
                minDist=distance[0];
                mClosestMarker = currentMarker;
            }
        }

        int distanceKm = (int)minDist/1000;//converting distance into km , float to integer to eliminate decimals
        //move camera to current location
        if (mClosestMarker != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mClosestMarker.getPosition().latitude, mClosestMarker.getPosition().longitude), 18));
            Toast.makeText(MapActivity.this, "Nearest Car Wash and Distance: "+ mClosestMarker.getTitle() + " " + distanceKm+" Km", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "Error getting the nearest Car Wash...", Toast.LENGTH_SHORT).show();
        }
    }

    //Called when the map is loaded
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (mapView != null  && mapView.findViewById(Integer.parseInt("1")) != null){
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 180);
        }

        //check if gps is enabled or not and then request user to enable it
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(MapActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(MapActivity.this, locationSettingsResponse -> {
            getDeviceLocation();
            setMarkersToCarWashes();
        });

        task.addOnFailureListener(MapActivity.this, e -> {
            if (e instanceof ResolvableApiException){
                ResolvableApiException resolvable = (ResolvableApiException) e;
                try {
                    resolvable.startResolutionForResult(MapActivity.this, 51);
                } catch (IntentSender.SendIntentException ex) {
                    ex.printStackTrace();
                }
            }
        });

        mMap.setOnMyLocationButtonClickListener(() -> {
            if (materialSearchBar.isSuggestionsVisible())
                materialSearchBar.clearSuggestions();
            if (materialSearchBar.isSearchEnabled())
                materialSearchBar.disableSearch();
            return false;
        });

        setMarkersToCarWashes();
    }

    private void setMarkersToCarWashes() {
        if (locations != null){
            for (int i = 0; i < locations.size(); i++){
                double latitude = Double.parseDouble(locations.get(i).getLatitude());
                double longitude = Double.parseDouble(locations.get(i).getLongitude());

                //set markers to a car wash places
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))).setTag(i);

                //Handling marker on clicks listener
                mMap.setOnMarkerClickListener(marker -> {
                    if (polyline != null){
                        polyline.remove();
                    }

                    infoBottomSheet.setVisibility(View.VISIBLE);
                    btnFind.setVisibility(View.GONE);

                        final int position = (int) marker.getTag();//getting the position of the clicked marker
                        String name = locations.get(position).getName();
                        String staff = locations.get(position).getStaff();
                        String description = locations.get(position).getComment();

                        nameTv.setText(name);
                        staffNameTv.setText("Staff: "+staff);
                        descriptionTv.setText("About: "+description);

                        //Handling call button click
                        callBtn.setOnClickListener(v -> {
                            String phone = locations.get(position).getPhone();
                            startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", "0"+phone, null)));
                        });

                    //Handling close button click
                        closeBtn.setOnClickListener(v -> {
                            infoBottomSheet.setVisibility(View.GONE);
                            btnFind.setVisibility(View.VISIBLE);
                            //remove polyline if present
                            if (polyline != null){
                                polyline.remove();
                            }
                            //remove textBubble if present
                            if (textBubble != null){
                                textBubble.remove();
                            }
                        });

                    //Handling direction button click
                    directionBtn.setOnClickListener(v -> calculateDirections(position));

                    //Handling navigation button click
                    navigationBtn.setOnClickListener(v -> {
                        if (polyline != null){
                            startGoogleMapNavigationActivity(position);
                        }
                        else {
                            Toast.makeText(MapActivity.this, "Get direction first before you navigate...", Toast.LENGTH_SHORT).show();
                        }
                    });

                    return false;
                });
            }
        }
    }

    private void startGoogleMapNavigationActivity(int index) {
        Uri navigationIntentUri = Uri.parse("google.navigation:q=" + Double.parseDouble(locations.get(index).getLatitude()) +"," + Double.parseDouble(locations.get(index).getLongitude()));//creating intent with latlng
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, navigationIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        /*For completeness, if the user doesn't have the maps app installed then it's going to be a
        good idea to catch the ActivityNotFoundException, then we can start the activity again
        without the maps app restriction, we can be pretty sure that we will never get to the
        Toast at the end since an internet browser is a valid application to launch this url scheme too.*/

        try
        {
            startActivity(mapIntent); // launching maps from a map application
        }
        catch(ActivityNotFoundException ex)
        {
            try
            {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, navigationIntentUri); // try to launch map using browser
                startActivity(unrestrictedIntent);
            }
            catch(ActivityNotFoundException innerEx)
            {
                Toast.makeText(this, "Please install a map application", Toast.LENGTH_LONG).show();// app is not installed and browser failed to launch the map
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
                setMarkersToCarWashes();
            }
        }
    }


    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()){
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null){
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback(){
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null){
                                            return;
                                        }
                                        mLastKnownLocation = locationResult.getLastLocation();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        }else {
                            Toast.makeText(MapActivity.this, "Unable to get last Location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void calculateDirections(Integer index){
        progressDialog.setMessage("Looking  for possible routes...");
        progressDialog.show();

        Log.d("mytag", "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                Double.parseDouble(locations.get(index).getLatitude()),
                Double.parseDouble(locations.get(index).getLongitude())
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mLastKnownLocation.getLatitude(),
                        mLastKnownLocation.getLongitude()
                )
        );
        Log.d("mytag", "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                progressDialog.dismiss();
                //getting the results of directions
                Log.d("mytag", "calculateDirections: routes: " + result.routes[0].toString());
                Log.d("mytag", "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d("mytag", "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d("mytag", "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                addPolylinesToMap(result, index);
            }

            @Override
            public void onFailure(Throwable e) {
                progressDialog.dismiss();
                Log.e("mytag", "calculateDirections: Failed to get directions: " + e.getMessage() );
                Toast.makeText(MapActivity.this, "Error getting routes...", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result, int position){
        new Handler(Looper.getMainLooper()).post(() -> {
            Log.d("mytag", "run: result routes: " + result.routes.length);

            for(DirectionsRoute route: result.routes){
                Log.d("mytag", "run: leg: " + route.legs[0].toString());
                List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                List<LatLng> newDecodedPath = new ArrayList<>();

                // This loops through all the LatLng coordinates of ONE polyline.
                for(com.google.maps.model.LatLng latLng: decodedPath){

                    newDecodedPath.add(new LatLng(
                            latLng.lat,
                            latLng.lng
                    ));
                }
                // drawing polyline from current location to destination
                polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                polyline.setColor(ContextCompat.getColor(MapActivity.this, R.color.colorBlue));

                //Get and set latitude and longitude of the current location
                final LatLng origin = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                //Get and set latitude and longitude of the clicked marker
                final LatLng destination = new LatLng(Double.parseDouble(locations.get(position).getLatitude()),Double.parseDouble(locations.get(position).getLongitude()));

                getDestinationInfo(origin, destination);

            }

            //move camera to current location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), 10));
        });
    }

    private void getDestinationInfo(LatLng origin, LatLng destination) {
        String serverKey = getResources().getString(R.string.google_maps_api_key);
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .transitMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        String status = direction.getStatus();
                        if (status.equals(RequestResult.OK)){
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);
                            Info distanceInfo = leg.getDistance();
                            Info durationInfo = leg.getDuration();

                            String distance = distanceInfo.getText();
                            String duration = durationInfo.getText();

                            //creating a text bubble to display distance and time
                            final LatLng TEXT_BUBBLE = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            textBubble = mMap.addMarker(new MarkerOptions()
                                    .position(TEXT_BUBBLE)
                                    .title(distance)
                                    .snippet(duration));
                            textBubble.showInfoWindow();
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Toast.makeText(MapActivity.this, "Sorry some error occurred...", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
