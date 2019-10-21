package com.interstellarstudios.hive.models;

public class Message {

    private String message;
    private long timeStamp;
    private boolean sender;
    private boolean seen;

    public Message() {
        //empty constructor needed
    }

    public Message(String message, long timeStamp, boolean sender, boolean seen) {

        this.message = message;
        this.timeStamp = timeStamp;
        this.sender = sender;
        this.seen = seen;
    }

    public String getMessage() {
        return message;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public boolean getSender() {
        return sender;
    }

    public boolean getSeen() {
        return seen;
    }
}
