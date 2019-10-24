package com.interstellarstudios.hive.models;

public class Message {

    private String message;
    private long timeStamp;
    private boolean isSender;
    private boolean seen;
    private String senderId;
    private String receiverId;
    private String senderUsername;
    private boolean isRead;

    public Message() {
        //empty constructor needed
    }

    public Message(String message, long timeStamp, boolean isSender, boolean seen, String senderId, String receiverId, String senderUsername, boolean isRead) {

        this.message = message;
        this.timeStamp = timeStamp;
        this.isSender = isSender;
        this.seen = seen;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderUsername = senderUsername;
        this.isRead = isRead;
    }

    public String getMessage() {
        return message;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public boolean getIsSender() {
        return isSender;
    }

    public boolean getSeen() {
        return seen;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public boolean getIsRead() {
        return isRead;
    }
}
