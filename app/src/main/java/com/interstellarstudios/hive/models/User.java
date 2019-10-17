package com.interstellarstudios.hive.models;

public class User {

    private String id;
    private String username;
    private String profilePicUrl;
    private String onlineOffline;
    private String status;
    private String emailAddress;

    public User() {
        //empty constructor needed
    }

    public User(String id, String username, String profilePicUrl, String onlineOffline, String status, String emailAddress) {

        this.id = id;
        this.username = username;
        this.profilePicUrl = profilePicUrl;
        this.onlineOffline = onlineOffline;
        this.status = status;
        this.emailAddress = emailAddress;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public String getOnlineOffline() {
        return onlineOffline;
    }

    public String getStatus() {
        return status;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
}
