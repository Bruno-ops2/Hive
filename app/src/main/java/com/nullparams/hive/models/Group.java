package com.nullparams.hive.models;

public class Group {

    private String groupName;
    private String groupPicUrl;
    private String groupAbout;
    private String groupAdmin;

    public Group() {
        //empty constructor needed
    }

    public Group(String groupName, String groupPicUrl, String groupAbout, String groupAdmin) {

        this.groupName = groupName;
        this.groupPicUrl = groupPicUrl;
        this.groupAbout = groupAbout;
        this.groupAdmin = groupAdmin;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupPicUrl() {
        return groupPicUrl;
    }

    public String getGroupAbout() {
        return groupAbout;
    }

    public String getGroupAdmin() {
        return groupAdmin;
    }
}
