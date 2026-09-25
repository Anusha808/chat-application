package com.chatapp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatSocketClient {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private Thread listenerThread;

    private Consumer<String> messageListener;

    public boolean connect(String username) {

        try {

            socket = new Socket(
                    "localhost",
                    5000
            );

            input = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            output = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

            String welcomeMessage =
                    input.readLine();

            System.out.println(
                    "Server: " + welcomeMessage
            );

            output.println(username);

            String loginResponse =
                    input.readLine();

            System.out.println(
                    "Server: " + loginResponse
            );

            if (!"LOGIN_SUCCESS".equals(
                    loginResponse
            )) {

                disconnect();

                return false;
            }

            startListening();

            System.out.println(
                    "Socket connected successfully!"
            );

            return true;

        } catch (IOException e) {

            System.out.println(
                    "Socket connection failed: "
                            + e.getMessage()
            );

            return false;
        }
    }


    // =========================================================
    // START LISTENING
    // =========================================================

    private void startListening() {

        listenerThread = new Thread(() -> {

            try {

                String message;

                while ((message = input.readLine())
                        != null) {

                    System.out.println(
                            "Received from server: "
                                    + message
                    );

                    if (messageListener != null) {

                        messageListener.accept(
                                message
                        );
                    }
                }

            } catch (IOException e) {

                System.out.println(
                        "Socket listener stopped."
                );
            }

        });

        listenerThread.setDaemon(true);

        listenerThread.start();
    }


    // =========================================================
    // SEND PRIVATE MESSAGE
    // =========================================================

    public void sendMessage(
            String receiverUsername,
            String message
    ) {

        if (output == null) {

            System.out.println(
                    "Socket is not connected."
            );

            return;
        }

        String data =
                receiverUsername
                        + "|"
                        + message;

        output.println(data);

        System.out.println(
                "Sent to server: " + data
        );
    }


    // =========================================================
    // SEND GROUP MESSAGE
    // =========================================================

    public void sendGroupMessage(
            int groupId,
            String message
    ) {

        if (output == null) {

            System.out.println(
                    "Socket is not connected."
            );

            return;
        }

        String data =
                "GROUP|"
                        + groupId
                        + "|"
                        + message;

        output.println(data);

        System.out.println(
                "Sent group message: "
                        + data
        );
    }


    // =========================================================
    // NOTIFY SERVER THAT A GROUP WAS CREATED
    // =========================================================

    public void notifyGroupCreated(
            int groupId
    ) {

        if (output == null) {

            System.out.println(
                    "Socket is not connected."
            );

            return;
        }

        String data =
                "GROUP_CREATED|"
                        + groupId;

        output.println(data);

        System.out.println(
                "Group creation notification sent: "
                        + data
        );
    }


    // =========================================================
    // NOTIFY SERVER THAT A MEMBER WAS ADDED
    // =========================================================

    public void notifyGroupMemberAdded(
            int groupId,
            int userId
    ) {

        if (output == null) {

            System.out.println(
                    "Socket is not connected."
            );

            return;
        }

        String data =
                "GROUP_MEMBER_ADDED|"
                        + groupId
                        + "|"
                        + userId;

        output.println(data);

        System.out.println(
                "Group member notification sent: "
                        + data
        );
    }


    // =========================================================
    // NOTIFY SERVER THAT A MEMBER WAS REMOVED
    // =========================================================

    public void notifyGroupMemberRemoved(
            int groupId,
            int userId
    ) {

        if (output == null) {

            System.out.println(
                    "Socket is not connected."
            );

            return;
        }

        String data =
                "GROUP_MEMBER_REMOVED|"
                        + groupId
                        + "|"
                        + userId;

        output.println(data);

        System.out.println(
                "Group member removal notification sent: "
                        + data
        );
    }


    // =========================================================
    // NOTIFY SERVER THAT A MEMBER LEFT THE GROUP
    // =========================================================

    public void notifyGroupMemberLeft(
            int groupId,
            int userId
    ) {

        if (output == null) {

            System.out.println(
                    "Socket is not connected."
            );

            return;
        }

        String data =
                "GROUP_MEMBER_LEFT|"
                        + groupId
                        + "|"
                        + userId;

        output.println(data);

        System.out.println(
                "Group member left notification sent: "
                        + data
        );
    }


    // =========================================================
    // NOTIFY SERVER THAT A GROUP WAS DELETED
    // =========================================================

    public void notifyGroupDeleted(
            int groupId
    ) {

        if (output == null) {

            System.out.println(
                    "Socket is not connected."
            );

            return;
        }

        String data =
                "GROUP_DELETED|"
                        + groupId;

        output.println(data);

        System.out.println(
                "Group deleted notification sent: "
                        + data
        );
    }


    // =========================================================
    // NOTIFY SERVER THAT A GROUP WAS RENAMED
    // =========================================================

    public void notifyGroupRenamed(
            int groupId,
            String newGroupName
    ) {

        if (output == null) {

            System.out.println(
                    "Socket is not connected."
            );

            return;
        }

        String data =
                "GROUP_RENAMED|"
                        + groupId
                        + "|"
                        + newGroupName;

        output.println(data);

        System.out.println(
                "Group renamed notification sent: "
                        + data
        );
    }


    // =========================================================
    // SET MESSAGE LISTENER
    // =========================================================

    public void setMessageListener(
            Consumer<String> messageListener
    ) {

        this.messageListener =
                messageListener;
    }


    // =========================================================
    // CHECK CONNECTION
    // =========================================================

    public boolean isConnected() {

        return socket != null
                && socket.isConnected()
                && !socket.isClosed();
    }


    // =========================================================
    // DISCONNECT
    // =========================================================

    public void disconnect() {

        try {

            if (socket != null) {

                socket.close();
            }

        } catch (IOException e) {

            System.out.println(
                    "Error closing socket."
            );
        }

        socket = null;
        input = null;
        output = null;
    }
}