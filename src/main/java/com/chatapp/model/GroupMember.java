package com.chatapp.model;

import java.time.LocalDateTime;

public class GroupMember {

    private int id;
    private int groupId;
    private int userId;
    private String username;
    private String email;
    private LocalDateTime joinedAt;


    // =========================================================
    // DEFAULT CONSTRUCTOR
    // =========================================================

    public GroupMember() {
    }


    // =========================================================
    // GETTERS AND SETTERS
    // =========================================================

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


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}