package com.chatapp.controller;

import com.chatapp.dao.UserDAO;
import com.chatapp.model.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {

            messageLabel.setText(
                    "Please enter username and password."
            );

            return;
        }

        User user = userDAO.loginUser(username, password);

        if (user != null) {

            openChatDashboard(user);

        } else {

            messageLabel.setText(
                    "Invalid username or password."
            );
        }
    }

    private void openChatDashboard(User user) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/chat.fxml")
            );

            Parent root = loader.load();

            ChatController chatController =
                    loader.getController();

            chatController.setUsername(
                    user.getUsername()
            );

            Scene scene = new Scene(root);

            scene.getStylesheets().add(
                    getClass()
                            .getResource("/css/chat.css")
                            .toExternalForm()
            );

            Stage stage =
                    (Stage) usernameField.getScene().getWindow();

            stage.setTitle(
                    "Chat Application - " + user.getUsername()
            );

            stage.setScene(scene);

            stage.setWidth(900);
            stage.setHeight(600);
            stage.setResizable(true);

            stage.show();

        } catch (Exception e) {

            System.out.println(
                    "Unable to open chat dashboard."
            );

            e.printStackTrace();

            messageLabel.setText(
                    "Unable to open chat dashboard."
            );
        }
    }

    @FXML
    private void openRegisterPage() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/register.fxml")
            );

            Parent root = loader.load();

            Stage stage =
                    (Stage) usernameField.getScene().getWindow();

            Scene scene = new Scene(root);

            scene.getStylesheets().add(
                    getClass()
                            .getResource("/css/register.css")
                            .toExternalForm()
            );

            stage.setTitle(
                    "Chat Application - Register"
            );

            stage.setScene(scene);

        } catch (Exception e) {

            System.out.println(
                    "Unable to open registration page."
            );

            e.printStackTrace();
        }
    }
}