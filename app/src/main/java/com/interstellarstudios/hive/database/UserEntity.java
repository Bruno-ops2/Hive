package com.interstellarstudios.hive.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users_table")
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    private int autoGenId;

    private String id;
    private String username;
    private String profilePicUrl;
    private String onlineOffline;
    private String status;
    private String emailAddress;

    public UserEntity (String id, String username, String profilePicUrl, String onlineOffline, String status, String emailAddress) {

        this.id = id;
        this.username = username;
        this.profilePicUrl = profilePicUrl;
        this.onlineOffline = onlineOffline;
        this.status = status;
        this.emailAddress = emailAddress;
    }

    public void setAutoGenId(int autoGenId) {
        this.autoGenId = autoGenId;
    }

    public int getAutoGenId() {
        return autoGenId;
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
