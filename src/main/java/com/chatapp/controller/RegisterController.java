package com.chatapp.controller;

import com.chatapp.dao.UserDAO;
import com.chatapp.model.User;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleRegister() {

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Check empty fields
        if (username.isEmpty()
                || email.isEmpty()
                || password.isEmpty()
                || confirmPassword.isEmpty()) {

            messageLabel.setText(
                    "Please fill in all fields."
            );

            return;
        }

        // Check password match
        if (!password.equals(confirmPassword)) {

            messageLabel.setText(
                    "Passwords do not match."
            );

            return;
        }

        // Check whether username already exists
        if (userDAO.findUserByUsername(username) != null) {

            messageLabel.setText(
                    "Username already exists."
            );

            return;
        }

        // Create new user
        User user = new User(
                username,
                email,
                password
        );

        boolean registered = userDAO.registerUser(user);

        if (registered) {

            messageLabel.setText(
                    "Registration successful!"
            );

            usernameField.clear();
            emailField.clear();
            passwordField.clear();
            confirmPasswordField.clear();

        } else {

            messageLabel.setText(
                    "Registration failed. Please try again."
            );
        }
    }
}