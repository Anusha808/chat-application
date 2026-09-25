package com.chatapplication.network;

import java.io.Serializable;

public class GroupMemberNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    private MessageType type;

    private int groupId;

    private String groupName;

    private int userId;

    private String username;

    private String action;

    public GroupMemberNotification() {
    }

    public GroupMemberNotification(
            MessageType type,
            int groupId,
            String groupName,
            int userId,
            String username,
            String action) {

        this.type = type;
        this.groupId = groupId;
        this.groupName = groupName;
        this.userId = userId;
        this.username = username;
        this.action = action;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {

        return "GroupMemberNotification{" +
                "type=" + type +
                ", groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}