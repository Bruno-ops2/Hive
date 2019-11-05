package com.interstellarstudios.hive.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages_table")
public class MessageEntity {

    @PrimaryKey(autoGenerate = true)
    private int autoGenId;

    private String messageId;
    private String message;
    private long timeStamp;
    private boolean isSender;
    private boolean seen;
    private String senderId;
    private String receiverId;
    private String senderUsername;
    private boolean isRead;
    private String messageType;
    private String imageUrl;
    private String imageFileName;

    public MessageEntity (String messageId, String message, long timeStamp, boolean isSender, boolean seen, String senderId, String receiverId, String senderUsername, boolean isRead, String messageType, String imageUrl, String imageFileName) {

        this.messageId = messageId;
        this.message = message;
        this.timeStamp = timeStamp;
        this.isSender = isSender;
        this.seen = seen;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderUsername = senderUsername;
        this.isRead = isRead;
        this.messageType = messageType;
        this.imageUrl = imageUrl;
        this.imageFileName = imageFileName;
    }

    public void setAutoGenId(int autoGenId) {
        this.autoGenId = autoGenId;
    }

    public int getAutoGenId() {
        return autoGenId;
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

    public String getMessageType() {
        return messageType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageFileName() {
        return imageFileName;
    }
}
