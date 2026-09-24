package com.chatapp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClientTest {

    public static void main(String[] args) {

        try (
            Socket socket = new Socket("localhost", 5000);

            BufferedReader input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            PrintWriter output = new PrintWriter(
                    socket.getOutputStream(),
                    true
            )
        ) {

            System.out.println("=================================");
            System.out.println("Connected to Chat Server!");
            System.out.println("=================================");

            // Receive server message
            String serverMessage = input.readLine();

            System.out.println("Server: " + serverMessage);

            // Send a test message
            output.println("Hello from Chat Client!");

            // Receive server response
            String response = input.readLine();

            System.out.println("Server: " + response);

        } catch (IOException e) {

            System.out.println("Client connection failed!");
            e.printStackTrace();
        }
    }
}