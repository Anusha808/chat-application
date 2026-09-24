package com.chatapp.dao;

import com.chatapp.model.Message;

import java.util.List;

public class MessageDAOTest {

    public static void main(String[] args) {

        MessageDAO messageDAO = new MessageDAO();

        // anusha (ID 1) sends a message to anusha2 (ID 2)
        Message message = new Message(
                1,
                2,
                "Hello from Anusha!"
        );

        boolean saved = messageDAO.saveMessage(message);

        if (saved) {

            System.out.println("=================================");
            System.out.println("Message Saved Successfully!");
            System.out.println("=================================");

        } else {

            System.out.println("Message Saving Failed!");
        }

        // Load conversation between anusha and anusha2
        List<Message> messages =
                messageDAO.getChatHistory(1, 2);

        System.out.println("=================================");
        System.out.println("Chat History");
        System.out.println("=================================");

        for (Message msg : messages) {

            System.out.println(
                    "Sender ID: " + msg.getSenderId()
            );

            System.out.println(
                    "Receiver ID: " + msg.getReceiverId()
            );

            System.out.println(
                    "Message: " + msg.getMessage()
            );

            System.out.println(
                    "Time: " + msg.getSentAt()
            );

            System.out.println("---------------------------------");
        }
    }
}