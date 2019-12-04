package com.nullparams.hive.models;

public class GroupMessage {

    private String messageId;
    private String message;
    private long timeStamp;
    private boolean isSender;
    private String senderId;
    private String senderUsername;
    private String messageType;
    private String imageUrl;
    private String imageFileName;
    private String senderProfilePicUrl;

    public GroupMessage() {
        //empty constructor needed
    }

    public GroupMessage(String messageId, String message, long timeStamp, boolean isSender, String senderId, String senderUsername, String messageType, String imageUrl, String imageFileName, String senderProfilePicUrl) {

        this.messageId = messageId;
        this.message = message;
        this.timeStamp = timeStamp;
        this.isSender = isSender;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.messageType = messageType;
        this.imageUrl = imageUrl;
        this.imageFileName = imageFileName;
        this.senderProfilePicUrl = senderProfilePicUrl;
    }

    public String getMessageId() {
        return messageId;
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

    public String getSenderId() {
        return senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public String getSenderProfilePicUrl() {
        return senderProfilePicUrl;
    }
}
