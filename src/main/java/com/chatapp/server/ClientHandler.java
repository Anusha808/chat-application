package com.chatapp.server;

import com.chatapp.dao.GroupDAO;
import com.chatapp.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;

    private BufferedReader input;
    private PrintWriter output;

    private String username;

    private final GroupDAO groupDAO = new GroupDAO();


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ClientHandler(Socket socket) {

        this.socket = socket;
    }


    // =========================================================
    // RUN
    // =========================================================

    @Override
    public void run() {

        try {

            input = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            output = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );


            // =================================================
            // SEND WELCOME MESSAGE
            // =================================================

            output.println(
                    "Connected to Chat Server!"
            );


            // =================================================
            // RECEIVE USERNAME
            // =================================================

            username = input.readLine();

            if (username == null
                    || username.trim().isEmpty()) {

                return;
            }

            username = username.trim();


            // =================================================
            // LOGIN SUCCESS
            // =================================================

            output.println(
                    "LOGIN_SUCCESS"
            );


            ChatServer.addUser(
                    username,
                    this
            );


            // =================================================
            // LISTEN FOR CLIENT MESSAGES
            // =================================================

            String message;

            while ((message = input.readLine()) != null) {

                System.out.println(
                        username
                                + " sent: "
                                + message
                );


                // =================================================
                // GROUP CREATED
                // =================================================

                if (message.startsWith(
                        "GROUP_CREATED|"
                )) {

                    handleGroupCreated(message);

                    continue;
                }


                // =================================================
                // GROUP MEMBER ADDED
                // =================================================

                if (message.startsWith(
                        "GROUP_MEMBER_ADDED|"
                )) {

                    handleGroupMemberAdded(message);

                    continue;
                }


                // =================================================
                // GROUP MEMBER REMOVED
                // =================================================

                if (message.startsWith(
                        "GROUP_MEMBER_REMOVED|"
                )) {

                    handleGroupMemberRemoved(message);

                    continue;
                }


                // =================================================
                // GROUP MEMBER LEFT
                // =================================================

                if (message.startsWith(
                        "GROUP_MEMBER_LEFT|"
                )) {

                    handleGroupMemberLeft(message);

                    continue;
                }


                // =================================================
                // GROUP DELETED
                // =================================================

                if (message.startsWith(
                        "GROUP_DELETED|"
                )) {

                    handleGroupDeleted(message);

                    continue;
                }


                // =================================================
                // GROUP RENAMED
                // =================================================

                if (message.startsWith(
                        "GROUP_RENAMED|"
                )) {

                    handleGroupRenamed(message);

                    continue;
                }


                // =================================================
                // GROUP MESSAGE
                // =================================================

                if (message.startsWith(
                        "GROUP|"
                )) {

                    handleGroupMessage(message);

                    continue;
                }


                // =================================================
                // PRIVATE MESSAGE
                // =================================================

                String[] parts =
                        message.split(
                                "\\|",
                                2
                        );


                if (parts.length < 2) {

                    output.println(
                            "ERROR|Invalid message format"
                    );

                    continue;
                }


                String receiverUsername =
                        parts[0].trim();

                String chatMessage =
                        parts[1].trim();


                if (receiverUsername.isEmpty()
                        || chatMessage.isEmpty()) {

                    output.println(
                            "ERROR|Message cannot be empty"
                    );

                    continue;
                }


                ClientHandler receiver =
                        ChatServer.getUser(
                                receiverUsername
                        );


                if (receiver != null) {

                    receiver.sendMessage(
                            username
                                    + "|"
                                    + chatMessage
                    );


                    output.println(
                            "MESSAGE_SENT"
                    );


                    System.out.println(
                            "Private message delivered from "
                                    + username
                                    + " to "
                                    + receiverUsername
                    );

                } else {

                    output.println(
                            "USER_OFFLINE|"
                                    + receiverUsername
                    );


                    System.out.println(
                            "User offline: "
                                    + receiverUsername
                    );
                }
            }


        } catch (IOException e) {

            System.out.println(
                    "Client disconnected: "
                            + e.getMessage()
            );

        } finally {

            ChatServer.removeUser(
                    username
            );


            try {

                socket.close();

            } catch (IOException e) {

                System.out.println(
                        "Error closing client socket."
                );
            }
        }
    }


    // =========================================================
    // HANDLE GROUP CREATED
    // =========================================================

    private void handleGroupCreated(
            String message
    ) {

        try {

            String[] parts =
                    message.split(
                            "\\|",
                            2
                    );


            if (parts.length < 2) {

                output.println(
                        "ERROR|Invalid group creation message"
                );

                return;
            }


            int groupId =
                    Integer.parseInt(
                            parts[1]
                    );


            String notification =
                    "GROUP_CREATED|"
                            + groupId;


            // Notify every connected client.

            for (ClientHandler client :
                    ChatServer
                            .getConnectedUsers()
                            .values()) {

                client.sendMessage(
                        notification
                );
            }


            System.out.println(
                    "Group creation notification broadcast. "
                            + "Group ID: "
                            + groupId
            );


        } catch (NumberFormatException e) {

            output.println(
                    "ERROR|Invalid group ID"
            );


        } catch (Exception e) {

            System.out.println(
                    "Error handling group creation: "
                            + e.getMessage()
            );


            output.println(
                    "ERROR|Could not notify group creation"
            );
        }
    }


    // =========================================================
    // HANDLE GROUP MEMBER ADDED
    // =========================================================

    private void handleGroupMemberAdded(
            String message
    ) {

        try {

            String[] parts =
                    message.split(
                            "\\|",
                            3
                    );


            if (parts.length < 3) {

                output.println(
                        "ERROR|Invalid group member message"
                );

                return;
            }


            int groupId =
                    Integer.parseInt(
                            parts[1]
                    );


            int userId =
                    Integer.parseInt(
                            parts[2]
                    );


            System.out.println(
                    "================================="
            );

            System.out.println(
                    "GROUP MEMBER ADDED"
            );

            System.out.println(
                    "Group ID: "
                            + groupId
            );

            System.out.println(
                    "User ID: "
                            + userId
            );

            System.out.println(
                    "================================="
            );


            String notification =
                    "GROUP_MEMBER_ADDED|"
                            + groupId
                            + "|"
                            + userId;


            // Notify every connected client.

            for (ClientHandler client :
                    ChatServer
                            .getConnectedUsers()
                            .values()) {

                client.sendMessage(
                        notification
                );
            }


            output.println(
                    "GROUP_MEMBER_ADDED_SUCCESS"
            );


            System.out.println(
                    "Group member notification broadcast."
            );


        } catch (NumberFormatException e) {

            output.println(
                    "ERROR|Invalid group or user ID"
            );


        } catch (Exception e) {

            System.out.println(
                    "Error handling group member addition: "
                            + e.getMessage()
            );


            output.println(
                    "ERROR|Could not notify group member addition"
            );
        }
    }


    // =========================================================
    // HANDLE GROUP MEMBER REMOVED
    // =========================================================

    private void handleGroupMemberRemoved(
            String message
    ) {

        try {

            String[] parts =
                    message.split(
                            "\\|",
                            3
                    );


            if (parts.length < 3) {

                output.println(
                        "ERROR|Invalid group member removal message"
                );

                return;
            }


            int groupId =
                    Integer.parseInt(
                            parts[1]
                    );


            int userId =
                    Integer.parseInt(
                            parts[2]
                    );


            System.out.println(
                    "================================="
            );

            System.out.println(
                    "GROUP MEMBER REMOVED"
            );

            System.out.println(
                    "Group ID: "
                            + groupId
            );

            System.out.println(
                    "User ID: "
                            + userId
            );

            System.out.println(
                    "================================="
            );


            String notification =
                    "GROUP_MEMBER_REMOVED|"
                            + groupId
                            + "|"
                            + userId;


            // Notify all connected clients.

            for (ClientHandler client :
                    ChatServer
                            .getConnectedUsers()
                            .values()) {

                client.sendMessage(
                        notification
                );
            }


            output.println(
                    "GROUP_MEMBER_REMOVED_SUCCESS"
            );


            System.out.println(
                    "Group member removal notification broadcast."
            );


        } catch (NumberFormatException e) {

            output.println(
                    "ERROR|Invalid group or user ID"
            );


        } catch (Exception e) {

            System.out.println(
                    "Error handling group member removal: "
                            + e.getMessage()
            );


            output.println(
                    "ERROR|Could not notify group member removal"
            );
        }
    }


    // =========================================================
    // HANDLE GROUP MEMBER LEFT
    // =========================================================

    private void handleGroupMemberLeft(
            String message
    ) {

        try {

            String[] parts =
                    message.split(
                            "\\|",
                            3
                    );


            if (parts.length < 3) {

                output.println(
                        "ERROR|Invalid group member left message"
                );

                return;
            }


            int groupId =
                    Integer.parseInt(
                            parts[1]
                    );


            int userId =
                    Integer.parseInt(
                            parts[2]
                    );


            System.out.println(
                    "================================="
            );

            System.out.println(
                    "GROUP MEMBER LEFT"
            );

            System.out.println(
                    "Group ID: "
                            + groupId
            );

            System.out.println(
                    "User ID: "
                            + userId
            );

            System.out.println(
                    "================================="
            );


            String notification =
                    "GROUP_MEMBER_LEFT|"
                            + groupId
                            + "|"
                            + userId;


            // -------------------------------------------------
            // NOTIFY ALL CONNECTED CLIENTS
            // -------------------------------------------------

            for (ClientHandler client :
                    ChatServer
                            .getConnectedUsers()
                            .values()) {

                client.sendMessage(
                        notification
                );
            }


            // -------------------------------------------------
            // SEND SUCCESS RESPONSE
            // -------------------------------------------------

            output.println(
                    "GROUP_MEMBER_LEFT_SUCCESS"
            );


            System.out.println(
                    "Group member left notification broadcast."
            );


        } catch (NumberFormatException e) {

            output.println(
                    "ERROR|Invalid group or user ID"
            );


        } catch (Exception e) {

            System.out.println(
                    "Error handling group member left: "
                            + e.getMessage()
            );


            output.println(
                    "ERROR|Could not notify group member left"
            );
        }
    }


    // =========================================================
    // HANDLE GROUP DELETED
    // =========================================================

    private void handleGroupDeleted(
            String message
    ) {

        try {

            String[] parts =
                    message.split(
                            "\\|",
                            2
                    );


            if (parts.length < 2) {

                output.println(
                        "ERROR|Invalid group deleted message"
                );

                return;
            }


            int groupId =
                    Integer.parseInt(
                            parts[1]
                    );


            System.out.println(
                    "================================="
            );

            System.out.println(
                    "GROUP DELETED"
            );

            System.out.println(
                    "Group ID: "
                            + groupId
            );

            System.out.println(
                    "================================="
            );


            String notification =
                    "GROUP_DELETED|"
                            + groupId;


            // -------------------------------------------------
            // NOTIFY ALL CONNECTED CLIENTS
            // -------------------------------------------------

            for (ClientHandler client :
                    ChatServer
                            .getConnectedUsers()
                            .values()) {

                client.sendMessage(
                        notification
                );
            }


            // -------------------------------------------------
            // SEND SUCCESS RESPONSE
            // -------------------------------------------------

            output.println(
                    "GROUP_DELETED_SUCCESS"
            );


            System.out.println(
                    "Group deletion notification broadcast."
            );


        } catch (NumberFormatException e) {

            output.println(
                    "ERROR|Invalid group ID"
            );


        } catch (Exception e) {

            System.out.println(
                    "Error handling group deletion: "
                            + e.getMessage()
            );


            output.println(
                    "ERROR|Could not notify group deletion"
            );
        }
    }


    // =========================================================
    // HANDLE GROUP RENAMED
    // =========================================================

    private void handleGroupRenamed(
            String message
    ) {

        try {

            String[] parts =
                    message.split(
                            "\\|",
                            3
                    );


            if (parts.length < 3) {

                output.println(
                        "ERROR|Invalid group renamed message"
                );

                return;
            }


            int groupId =
                    Integer.parseInt(
                            parts[1]
                    );


            String newGroupName =
                    parts[2].trim();


            if (newGroupName.isEmpty()) {

                output.println(
                        "ERROR|Group name cannot be empty"
                );

                return;
            }


            System.out.println(
                    "================================="
            );

            System.out.println(
                    "GROUP RENAMED"
            );

            System.out.println(
                    "Group ID: "
                            + groupId
            );

            System.out.println(
                    "New Group Name: "
                            + newGroupName
            );

            System.out.println(
                    "================================="
            );


            String notification =
                    "GROUP_RENAMED|"
                            + groupId
                            + "|"
                            + newGroupName;


            // -------------------------------------------------
            // NOTIFY ALL CONNECTED CLIENTS
            // -------------------------------------------------

            for (ClientHandler client :
                    ChatServer
                            .getConnectedUsers()
                            .values()) {

                client.sendMessage(
                        notification
                );
            }


            // -------------------------------------------------
            // SEND SUCCESS RESPONSE
            // -------------------------------------------------

            output.println(
                    "GROUP_RENAMED_SUCCESS"
            );


            System.out.println(
                    "Group rename notification broadcast."
            );


        } catch (NumberFormatException e) {

            output.println(
                    "ERROR|Invalid group ID"
            );


        } catch (Exception e) {

            System.out.println(
                    "Error handling group rename: "
                            + e.getMessage()
            );


            output.println(
                    "ERROR|Could not notify group rename"
            );
        }
    }


    // =========================================================
    // HANDLE GROUP MESSAGE
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

            output.println(
                    "ERROR|Invalid group message format"
            );

            return;
        }


        try {

            int groupId =
                    Integer.parseInt(
                            parts[1]
                    );


            String groupMessage =
                    parts[2].trim();


            if (groupMessage.isEmpty()) {

                output.println(
                        "ERROR|Message cannot be empty"
                );

                return;
            }


            List<User> members =
                    groupDAO.getGroupMembers(
                            groupId
                    );


            boolean delivered = false;


            for (User member : members) {

                ClientHandler memberHandler =
                        ChatServer.getUser(
                                member.getUsername()
                        );


                if (memberHandler != null) {

                    memberHandler.sendMessage(
                            "GROUP_MESSAGE|"
                                    + groupId
                                    + "|"
                                    + username
                                    + "|"
                                    + groupMessage
                    );


                    delivered = true;
                }
            }


            if (delivered) {

                output.println(
                        "GROUP_MESSAGE_SENT"
                );


                System.out.println(
                        "Group message delivered. "
                                + "Group ID: "
                                + groupId
                                + ", Sender: "
                                + username
                );


            } else {

                output.println(
                        "ERROR|No group members are online"
                );
            }


        } catch (NumberFormatException e) {

            output.println(
                    "ERROR|Invalid group ID"
            );


        } catch (Exception e) {

            System.out.println(
                    "Error handling group message: "
                            + e.getMessage()
            );


            output.println(
                    "ERROR|Could not send group message"
            );
        }
    }


    // =========================================================
    // SEND MESSAGE
    // =========================================================

    public void sendMessage(
            String message
    ) {

        if (output != null) {

            output.println(message);
        }
    }
}