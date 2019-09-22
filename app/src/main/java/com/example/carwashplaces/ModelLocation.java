package com.example.carwashplaces;

import java.util.HashMap;

public class ModelLocation extends HashMap<String, String> {
    private String name, staff, latitude, longitude, id, phone, comment;

    public ModelLocation() {

    }

    public ModelLocation(String name, String staff, String latitude, String longitude, String id, String phone, String comment) {
        this.name = name;
        this.staff = staff;
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.phone = phone;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStaff() {
        return staff;
    }

    public void setStaff(String staff) {
        this.staff = staff;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
