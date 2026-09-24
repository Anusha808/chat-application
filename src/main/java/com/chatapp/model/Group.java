package com.chatapp.model;

import java.time.LocalDateTime;

public class Group {

    private int id;
    private String groupName;
    private int createdBy;
    private LocalDateTime createdAt;

    public Group() {
    }

    public Group(
            int id,
            String groupName,
            int createdBy,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.groupName = groupName;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Group(
            String groupName,
            int createdBy
    ) {
        this.groupName = groupName;
        this.createdBy = createdBy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return groupName;
    }
}