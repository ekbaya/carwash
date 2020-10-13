package com.example.carwashplaces.services;

import com.example.carwashplaces.models.Location;

import java.util.ArrayList;

public interface LocationCallback {
    void onSuccess(ArrayList<Location> fetchedLocations);
}
