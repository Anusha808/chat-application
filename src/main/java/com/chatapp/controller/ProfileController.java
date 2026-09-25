package com.chatapp.controller;

import com.chatapp.dao.UserDAO;
import com.chatapp.model.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProfileController {

    // =========================================================
    // PROFILE DISPLAY
    // =========================================================

    @FXML
    private Label avatarLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label userIdLabel;


    // =========================================================
    // EDIT PROFILE SECTION
    // =========================================================

    @FXML
    private VBox editProfileSection;


    // =========================================================
    // CHANGE PASSWORD SECTION
    // =========================================================

    @FXML
    private VBox passwordSection;


    // =========================================================
    // EDIT PROFILE FIELDS
    // =========================================================

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;


    // =========================================================
    // CHANGE PASSWORD FIELDS
    // =========================================================

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;


    // =========================================================
    // BUTTONS
    // =========================================================

    @FXML
    private Button editProfileButton;

    @FXML
    private Button saveProfileButton;

    @FXML
    private Button cancelEditButton;

    @FXML
    private Button changePasswordButton;

    @FXML
    private Button backButton;


    // =========================================================
    // USER INFORMATION
    // =========================================================

    private User currentUser;

    private final UserDAO userDAO =
            new UserDAO();


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    private void initialize() {

        System.out.println(
                "ProfileController initialized."
        );

        if (statusLabel != null) {

            statusLabel.setText(
                    "ONLINE"
            );
        }

        /*
         * Start in normal profile mode.
         *
         * Username/email editing fields
         * and password section remain hidden.
         */

        setEditMode(false);
    }


    // =========================================================
    // SET USER
    // =========================================================

    public void setUser(User user) {

        if (user == null) {

            System.out.println(
                    "Profile user cannot be null."
            );

            return;
        }

        this.currentUser = user;

        loadUserData();
    }


    // =========================================================
    // LOAD USER DATA
    // =========================================================

    private void loadUserData() {

        if (currentUser == null) {

            return;
        }


        // -----------------------------------------------------
        // AVATAR
        // -----------------------------------------------------

        if (avatarLabel != null) {

            String username =
                    currentUser.getUsername();

            if (username != null
                    && !username.isBlank()) {

                avatarLabel.setText(
                        username
                                .substring(0, 1)
                                .toUpperCase()
                );

            } else {

                avatarLabel.setText(
                        "U"
                );
            }
        }


        // -----------------------------------------------------
        // DISPLAY USERNAME
        // -----------------------------------------------------

        if (usernameLabel != null) {

            usernameLabel.setText(
                    currentUser.getUsername()
            );
        }


        // -----------------------------------------------------
        // DISPLAY EMAIL
        // -----------------------------------------------------

        if (emailLabel != null) {

            emailLabel.setText(
                    currentUser.getEmail()
            );
        }


        // -----------------------------------------------------
        // DISPLAY STATUS
        // -----------------------------------------------------

        if (statusLabel != null) {

            statusLabel.setText(
                    "ONLINE"
            );
        }


        // -----------------------------------------------------
        // DISPLAY USER ID
        // -----------------------------------------------------

        if (userIdLabel != null) {

            userIdLabel.setText(
                    String.valueOf(
                            currentUser.getId()
                    )
            );
        }


        // -----------------------------------------------------
        // LOAD EDIT FIELDS
        // -----------------------------------------------------

        if (usernameField != null) {

            usernameField.setText(
                    currentUser.getUsername()
            );
        }

        if (emailField != null) {

            emailField.setText(
                    currentUser.getEmail()
            );
        }
    }


    // =========================================================
    // EDIT PROFILE
    // =========================================================

    @FXML
    private void handleEditProfile() {

        if (currentUser == null) {

            showError(
                    "Profile",
                    "No user information found."
            );

            return;
        }

        /*
         * Show username, email AND password section.
         */

        setEditMode(true);
    }


    // =========================================================
    // SAVE PROFILE
    // =========================================================

    @FXML
    private void handleSaveProfile() {

        if (currentUser == null) {

            showError(
                    "Update Profile",
                    "No logged-in user found."
            );

            return;
        }


        String newUsername =
                usernameField
                        .getText()
                        .trim();

        String newEmail =
                emailField
                        .getText()
                        .trim();


        // -----------------------------------------------------
        // VALIDATION
        // -----------------------------------------------------

        if (newUsername.isEmpty()) {

            showWarning(
                    "Invalid Username",
                    "Username cannot be empty."
            );

            return;
        }


        if (newEmail.isEmpty()) {

            showWarning(
                    "Invalid Email",
                    "Email cannot be empty."
            );

            return;
        }


        if (!newEmail.contains("@")
                || !newEmail.contains(".")) {

            showWarning(
                    "Invalid Email",
                    "Please enter a valid email address."
            );

            return;
        }


        // -----------------------------------------------------
        // CHECK USERNAME CHANGE
        // -----------------------------------------------------

        if (!newUsername.equals(
                currentUser.getUsername()
        )) {

            User existingUser =
                    userDAO.findUserByUsername(
                            newUsername
                    );

            if (existingUser != null
                    && existingUser.getId()
                    != currentUser.getId()) {

                showWarning(
                        "Username Already Exists",
                        "Please choose a different username."
                );

                return;
            }
        }


        // -----------------------------------------------------
        // UPDATE DATABASE
        // -----------------------------------------------------

        boolean updated =
                userDAO.updateUserProfile(
                        currentUser.getId(),
                        newUsername,
                        newEmail
                );


        if (!updated) {

            showError(
                    "Update Failed",
                    "Could not update your profile."
            );

            return;
        }


        // -----------------------------------------------------
        // UPDATE CURRENT USER OBJECT
        // -----------------------------------------------------

        currentUser.setUsername(
                newUsername
        );

        currentUser.setEmail(
                newEmail
        );


        // -----------------------------------------------------
        // REFRESH DISPLAY
        // -----------------------------------------------------

        loadUserData();


        /*
         * Return to normal display mode.
         */

        setEditMode(false);


        showInformation(
                "Profile Updated",
                "Your profile has been updated successfully."
        );
    }


    // =========================================================
    // CANCEL EDIT
    // =========================================================

    @FXML
    private void handleCancelEdit() {

        if (currentUser != null) {

            if (usernameField != null) {

                usernameField.setText(
                        currentUser.getUsername()
                );
            }

            if (emailField != null) {

                emailField.setText(
                        currentUser.getEmail()
                );
            }
        }


        /*
         * Clear password fields when cancelling.
         */

        clearPasswordFields();


        /*
         * Return to normal profile display.
         */

        setEditMode(false);
    }


    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @FXML
    private void handleChangePassword() {

        if (currentUser == null) {

            showError(
                    "Change Password",
                    "No logged-in user found."
            );

            return;
        }


        String currentPassword =
                currentPasswordField
                        .getText();

        String newPassword =
                newPasswordField
                        .getText();

        String confirmPassword =
                confirmPasswordField
                        .getText();


        // -----------------------------------------------------
        // VALIDATION
        // -----------------------------------------------------

        if (currentPassword.isEmpty()) {

            showWarning(
                    "Current Password",
                    "Please enter your current password."
            );

            return;
        }


        if (newPassword.isEmpty()) {

            showWarning(
                    "New Password",
                    "Please enter a new password."
            );

            return;
        }


        if (newPassword.length() < 4) {

            showWarning(
                    "New Password",
                    "Password should contain at least 4 characters."
            );

            return;
        }


        if (confirmPassword.isEmpty()) {

            showWarning(
                    "Confirm Password",
                    "Please confirm your new password."
            );

            return;
        }


        if (!newPassword.equals(
                confirmPassword
        )) {

            showWarning(
                    "Password Mismatch",
                    "New password and confirm password do not match."
            );

            return;
        }


        // -----------------------------------------------------
        // CHANGE PASSWORD IN DATABASE
        // -----------------------------------------------------

        boolean changed =
                userDAO.changePassword(
                        currentUser.getId(),
                        currentPassword,
                        newPassword
                );


        if (!changed) {

            showError(
                    "Password Change Failed",
                    "Current password is incorrect."
            );

            return;
        }


        // -----------------------------------------------------
        // CLEAR PASSWORD FIELDS
        // -----------------------------------------------------

        clearPasswordFields();


        // -----------------------------------------------------
        // SUCCESS
        // -----------------------------------------------------

        showInformation(
                "Password Changed",
                "Your password has been changed successfully."
        );
    }


    // =========================================================
    // CLEAR PASSWORD FIELDS
    // =========================================================

    private void clearPasswordFields() {

        if (currentPasswordField != null) {

            currentPasswordField.clear();
        }

        if (newPasswordField != null) {

            newPasswordField.clear();
        }

        if (confirmPasswordField != null) {

            confirmPasswordField.clear();
        }
    }


    // =========================================================
    // SET EDIT MODE
    // =========================================================

    private void setEditMode(
            boolean editing
    ) {

        // -----------------------------------------------------
        // EDIT PROFILE SECTION
        // -----------------------------------------------------

        if (editProfileSection != null) {

            editProfileSection.setVisible(
                    editing
            );

            editProfileSection.setManaged(
                    editing
            );
        }


        // -----------------------------------------------------
        // PASSWORD SECTION
        // -----------------------------------------------------

        if (passwordSection != null) {

            passwordSection.setVisible(
                    editing
            );

            passwordSection.setManaged(
                    editing
            );
        }


        // -----------------------------------------------------
        // EDIT PROFILE BUTTON
        // -----------------------------------------------------

        if (editProfileButton != null) {

            editProfileButton.setVisible(
                    !editing
            );

            editProfileButton.setManaged(
                    !editing
            );
        }
    }


    // =========================================================
    // BACK TO CHAT
    // =========================================================

    @FXML
    private void handleBackToChat() {

        if (currentUser == null) {

            showError(
                    "Chat",
                    "User information is missing."
            );

            return;
        }


        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/chat.fxml"
                            )
                    );


            Parent root =
                    loader.load();


            // -------------------------------------------------
            // GET CHAT CONTROLLER
            // -------------------------------------------------

            ChatController chatController =
                    loader.getController();


            // -------------------------------------------------
            // PASS CURRENT USER
            // -------------------------------------------------

            chatController.setCurrentUser(
                    currentUser
            );


            // -------------------------------------------------
            // CREATE SCENE
            // -------------------------------------------------

            Scene scene =
                    new Scene(
                            root
                    );


            // -------------------------------------------------
            // LOAD CHAT CSS
            // -------------------------------------------------

            if (getClass()
                    .getResource(
                            "/css/chat.css"
                    ) != null) {

                scene.getStylesheets().add(
                        getClass()
                                .getResource(
                                        "/css/chat.css"
                                )
                                .toExternalForm()
                );
            }


            // -------------------------------------------------
            // GET WINDOW
            // -------------------------------------------------

            Stage stage =
                    (Stage) backButton
                            .getScene()
                            .getWindow();


            // -------------------------------------------------
            // WINDOW SETTINGS
            // -------------------------------------------------

            stage.setTitle(
                    "Chat Application - "
                            + currentUser.getUsername()
            );

            stage.setScene(
                    scene
            );

            stage.setWidth(
                    900
            );

            stage.setHeight(
                    600
            );

            stage.setResizable(
                    true
            );

            stage.show();


        } catch (Exception e) {

            System.out.println(
                    "Unable to return to chat."
            );

            e.printStackTrace();

            showError(
                    "Chat",
                    "Unable to open chat dashboard."
            );
        }
    }


    // =========================================================
    // ALERT - INFORMATION
    // =========================================================

    private void showInformation(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }


    // =========================================================
    // ALERT - WARNING
    // =========================================================

    private void showWarning(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }


    // =========================================================
    // ALERT - ERROR
    // =========================================================

    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }
}