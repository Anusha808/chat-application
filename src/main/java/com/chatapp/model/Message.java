package com.chatapp.model;

import java.time.LocalDateTime;

public class Message {

    private int id;
    private int senderId;
    private int receiverId;
    private String message;
    private LocalDateTime sentAt;

    public Message() {
    }

    public Message(
            int senderId,
            int receiverId,
            String message
    ) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
    }

    public Message(
            int id,
            int senderId,
            int receiverId,
            String message,
            LocalDateTime sentAt
    ) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.sentAt = sentAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
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