package com.lazypanda.models;

public class TypingStatus {
    private String senderId;
    private boolean isTyping;

    public TypingStatus(String senderId, boolean isTyping) {
        this.senderId = senderId;
        this.isTyping = isTyping;
    }

    public String getSenderId() {
        return senderId;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }
}
