package com.chatapp.controller;

import com.chatapp.client.ChatSocketClient;
import com.chatapp.dao.GroupMemberDAO;
import com.chatapp.model.GroupMember;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.util.List;

public class AddMemberController {

    private final int groupId;
    private final String groupName;
    private final Connection connection;
    private final GroupMembersController membersController;
    private final ChatSocketClient socketClient;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AddMemberController(
            int groupId,
            String groupName,
            Connection connection,
            GroupMembersController membersController,
            ChatSocketClient socketClient) {

        this.groupId = groupId;
        this.groupName = groupName;
        this.connection = connection;
        this.membersController = membersController;
        this.socketClient = socketClient;
    }


    // =========================================================
    // SHOW WINDOW
    // =========================================================

    public void show() {

        Stage stage =
                new Stage();

        stage.setTitle(
                "Add Member - " + groupName
        );


        // =====================================================
        // TITLE
        // =====================================================

        Label title =
                new Label(
                        "➕ Add Member"
                );

        title.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #172033;"
        );


        // =====================================================
        // GROUP NAME
        // =====================================================

        Label groupLabel =
                new Label(
                        "Group: " + groupName
                );

        groupLabel.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #64748b;"
        );


        // =====================================================
        // DAO
        // =====================================================

        GroupMemberDAO dao =
                new GroupMemberDAO(
                        connection
                );


        // =====================================================
        // LOAD NON-MEMBERS
        // =====================================================

        List<GroupMember> nonMembers =
                dao.getNonMembers(
                        groupId
                );


        // =====================================================
        // COMBO BOX
        // =====================================================

        ComboBox<GroupMember> memberComboBox =
                new ComboBox<>(
                        FXCollections.observableArrayList(
                                nonMembers
                        )
                );

        memberComboBox.setPromptText(
                "Select a user"
        );

        memberComboBox.setPrefWidth(
                330
        );


        // =====================================================
        // COMBO BOX LIST CELL
        // =====================================================

        memberComboBox.setCellFactory(
                listView ->
                        new ListCell<GroupMember>() {

                            @Override
                            protected void updateItem(
                                    GroupMember member,
                                    boolean empty) {

                                super.updateItem(
                                        member,
                                        empty
                                );

                                if (empty
                                        || member == null) {

                                    setText(null);

                                } else {

                                    setText(
                                            member.getUsername()
                                                    + " - "
                                                    + member.getEmail()
                                    );
                                }
                            }
                        }
        );


        // =====================================================
        // SELECTED VALUE
        // =====================================================

        memberComboBox.setButtonCell(
                new ListCell<GroupMember>() {

                    @Override
                    protected void updateItem(
                            GroupMember member,
                            boolean empty) {

                        super.updateItem(
                                member,
                                empty
                        );

                        if (empty
                                || member == null) {

                            setText(null);

                        } else {

                            setText(
                                    member.getUsername()
                            );
                        }
                    }
                }
        );


        // =====================================================
        // ALL USERS ALREADY MEMBERS
        // =====================================================

        if (nonMembers.isEmpty()) {

            memberComboBox.setDisable(
                    true
            );

            memberComboBox.setPromptText(
                    "All users are already members"
            );
        }


        // =====================================================
        // ADD BUTTON
        // =====================================================

        Button addButton =
                new Button(
                        "Add Member"
                );

        addButton.setPrefWidth(
                130
        );

        addButton.setStyle(
                "-fx-background-color: #4f8fe8;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 9 18;" +
                "-fx-background-radius: 8;"
        );


        // =====================================================
        // CANCEL BUTTON
        // =====================================================

        Button cancelButton =
                new Button(
                        "Cancel"
                );

        cancelButton.setPrefWidth(
                100
        );

        cancelButton.setStyle(
                "-fx-padding: 9 18;" +
                "-fx-background-radius: 8;"
        );


        // =====================================================
        // ADD MEMBER ACTION
        // =====================================================

        addButton.setOnAction(
                event -> {

                    GroupMember selectedMember =
                            memberComboBox.getValue();


                    // -----------------------------------------
                    // NO MEMBER SELECTED
                    // -----------------------------------------

                    if (selectedMember == null) {

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Select Member",
                                "Please select a user to add."
                        );

                        return;
                    }


                    int userId =
                            selectedMember.getUserId();


                    // -----------------------------------------
                    // CHECK MEMBER
                    // -----------------------------------------

                    if (dao.isMember(
                            groupId,
                            userId
                    )) {

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Already a Member",
                                selectedMember.getUsername()
                                        + " is already a member."
                        );

                        return;
                    }


                    // -----------------------------------------
                    // ADD TO DATABASE
                    // -----------------------------------------

                    boolean added =
                            dao.addMember(
                                    groupId,
                                    userId
                            );


                    if (!added) {

                        showAlert(
                                Alert.AlertType.ERROR,
                                "Failed",
                                "Could not add the member."
                        );

                        return;
                    }


                    // -----------------------------------------
                    // SEND SOCKET NOTIFICATION
                    // -----------------------------------------

                    if (socketClient != null
                            && socketClient.isConnected()) {

                        socketClient
                                .notifyGroupMemberAdded(
                                        groupId,
                                        userId
                                );

                        System.out.println(
                                "Group member notification sent."
                        );

                    } else {

                        System.out.println(
                                "Socket is not connected."
                        );
                    }


                    // -----------------------------------------
                    // REFRESH MEMBER WINDOW
                    // -----------------------------------------

                    if (membersController != null) {

                        membersController
                                .loadMembers();
                    }


                    // -----------------------------------------
                    // SUCCESS ALERT
                    // -----------------------------------------

                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Member Added",
                            selectedMember.getUsername()
                                    + " was added to "
                                    + groupName
                    );


                    // -----------------------------------------
                    // CLOSE WINDOW
                    // -----------------------------------------

                    stage.close();
                }
        );


        // =====================================================
        // CANCEL ACTION
        // =====================================================

        cancelButton.setOnAction(
                event -> stage.close()
        );


        // =====================================================
        // BUTTON LAYOUT
        // =====================================================

        HBox buttons =
                new HBox(
                        10,
                        addButton,
                        cancelButton
                );

        buttons.setAlignment(
                Pos.CENTER_RIGHT
        );


        // =====================================================
        // ROOT LAYOUT
        // =====================================================

        VBox root =
                new VBox(
                        15,
                        title,
                        groupLabel,
                        memberComboBox,
                        buttons
                );

        root.setPadding(
                new Insets(25)
        );

        root.setAlignment(
                Pos.TOP_LEFT
        );

        root.setStyle(
                "-fx-background-color: #f7faff;"
        );


        // =====================================================
        // SCENE
        // =====================================================

        Scene scene =
                new Scene(
                        root,
                        430,
                        270
                );

        stage.setScene(
                scene
        );

        stage.show();
    }


    // =========================================================
    // ALERT
    // =========================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert =
                new Alert(type);

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