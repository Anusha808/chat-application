package com.chatapp.controller;

import com.chatapp.dao.UserDAO;
import com.chatapp.model.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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


    // =========================================================
    // REGISTER USER
    // =========================================================

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


    // =========================================================
    // OPEN LOGIN PAGE
    // =========================================================

    @FXML
    private void openLoginPage() {

        try {

            System.out.println("Opening Login page...");


            // Load login.fxml
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
            );


            Parent root = loader.load();


            // Create new scene
            Scene scene = new Scene(root);


            // Load login CSS
            var css = getClass().getResource("/css/login.css");

            if (css != null) {

                scene.getStylesheets().add(
                        css.toExternalForm()
                );

            } else {

                System.out.println(
                        "Warning: login.css not found."
                );
            }


            // Get current window
            Stage stage =
                    (Stage) usernameField
                            .getScene()
                            .getWindow();


            // Change title
            stage.setTitle(
                    "Chat Application - Login"
            );


            // Change scene
            stage.setScene(scene);


            // Show login page
            stage.show();


            System.out.println(
                    "Login page opened successfully."
            );


        } catch (Exception e) {

            System.out.println(
                    "Unable to open Login page."
            );

            e.printStackTrace();


            // Show error message
            Alert alert =
                    new Alert(Alert.AlertType.ERROR);

            alert.setTitle(
                    "Login Page Error"
            );

            alert.setHeaderText(
                    "Unable to open Login page"
            );

            alert.setContentText(
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Unknown error occurred."
            );

            alert.showAndWait();
        }
    }
}