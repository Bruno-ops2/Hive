package com.nullparams.hive.models;

public class GroupParticipant {

    private String id;

    public GroupParticipant() {
        //empty constructor needed
    }

    public GroupParticipant(String id) {

        this.id = id;
    }

    public String getId() {
        return id;
    }
}
