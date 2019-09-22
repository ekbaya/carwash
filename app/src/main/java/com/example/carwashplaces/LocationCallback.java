package com.example.carwashplaces;

import java.util.ArrayList;

public interface LocationCallback {
    void onSuccess(ArrayList<ModelLocation> fetchedLocations);
}
