package com.chatapp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private static final int PORT = 5000;

    private static final Map<String, ClientHandler> connectedUsers =
            new ConcurrentHashMap<>();

    // =========================================================
    // START SERVER
    // =========================================================

    public static void main(String[] args) {

        System.out.println(
                "================================="
        );

        System.out.println(
                "     Chat Server Starting..."
        );

        System.out.println(
                "================================="
        );

        try (ServerSocket serverSocket =
                     new ServerSocket(PORT)) {

            System.out.println(
                    "Server started successfully!"
            );

            System.out.println(
                    "Waiting for clients on port "
                            + PORT
                            + "..."
            );

            while (true) {

                Socket clientSocket =
                        serverSocket.accept();

                System.out.println(
                        "Client connected: "
                                + clientSocket.getInetAddress()
                );

                ClientHandler clientHandler =
                        new ClientHandler(
                                clientSocket
                        );

                Thread clientThread =
                        new Thread(
                                clientHandler
                        );

                clientThread.start();
            }

        } catch (IOException e) {

            System.out.println(
                    "Server error: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // =========================================================
    // ADD USER
    // =========================================================

    public static void addUser(
            String username,
            ClientHandler clientHandler
    ) {

        connectedUsers.put(
                username,
                clientHandler
        );

        System.out.println(
                "User connected: "
                        + username
        );

        System.out.println(
                "Online users: "
                        + connectedUsers.keySet()
        );

        broadcastOnlineUsers();
    }

    // =========================================================
    // REMOVE USER
    // =========================================================

    public static void removeUser(
            String username
    ) {

        if (username != null) {

            connectedUsers.remove(
                    username
            );

            System.out.println(
                    "User disconnected: "
                            + username
            );

            System.out.println(
                    "Online users: "
                            + connectedUsers.keySet()
            );

            broadcastOnlineUsers();
        }
    }

    // =========================================================
    // GET USER
    // =========================================================

    public static ClientHandler getUser(
            String username
    ) {

        return connectedUsers.get(
                username
        );
    }

    // =========================================================
    // CHECK USER ONLINE
    // =========================================================

    public static boolean isUserOnline(
            String username
    ) {

        return connectedUsers.containsKey(
                username
        );
    }

    // =========================================================
    // GET CONNECTED USERS
    // =========================================================

    public static Map<String, ClientHandler>
    getConnectedUsers() {

        return connectedUsers;
    }

    // =========================================================
    // BROADCAST ONLINE USERS
    // =========================================================

    private static void broadcastOnlineUsers() {

        String onlineUsers =
                String.join(
                        ",",
                        connectedUsers.keySet()
                );

        String message =
                "ONLINE_USERS|"
                        + onlineUsers;

        for (ClientHandler client :
                connectedUsers.values()) {

            client.sendMessage(
                    message
            );
        }
    }
}