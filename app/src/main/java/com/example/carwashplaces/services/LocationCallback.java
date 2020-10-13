package com.example.carwashplaces.services;

import com.example.carwashplaces.models.ModelLocation;

import java.util.ArrayList;

public interface LocationCallback {
    void onSuccess(ArrayList<ModelLocation> fetchedLocations);
}
