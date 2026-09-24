package com.chatapp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

    private static final int PORT = 5000;

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println("     Chat Server Starting...");
        System.out.println("=================================");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Server started successfully!");
            System.out.println("Waiting for clients on port " + PORT + "...");

            while (true) {

                Socket clientSocket = serverSocket.accept();

                System.out.println(
                        "Client connected: "
                                + clientSocket.getInetAddress()
                );

                Thread clientThread = new Thread(
                        new ClientHandler(clientSocket)
                );

                clientThread.start();
            }

        } catch (IOException e) {

            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}