package com.chatapp.model;

import java.time.LocalDateTime;

public class GroupMessage {

    private int id;
    private int groupId;
    private int senderId;
    private String message;
    private LocalDateTime sentAt;

    public GroupMessage() {
    }

    public GroupMessage(
            int groupId,
            int senderId,
            String message
    ) {
        this.groupId = groupId;
        this.senderId = senderId;
        this.message = message;
    }

    public GroupMessage(
            int id,
            int groupId,
            int senderId,
            String message,
            LocalDateTime sentAt
    ) {
        this.id = id;
        this.groupId = groupId;
        this.senderId = senderId;
        this.message = message;
        this.sentAt = sentAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}