package com.lazypanda.models;

import com.google.firebase.database.PropertyName;

public class ModelChat {

    private String message, receiver, sender, timeStamp;
    private boolean isSeen;

    public ModelChat() {}

    public ModelChat(String message, String receiver, String sender, String timeStamp, boolean isSeen) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.isSeen = isSeen;
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return this.receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public boolean getIsSeen() {
        return this.isSeen;
    }

    public void setIsSeen(boolean isSeen) {
        this.isSeen = isSeen;
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
