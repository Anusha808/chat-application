package com.chatapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsController {

    @FXML
    private CheckBox notificationsCheckBox;

    @FXML
    private CheckBox soundCheckBox;

    @FXML
    private CheckBox darkModeCheckBox;

    @FXML
    private CheckBox onlineStatusCheckBox;

    @FXML
    private Label statusLabel;


    // ==========================================
    // INITIALIZE
    // ==========================================

    @FXML
    public void initialize() {

        // Default settings
        notificationsCheckBox.setSelected(true);
        soundCheckBox.setSelected(true);
        darkModeCheckBox.setSelected(false);
        onlineStatusCheckBox.setSelected(true);

        statusLabel.setText("");
    }


    // ==========================================
    // SAVE SETTINGS
    // ==========================================

    @FXML
    private void handleSaveSettings() {

        boolean notifications =
                notificationsCheckBox.isSelected();

        boolean sound =
                soundCheckBox.isSelected();

        boolean darkMode =
                darkModeCheckBox.isSelected();

        boolean onlineStatus =
                onlineStatusCheckBox.isSelected();


        // For now, display confirmation.
        // Later these settings can be connected
        // to a database or application preferences.

        System.out.println("========== SETTINGS ==========");
        System.out.println("Notifications : " + notifications);
        System.out.println("Message Sound : " + sound);
        System.out.println("Dark Mode     : " + darkMode);
        System.out.println("Online Status : " + onlineStatus);
        System.out.println("==============================");


        statusLabel.setText("✓ Settings saved successfully!");


        // Automatically remove message after a few seconds
        javafx.animation.PauseTransition pause =
                new javafx.animation.PauseTransition(
                        javafx.util.Duration.seconds(3)
                );

        pause.setOnFinished(event ->
                statusLabel.setText("")
        );

        pause.play();
    }


    // ==========================================
    // BACK TO CHAT
    // ==========================================

    @FXML
    private void handleBackToChat() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/chat.fxml"
                            )
                    );

            Parent root = loader.load();


            // Get existing stage
            Stage stage =
                    (Stage) statusLabel
                            .getScene()
                            .getWindow();


            Scene scene =
                    new Scene(root);


            // Load chat CSS
            String css =
                    getClass()
                            .getResource(
                                    "/css/chat.css"
                            )
                            .toExternalForm();

            scene.getStylesheets().add(css);


            stage.setScene(scene);
            stage.setTitle("Chat Application");
            stage.show();


        } catch (IOException e) {

            e.printStackTrace();

            showError(
                    "Unable to return to Chat.",
                    e.getMessage()
            );
        }
    }


    // ==========================================
    // ERROR ALERT
    // ==========================================

    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}