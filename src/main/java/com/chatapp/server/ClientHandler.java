package com.chatapp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;

    private BufferedReader input;
    private PrintWriter output;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {

            input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            output = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

            output.println("Connected to Chat Server!");

            String message;

            while ((message = input.readLine()) != null) {

                System.out.println(
                        "Client message: " + message
                );

                output.println(
                        "Server received: " + message
                );
            }

        } catch (IOException e) {

            System.out.println(
                    "Client disconnected: " + e.getMessage()
            );

        } finally {

            try {
                socket.close();
            } catch (IOException e) {
                System.out.println(
                        "Error closing client socket."
                );
            }
        }
    }
}