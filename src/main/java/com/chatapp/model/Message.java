package com.chatapp.model;

import java.time.LocalDateTime;

public class Message {

    private int id;
    private int senderId;
    private int receiverId;
    private String message;
    private LocalDateTime sentAt;

    // File/Image attachment fields
    private String fileName;
    private String filePath;
    private String fileType;

    public Message() {
    }

    // Constructor for normal text message
    public Message(
            int senderId,
            int receiverId,
            String message
    ) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
    }

    // Constructor for loading messages from database
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

    // Constructor for file/image message
    public Message(
            int senderId,
            int receiverId,
            String message,
            String fileName,
            String filePath,
            String fileType
    ) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
    }

    // Full constructor
    public Message(
            int id,
            int senderId,
            int receiverId,
            String message,
            LocalDateTime sentAt,
            String fileName,
            String filePath,
            String fileType
    ) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.sentAt = sentAt;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
    }

    // ID
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Sender ID
    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    // Receiver ID
    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    // Message
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Sent At
    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    // File Name
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    // File Path
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // File Type
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}