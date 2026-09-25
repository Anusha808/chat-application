package com.chatapp.server;

import com.chatapp.dao.GroupDAO;
import com.chatapp.model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    // =========================================================
    // CLIENT CONNECTION
    // =========================================================

    private final Socket socket;

    private BufferedReader input;

    private PrintWriter output;

    private String username;

    // =========================================================
    // GROUP DAO
    // =========================================================

    private final GroupDAO groupDAO =
            new GroupDAO();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ClientHandler(
            Socket socket
    ) {

        this.socket = socket;
    }

    // =========================================================
    // RUN
    // =========================================================

    @Override
    public void run() {

        try {

            input =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()
                            )
                    );

            output =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true
                    );

            // =================================================
            // WELCOME
            // =================================================

            output.println(
                    "Connected to Chat Server!"
            );

            // =================================================
            // RECEIVE USERNAME
            // =================================================

            username =
                    input.readLine();

            if (username == null
                    || username.isBlank()) {

                disconnect();

                return;
            }

            username =
                    username.trim();

            // =================================================
            // LOGIN SUCCESS
            // =================================================

            output.println(
                    "LOGIN_SUCCESS"
            );

            // =================================================
            // ADD USER TO SERVER
            // =================================================

            ChatServer.addUser(
                    username,
                    this
            );

            System.out.println(
                    "Client logged in: "
                            + username
            );

            // =================================================
            // LISTEN FOR MESSAGES
            // =================================================

            String message;

            while ((message =
                    input.readLine()) != null) {

                if (message.isBlank()) {

                    continue;
                }

                System.out.println(
                        "Message from "
                                + username
                                + ": "
                                + message
                );

                // =================================================
                // GROUP CREATED
                // =================================================

                if (message.startsWith(
                        "GROUP_CREATED|"
                )) {

                    handleGroupCreated(
                            message
                    );

                    continue;
                }

                // =================================================
                // GROUP MEMBER ADDED
                // =================================================

                if (message.startsWith(
                        "GROUP_MEMBER_ADDED|"
                )) {

                    handleGroupMemberAdded(
                            message
                    );

                    continue;
                }

                // =================================================
                // GROUP MEMBER REMOVED
                // =================================================

                if (message.startsWith(
                        "GROUP_MEMBER_REMOVED|"
                )) {

                    handleGroupMemberRemoved(
                            message
                    );

                    continue;
                }

                // =================================================
                // GROUP MEMBER LEFT
                // =================================================

                if (message.startsWith(
                        "GROUP_MEMBER_LEFT|"
                )) {

                    handleGroupMemberLeft(
                            message
                    );

                    continue;
                }

                // =================================================
                // GROUP DELETED
                // =================================================

                if (message.startsWith(
                        "GROUP_DELETED|"
                )) {

                    handleGroupDeleted(
                            message
                    );

                    continue;
                }

                // =================================================
                // GROUP RENAMED
                // =================================================

                if (message.startsWith(
                        "GROUP_RENAMED|"
                )) {

                    handleGroupRenamed(
                            message
                    );

                    continue;
                }

                // =================================================
                // FILE / IMAGE MESSAGE
                // =================================================

                if (message.startsWith(
                        "FILE|"
                )) {

                    handleFileMessage(
                            message
                    );

                    continue;
                }

                // =================================================
                // GROUP MESSAGE
                // =================================================

                if (message.startsWith(
                        "GROUP|"
                )) {

                    handleGroupMessage(
                            message
                    );

                    continue;
                }

                // =================================================
                // PRIVATE TEXT MESSAGE
                // =================================================

                handlePrivateMessage(
                        message
                );
            }

        } catch (IOException e) {

            System.out.println(
                    "Client connection error for "
                            + username
                            + ": "
                            + e.getMessage()
            );

        } finally {

            // =================================================
            // REMOVE USER
            // =================================================

            if (username != null) {

                ChatServer.removeUser(
                        username
                );
            }

            disconnect();
        }
    }

    // =========================================================
    // PRIVATE TEXT MESSAGE
    // =========================================================

    private void handlePrivateMessage(
            String message
    ) {

        String[] parts =
                message.split(
                        "\\|",
                        2
                );

        if (parts.length < 2) {

            System.out.println(
                    "Invalid private message."
            );

            return;
        }

        String receiverUsername =
                parts[0].trim();

        String chatMessage =
                parts[1].trim();

        if (receiverUsername.isEmpty()) {

            return;
        }

        // =====================================================
        // FIND RECEIVER
        // =====================================================

        ClientHandler receiver =
                ChatServer.getUser(
                        receiverUsername
                );

        if (receiver != null) {

            // =================================================
            // SEND MESSAGE TO RECEIVER
            // =================================================

            receiver.sendMessage(
                    username
                            + "|"
                            + chatMessage
            );

            // =================================================
            // INFORM SENDER
            // =================================================

            sendMessage(
                    "MESSAGE_SENT"
            );

            System.out.println(
                    "Message delivered from "
                            + username
                            + " to "
                            + receiverUsername
            );

        } else {

            // =================================================
            // RECEIVER OFFLINE
            // =================================================

            sendMessage(
                    "USER_OFFLINE|"
                            + receiverUsername
            );

            System.out.println(
                    "User offline: "
                            + receiverUsername
            );
        }
    }

    // =========================================================
    // FILE / IMAGE MESSAGE
    // =========================================================

    private void handleFileMessage(
            String message
    ) {

        /*
         * Expected format:
         *
         * FILE|
         * receiverUsername|
         * fileName|
         * fileType|
         * filePath
         */

        String[] parts =
                message.split(
                        "\\|",
                        5
                );

        if (parts.length < 5) {

            System.out.println(
                    "Invalid file message."
            );

            sendMessage(
                    "ERROR|Invalid file message."
            );

            return;
        }

        String receiverUsername =
                parts[1].trim();

        String fileName =
                parts[2].trim();

        String fileType =
                parts[3].trim();

        String filePath =
                parts[4].trim();

        // =====================================================
        // CHECK FILE
        // =====================================================

        File file =
                new File(
                        filePath
                );

        if (!file.exists()) {

            System.out.println(
                    "File does not exist: "
                            + filePath
            );

            sendMessage(
                    "ERROR|File does not exist on server."
            );

            return;
        }

        // =====================================================
        // FIND RECEIVER
        // =====================================================

        ClientHandler receiver =
                ChatServer.getUser(
                        receiverUsername
                );

        if (receiver == null) {

            sendMessage(
                    "USER_OFFLINE|"
                            + receiverUsername
            );

            System.out.println(
                    "File receiver is offline: "
                            + receiverUsername
            );

            return;
        }

        // =====================================================
        // SEND FILE INFORMATION TO RECEIVER
        // =====================================================

        String fileMessage =
                "FILE_MESSAGE|"
                        + username
                        + "|"
                        + fileName
                        + "|"
                        + fileType
                        + "|"
                        + filePath;

        receiver.sendMessage(
                fileMessage
        );

        // =====================================================
        // INFORM SENDER
        // =====================================================

        sendMessage(
                "FILE_SENT"
        );

        System.out.println(
                "================================="
        );

        System.out.println(
                "FILE MESSAGE DELIVERED"
        );

        System.out.println(
                "Sender: "
                        + username
        );

        System.out.println(
                "Receiver: "
                        + receiverUsername
        );

        System.out.println(
                "File: "
                        + fileName
        );

        System.out.println(
                "Type: "
                        + fileType
        );

        System.out.println(
                "Path: "
                        + filePath
        );

        System.out.println(
                "================================="
        );
    }

    // =========================================================
    // GROUP MESSAGE
    // =========================================================

    private void handleGroupMessage(
            String message
    ) {

        String[] parts =
                message.split(
                        "\\|",
                        3
                );

        if (parts.length < 3) {

            System.out.println(
                    "Invalid group message."
            );

            return;
        }

        int groupId;

        try {

            groupId =
                    Integer.parseInt(
                            parts[1].trim()
                    );

        } catch (NumberFormatException e) {

            System.out.println(
                    "Invalid group ID."
            );

            return;
        }

        String groupMessage =
                parts[2];

        // =====================================================
        // GET GROUP MEMBERS
        // =====================================================

        List<User> members =
                groupDAO.getGroupMembers(
                        groupId
                );

        // =====================================================
        // SEND TO ALL ONLINE MEMBERS
        // =====================================================

        for (User member :
                members) {

            ClientHandler receiver =
                    ChatServer.getUser(
                            member.getUsername()
                    );

            if (receiver != null) {

                receiver.sendMessage(
                        "GROUP_MESSAGE|"
                                + groupId
                                + "|"
                                + username
                                + "|"
                                + groupMessage
                );
            }
        }

        // =====================================================
        // CONFIRM TO SENDER
        // =====================================================

        sendMessage(
                "GROUP_MESSAGE_SENT"
        );
    }

    // =========================================================
    // GROUP CREATED
    // =========================================================

    private void handleGroupCreated(
            String message
    ) {

        String[] parts =
                message.split(
                        "\\|",
                        2
                );

        if (parts.length < 2) {

            return;
        }

        String groupId =
                parts[1];

        System.out.println(
                "Group created notification: "
                        + groupId
        );

        broadcastToAllExceptSender(
                "GROUP_CREATED|"
                        + groupId
        );
    }

    // =========================================================
    // GROUP MEMBER ADDED
    // =========================================================

    private void handleGroupMemberAdded(
            String message
    ) {

        String[] parts =
                message.split(
                        "\\|",
                        3
                );

        if (parts.length < 3) {

            return;
        }

        String groupId =
                parts[1];

        String userId =
                parts[2];

        System.out.println(
                "Group member added: group="
                        + groupId
                        + ", user="
                        + userId
        );

        broadcastToAllExceptSender(
                "GROUP_MEMBER_ADDED|"
                        + groupId
                        + "|"
                        + userId
        );
    }

    // =========================================================
    // GROUP MEMBER REMOVED
    // =========================================================

    private void handleGroupMemberRemoved(
            String message
    ) {

        String[] parts =
                message.split(
                        "\\|",
                        3
                );

        if (parts.length < 3) {

            return;
        }

        String groupId =
                parts[1];

        String userId =
                parts[2];

        System.out.println(
                "Group member removed: group="
                        + groupId
                        + ", user="
                        + userId
        );

        broadcastToAllExceptSender(
                "GROUP_MEMBER_REMOVED|"
                        + groupId
                        + "|"
                        + userId
        );
    }

    // =========================================================
    // GROUP MEMBER LEFT
    // =========================================================

    private void handleGroupMemberLeft(
            String message
    ) {

        String[] parts =
                message.split(
                        "\\|",
                        3
                );

        if (parts.length < 3) {

            return;
        }

        String groupId =
                parts[1];

        String userId =
                parts[2];

        System.out.println(
                "Group member left: group="
                        + groupId
                        + ", user="
                        + userId
        );

        broadcastToAllExceptSender(
                "GROUP_MEMBER_LEFT|"
                        + groupId
                        + "|"
                        + userId
        );
    }

    // =========================================================
    // GROUP DELETED
    // =========================================================

    private void handleGroupDeleted(
            String message
    ) {

        String[] parts =
                message.split(
                        "\\|",
                        2
                );

        if (parts.length < 2) {

            return;
        }

        String groupId =
                parts[1];

        System.out.println(
                "Group deleted: "
                        + groupId
        );

        broadcastToAllExceptSender(
                "GROUP_DELETED|"
                        + groupId
        );
    }

    // =========================================================
    // GROUP RENAMED
    // =========================================================

    private void handleGroupRenamed(
            String message
    ) {

        String[] parts =
                message.split(
                        "\\|",
                        3
                );

        if (parts.length < 3) {

            return;
        }

        String groupId =
                parts[1];

        String newGroupName =
                parts[2];

        System.out.println(
                "Group renamed: "
                        + groupId
                        + " -> "
                        + newGroupName
        );

        broadcastToAllExceptSender(
                "GROUP_RENAMED|"
                        + groupId
                        + "|"
                        + newGroupName
        );
    }

    // =========================================================
    // BROADCAST TO ALL EXCEPT CURRENT USER
    // =========================================================

    private void broadcastToAllExceptSender(
            String message
    ) {

        for (ClientHandler client :
                ChatServer.getConnectedUsers()
                        .values()) {

            if (client != this) {

                client.sendMessage(
                        message
                );
            }
        }
    }

    // =========================================================
    // SEND MESSAGE
    // =========================================================

    public void sendMessage(
            String message
    ) {

        if (output == null) {

            return;
        }

        output.println(
                message
        );
    }

    // =========================================================
    // DISCONNECT
    // =========================================================

    private void disconnect() {

        try {

            if (input != null) {

                input.close();
            }

        } catch (IOException e) {

            System.out.println(
                    "Error closing input stream."
            );
        }

        try {

            if (output != null) {

                output.close();
            }

        } catch (Exception e) {

            System.out.println(
                    "Error closing output stream."
            );
        }

        try {

            if (socket != null
                    && !socket.isClosed()) {

                socket.close();
            }

        } catch (IOException e) {

            System.out.println(
                    "Error closing socket."
            );
        }
    }
}