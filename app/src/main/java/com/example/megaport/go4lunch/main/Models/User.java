package com.example.megaport.go4lunch.main.Models;

import android.support.annotation.Nullable;


public class User {
    private String uid;
    private String username;
    @Nullable
    private String urlPicture;
    private int searchRadius;
    private int defaultZoom;
    private boolean isNotificationOn;

    public User() { }

    public User(String uid){
        this.uid = uid;
    }

    public User(String uid, String username, @Nullable String urlPicture, int searchRadius, int defaultZoom, boolean isNotificationOn) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.searchRadius = searchRadius;
        this.defaultZoom = defaultZoom;
        this.isNotificationOn = isNotificationOn;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    @Nullable
    public String getUrlPicture() { return urlPicture; }

    public int getSearchRadius() {
        return searchRadius;
    }

    public int getDefaultZoom() {
        return defaultZoom;
    }

    public boolean isNotificationOn() {
        return isNotificationOn;
    }

    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setUid(String uid) { this.uid = uid; }
    public void setUrlPicture(@Nullable String urlPicture) { this.urlPicture = urlPicture; }

    public void setSearchRadius(int searchRadius) {
        this.searchRadius = searchRadius;
    }

    public void setDefaultZoom(int defaultZoom) {
        this.defaultZoom = defaultZoom;
    }

    public void setNotificationOn(boolean notificationOn) {
        isNotificationOn = notificationOn;
    }
}
