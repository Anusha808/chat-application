package com.chatapp.controller;

import com.chatapp.dao.MessageDAO;
import com.chatapp.dao.UserDAO;
import com.chatapp.model.Message;
import com.chatapp.model.User;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.List;

public class ChatController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private ListView<String> userListView;

    @FXML
    private ListView<String> groupListView;

    @FXML
    private ListView<String> messageListView;

    @FXML
    private Label chatTitleLabel;

    @FXML
    private TextField messageField;

    private String username;

    private User currentUser;
    private User selectedUser;

    private final UserDAO userDAO = new UserDAO();
    private final MessageDAO messageDAO = new MessageDAO();

    @FXML
    private void initialize() {

        userListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {

                    if (newValue != null) {
                        openPrivateChat(newValue);
                    }
                });
    }

    public void setUsername(String username) {

        this.username = username;

        welcomeLabel.setText(
                "Welcome, " + username + "!"
        );

        currentUser =
                userDAO.findUserByUsername(username);

        loadUsers();
    }

    private void loadUsers() {

        List<User> users =
                userDAO.getAllUsers();

        List<String> usernames = users.stream()
                .map(User::getUsername)
                .filter(name -> !name.equals(username))
                .toList();

        userListView.setItems(
                FXCollections.observableArrayList(usernames)
        );
    }

    private void openPrivateChat(String selectedUsername) {

        selectedUser =
                userDAO.findUserByUsername(selectedUsername);

        if (selectedUser == null) {
            return;
        }

        chatTitleLabel.setText(
                "Chat with " + selectedUser.getUsername()
        );

        loadChatHistory();
    }

    private void loadChatHistory() {

        if (currentUser == null || selectedUser == null) {
            return;
        }

        List<Message> messages =
                messageDAO.getChatHistory(
                        currentUser.getId(),
                        selectedUser.getId()
                );

        List<String> chatMessages = messages.stream()
                .map(message -> {

                    String sender;

                    if (message.getSenderId()
                            == currentUser.getId()) {

                        sender = "You";

                    } else {

                        sender =
                                selectedUser.getUsername();
                    }

                    return sender
                            + ": "
                            + message.getMessage();
                })
                .toList();

        messageListView.setItems(
                FXCollections.observableArrayList(
                        chatMessages
                )
        );

        if (!chatMessages.isEmpty()) {

            messageListView.scrollTo(
                    chatMessages.size() - 1
            );
        }
    }

    @FXML
    private void handleSendMessage() {

        System.out.println("SEND BUTTON CLICKED");

        // Check whether a user is selected
        if (selectedUser == null) {

            System.out.println(
                    "Please select a user first."
            );

            chatTitleLabel.setText(
                    "Please select a user first"
            );

            return;
        }

        // Get message text
        String text =
                messageField.getText().trim();

        // Check empty message
        if (text.isEmpty()) {

            System.out.println(
                    "Message is empty."
            );

            return;
        }

        // Check current user
        if (currentUser == null) {

            System.out.println(
                    "Current user is not available."
            );

            return;
        }

        // Create message
        Message message = new Message(
                currentUser.getId(),
                selectedUser.getId(),
                text
        );

        System.out.println(
                "Sending message: " + text
        );

        System.out.println(
                "Sender ID: "
                        + currentUser.getId()
        );

        System.out.println(
                "Receiver ID: "
                        + selectedUser.getId()
        );

        // Save message to database
        boolean saved =
                messageDAO.saveMessage(message);

        if (saved) {

            System.out.println(
                    "Message saved successfully!"
            );

            // Clear input field
            messageField.clear();

            // Reload conversation
            loadChatHistory();

        } else {

            System.out.println(
                    "Message could not be saved."
            );
        }
    }

    @FXML
    private void handleLogout() {

        System.out.println(
                "User logged out: " + username
        );
    }
}