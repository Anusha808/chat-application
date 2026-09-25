package com.chatapp.controller;

import com.chatapp.client.ChatSocketClient;
import com.chatapp.dao.GroupDAO;
import com.chatapp.dao.GroupMemberDAO;
import com.chatapp.model.GroupMember;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.sql.Connection;
import java.util.List;

public class GroupMembersController {

    private final int groupId;
    private final String groupName;
    private final int groupCreatorId;
    private final int currentUserId;
    private final Connection connection;
    private final ChatSocketClient socketClient;

    private final GroupMemberDAO groupMemberDAO;
    private final GroupDAO groupDAO;

    private VBox membersBox;
    private Label memberCountLabel;
    private Stage stage;

    public GroupMembersController(
            int groupId,
            String groupName,
            int groupCreatorId,
            int currentUserId,
            Connection connection,
            ChatSocketClient socketClient) {

        this.groupId = groupId;
        this.groupName = groupName;
        this.groupCreatorId = groupCreatorId;
        this.currentUserId = currentUserId;
        this.connection = connection;
        this.socketClient = socketClient;

        this.groupMemberDAO =
                new GroupMemberDAO(connection);

        this.groupDAO =
                new GroupDAO();
    }

    // =========================================================
    // SHOW GROUP MEMBERS WINDOW
    // =========================================================

    public void show() {

        stage = new Stage();

        stage.setTitle(
                "Group Members - " + groupName
        );

        VBox root = new VBox(15);

        root.setPadding(
                new Insets(20)
        );

        root.setStyle(
                "-fx-background-color: #f7faff;"
        );

        // =====================================================
        // TITLE
        // =====================================================

        Label title =
                new Label("Group Members");

        title.setStyle(
                "-fx-font-size: 24px;" +
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
        // MEMBER COUNT
        // =====================================================

        memberCountLabel =
                new Label();

        memberCountLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #64748b;"
        );

        // =====================================================
        // TITLE BOX
        // =====================================================

        VBox titleBox =
                new VBox(
                        4,
                        title,
                        groupLabel,
                        memberCountLabel
                );

        HBox.setHgrow(
                titleBox,
                Priority.ALWAYS
        );

        // =====================================================
        // HEADER
        // =====================================================

        HBox header =
                new HBox(10);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        header.getChildren().add(
                titleBox
        );

        boolean isAdmin =
                currentUserId == groupCreatorId;

        // =====================================================
        // ADMIN BUTTONS
        // =====================================================

        if (isAdmin) {

            // -------------------------------------------------
            // ADD MEMBER
            // -------------------------------------------------

            Button addButton =
                    new Button("+ Add Member");

            addButton.setStyle(
                    "-fx-background-color: #4f8fe8;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 9 15;" +
                    "-fx-cursor: hand;"
            );

            addButton.setOnAction(
                    event ->
                            showAddMemberDialog()
            );

            header.getChildren().add(
                    addButton
            );

            // -------------------------------------------------
            // RENAME GROUP
            // -------------------------------------------------

            Button renameButton =
                    new Button("Rename Group");

            renameButton.setStyle(
                    "-fx-background-color: #3b82f6;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 9 15;" +
                    "-fx-cursor: hand;"
            );

            renameButton.setOnAction(
                    event ->
                            handleRenameGroup()
            );

            header.getChildren().add(
                    renameButton
            );

            // -------------------------------------------------
            // DELETE GROUP
            // -------------------------------------------------

            Button deleteButton =
                    new Button("Delete Group");

            deleteButton.setStyle(
                    "-fx-background-color: #dc2626;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 9 15;" +
                    "-fx-cursor: hand;"
            );

            deleteButton.setOnAction(
                    event ->
                            handleDeleteGroup()
            );

            header.getChildren().add(
                    deleteButton
            );
        }

        // =====================================================
        // NORMAL MEMBER - LEAVE GROUP
        // =====================================================

        if (!isAdmin) {

            Button leaveButton =
                    new Button("Leave Group");

            leaveButton.setStyle(
                    "-fx-background-color: #ef4444;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 9 15;" +
                    "-fx-cursor: hand;"
            );

            leaveButton.setOnAction(
                    event ->
                            handleLeaveGroup()
            );

            header.getChildren().add(
                    leaveButton
            );
        }

        // =====================================================
        // MEMBERS BOX
        // =====================================================

        membersBox =
                new VBox(10);

        membersBox.setPadding(
                new Insets(5)
        );

        // =====================================================
        // SCROLL PANE
        // =====================================================

        ScrollPane scrollPane =
                new ScrollPane(
                        membersBox
                );

        scrollPane.setFitToWidth(true);

        scrollPane.setPrefHeight(
                380
        );

        scrollPane.setPrefWidth(
                500
        );

        scrollPane.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background: transparent;"
        );

        // =====================================================
        // CLOSE BUTTON
        // =====================================================

        Button closeButton =
                new Button("Close");

        closeButton.setPrefWidth(
                100
        );

        closeButton.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #172033;" +
                "-fx-border-color: #d7e1ed;" +
                "-fx-border-radius: 7;" +
                "-fx-background-radius: 7;" +
                "-fx-padding: 8 15;" +
                "-fx-cursor: hand;"
        );

        closeButton.setOnAction(
                event ->
                        stage.close()
        );

        HBox bottom =
                new HBox(
                        closeButton
                );

        bottom.setAlignment(
                Pos.CENTER_RIGHT
        );

        // =====================================================
        // ROOT
        // =====================================================

        root.getChildren().addAll(
                header,
                scrollPane,
                bottom
        );

        // =====================================================
        // SCENE
        // =====================================================

        Scene scene =
                new Scene(
                        root,
                        900,
                        520
                );

        stage.setScene(
                scene
        );

        stage.show();

        // =====================================================
        // LOAD MEMBERS
        // =====================================================

        loadMembers();
    }

    // =========================================================
    // LOAD MEMBERS
    // =========================================================

    public void loadMembers() {

        if (membersBox == null
                || memberCountLabel == null) {

            return;
        }

        List<GroupMember> members =
                groupMemberDAO.getGroupMembers(
                        groupId
                );

        membersBox
                .getChildren()
                .clear();

        memberCountLabel.setText(
                members.size()
                        + (
                        members.size() == 1
                                ? " member"
                                : " members"
                )
        );

        boolean isAdmin =
                currentUserId == groupCreatorId;

        // =====================================================
        // SHOW ADMIN FIRST
        // =====================================================

        for (GroupMember member : members) {

            if (member.getUserId()
                    == groupCreatorId) {

                membersBox.getChildren().add(
                        createMemberCard(
                                member,
                                isAdmin
                        )
                );
            }
        }

        // =====================================================
        // SHOW OTHER MEMBERS
        // =====================================================

        for (GroupMember member : members) {

            if (member.getUserId()
                    != groupCreatorId) {

                membersBox.getChildren().add(
                        createMemberCard(
                                member,
                                isAdmin
                        )
                );
            }
        }

        // =====================================================
        // EMPTY STATE
        // =====================================================

        if (members.isEmpty()) {

            Label emptyLabel =
                    new Label(
                            "No members found."
                    );

            emptyLabel.setStyle(
                    "-fx-text-fill: #64748b;" +
                    "-fx-font-size: 14px;" +
                    "-fx-padding: 20;"
            );

            membersBox.getChildren().add(
                    emptyLabel
            );
        }
    }

    // =========================================================
    // CREATE MEMBER CARD
    // =========================================================

    private HBox createMemberCard(
            GroupMember member,
            boolean isAdmin) {

        HBox card =
                new HBox(12);

        card.setAlignment(
                Pos.CENTER_LEFT
        );

        card.setPadding(
                new Insets(12)
        );

        boolean memberIsAdmin =
                member.getUserId()
                        == groupCreatorId;

        // =====================================================
        // ADMIN CARD STYLE
        // =====================================================

        if (memberIsAdmin) {

            card.setStyle(
                    "-fx-background-color: #eef5ff;" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: #4f8fe8;" +
                    "-fx-border-radius: 10;" +
                    "-fx-border-width: 1.2;"
            );

        } else {

            card.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: #e2eaf4;" +
                    "-fx-border-radius: 10;"
            );
        }

        // =====================================================
        // AVATAR
        // =====================================================

        Circle avatar =
                new Circle(22);

        avatar.setFill(
                Color.web(
                        memberIsAdmin
                                ? "#3b82f6"
                                : "#94a3b8"
                )
        );

        String username =
                member.getUsername();

        String firstLetter =
                "?";

        if (username != null
                && !username.trim().isEmpty()) {

            firstLetter =
                    username
                            .trim()
                            .substring(0, 1)
                            .toUpperCase();
        }

        Label avatarText =
                new Label(
                        firstLetter
                );

        avatarText.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
        );

        VBox avatarBox =
                new VBox(
                        avatarText
                );

        avatarBox.setAlignment(
                Pos.CENTER
        );

        avatarBox.setPrefWidth(
                44
        );

        // =====================================================
        // MEMBER DETAILS
        // =====================================================

        VBox details =
                new VBox(4);

        // -----------------------------------------------------
        // USERNAME
        // -----------------------------------------------------

        Label usernameLabel =
                new Label(
                        member.getUsername()
                );

        usernameLabel.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #172033;"
        );

        // -----------------------------------------------------
        // EMAIL
        // -----------------------------------------------------

        Label emailLabel =
                new Label(
                        member.getEmail()
                );

        emailLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #64748b;"
        );

        // -----------------------------------------------------
        // ROLE
        // -----------------------------------------------------

        HBox roleBox =
                new HBox(6);

        roleBox.setAlignment(
                Pos.CENTER_LEFT
        );

        if (memberIsAdmin) {

            Label crownLabel =
                    new Label("👑");

            crownLabel.setStyle(
                    "-fx-font-size: 12px;"
            );

            Label roleLabel =
                    new Label(
                            "GROUP ADMIN"
                    );

            roleLabel.setStyle(
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #3b82f6;"
            );

            roleBox.getChildren().addAll(
                    crownLabel,
                    roleLabel
            );

        } else {

            Label roleLabel =
                    new Label(
                            "MEMBER"
                    );

            roleLabel.setStyle(
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #64748b;"
            );

            roleBox.getChildren().add(
                    roleLabel
            );
        }

        details.getChildren().addAll(
                usernameLabel,
                emailLabel,
                roleBox
        );

        HBox.setHgrow(
                details,
                Priority.ALWAYS
        );

        // =====================================================
        // ADD ELEMENTS TO CARD
        // =====================================================

        card.getChildren().addAll(
                avatarBox,
                details
        );

        // =====================================================
        // ADMIN REMOVE BUTTON
        // =====================================================

        if (isAdmin
                && !memberIsAdmin
                && member.getUserId()
                != currentUserId) {

            Button removeButton =
                    new Button(
                            "Remove"
                    );

            removeButton.setStyle(
                    "-fx-background-color: #ef4444;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 7;" +
                    "-fx-padding: 7 12;" +
                    "-fx-cursor: hand;"
            );

            removeButton.setOnAction(
                    event ->
                            handleRemoveMember(
                                    member
                            )
            );

            card.getChildren().add(
                    removeButton
            );
        }

        return card;
    }

    // =========================================================
    // REMOVE MEMBER - ADMIN ONLY
    // =========================================================

    private void handleRemoveMember(
            GroupMember member) {

        if (currentUserId
                != groupCreatorId) {

            showWarning(
                    "Permission Denied",
                    "Only the group admin can remove members."
            );

            return;
        }

        if (member.getUserId()
                == groupCreatorId) {

            showWarning(
                    "Cannot Remove Admin",
                    "The group admin cannot be removed from the group."
            );

            return;
        }

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "Remove Member"
        );

        confirmation.setHeaderText(
                "Remove "
                        + member.getUsername()
                        + "?"
        );

        confirmation.setContentText(
                "Are you sure you want to remove "
                        + member.getUsername()
                        + " from this group?"
        );

        ButtonType cancelButton =
                new ButtonType(
                        "CANCEL",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        ButtonType removeButton =
                new ButtonType(
                        "REMOVE",
                        ButtonBar.ButtonData.OK_DONE
                );

        confirmation
                .getButtonTypes()
                .setAll(
                        cancelButton,
                        removeButton
                );

        confirmation.showAndWait()
                .ifPresent(result -> {

                    if (result != removeButton) {
                        return;
                    }

                    boolean removed =
                            groupMemberDAO.removeMember(
                                    groupId,
                                    member.getUserId()
                            );

                    if (!removed) {

                        showError(
                                "Remove Member",
                                "Unable to remove member",
                                "The member could not be removed."
                        );

                        return;
                    }

                    loadMembers();

                    if (socketClient != null
                            && socketClient.isConnected()) {

                        socketClient
                                .notifyGroupMemberRemoved(
                                        groupId,
                                        member.getUserId()
                                );
                    }

                    showInformation(
                            "Member Removed",
                            member.getUsername()
                                    + " has been removed from the group."
                    );
                });
    }

    // =========================================================
    // RENAME GROUP - ADMIN ONLY
    // =========================================================

    private void handleRenameGroup() {

        if (currentUserId
                != groupCreatorId) {

            showWarning(
                    "Permission Denied",
                    "Only the group admin can rename the group."
            );

            return;
        }

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "Rename Group"
        );

        dialog.setHeaderText(
                "Rename " + groupName
        );

        ButtonType renameButton =
                new ButtonType(
                        "RENAME",
                        ButtonBar.ButtonData.OK_DONE
                );

        ButtonType cancelButton =
                new ButtonType(
                        "CANCEL",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        renameButton,
                        cancelButton
                );

        VBox content =
                new VBox(10);

        content.setPadding(
                new Insets(20)
        );

        content.setPrefWidth(
                350
        );

        Label nameLabel =
                new Label(
                        "New Group Name"
                );

        nameLabel.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
        );

        TextField nameField =
                new TextField();

        nameField.setText(
                groupName
        );

        nameField.selectAll();

        nameField.setPromptText(
                "Enter new group name"
        );

        content.getChildren().addAll(
                nameLabel,
                nameField
        );

        dialog.getDialogPane()
                .setContent(
                        content
                );

        dialog.setResultConverter(
                button -> {

                    if (button != renameButton) {
                        return button;
                    }

                    String newGroupName =
                            nameField
                                    .getText()
                                    .trim();

                    if (newGroupName.isEmpty()) {

                        showWarning(
                                "Invalid Group Name",
                                "Group name cannot be empty."
                        );

                        return null;
                    }

                    if (newGroupName.equals(
                            groupName
                    )) {

                        showInformation(
                                "Rename Group",
                                "The group already has this name."
                        );

                        return null;
                    }

                    boolean renamed =
                            groupDAO.renameGroup(
                                    groupId,
                                    currentUserId,
                                    newGroupName
                            );

                    if (!renamed) {

                        showError(
                                "Rename Group",
                                "Unable to rename group",
                                "The group could not be renamed."
                        );

                        return null;
                    }

                    if (socketClient != null
                            && socketClient.isConnected()) {

                        socketClient
                                .notifyGroupRenamed(
                                        groupId,
                                        newGroupName
                                );
                    }

                    showInformation(
                            "Group Renamed",
                            "Group renamed successfully to "
                                    + newGroupName
                    );

                    if (stage != null) {
                        stage.close();
                    }

                    return button;
                }
        );

        dialog.showAndWait();
    }

    // =========================================================
    // DELETE GROUP - ADMIN ONLY
    // =========================================================

    private void handleDeleteGroup() {

        if (currentUserId
                != groupCreatorId) {

            showWarning(
                    "Permission Denied",
                    "Only the group admin can delete the group."
            );

            return;
        }

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "Delete Group"
        );

        confirmation.setHeaderText(
                "Delete " + groupName + "?"
        );

        confirmation.setContentText(
                "This will permanently delete the group, "
                        + "all group members, and all group messages."
                        + "\n\n"
                        + "This action cannot be undone."
        );

        ButtonType cancelButton =
                new ButtonType(
                        "CANCEL",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        ButtonType deleteButton =
                new ButtonType(
                        "DELETE GROUP",
                        ButtonBar.ButtonData.OK_DONE
                );

        confirmation
                .getButtonTypes()
                .setAll(
                        cancelButton,
                        deleteButton
                );

        confirmation.showAndWait()
                .ifPresent(result -> {

                    if (result != deleteButton) {
                        return;
                    }

                    boolean deleted =
                            groupDAO.deleteGroup(
                                    groupId,
                                    currentUserId
                            );

                    if (!deleted) {

                        showError(
                                "Delete Group",
                                "Unable to delete group",
                                "The group could not be deleted."
                        );

                        return;
                    }

                    if (socketClient != null
                            && socketClient.isConnected()) {

                        socketClient
                                .notifyGroupDeleted(
                                        groupId
                                );
                    }

                    showInformation(
                            "Group Deleted",
                            groupName
                                    + " has been deleted successfully."
                    );

                    if (stage != null) {
                        stage.close();
                    }
                });
    }

    // =========================================================
    // LEAVE GROUP - NORMAL MEMBER ONLY
    // =========================================================

    private void handleLeaveGroup() {

        if (currentUserId
                == groupCreatorId) {

            showWarning(
                    "Cannot Leave Group",
                    "The group admin cannot leave the group."
            );

            return;
        }

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "Leave Group"
        );

        confirmation.setHeaderText(
                "Leave " + groupName + "?"
        );

        confirmation.setContentText(
                "Are you sure you want to leave this group?"
        );

        ButtonType cancelButton =
                new ButtonType(
                        "CANCEL",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        ButtonType leaveButton =
                new ButtonType(
                        "LEAVE GROUP",
                        ButtonBar.ButtonData.OK_DONE
                );

        confirmation
                .getButtonTypes()
                .setAll(
                        cancelButton,
                        leaveButton
                );

        confirmation.showAndWait()
                .ifPresent(result -> {

                    if (result != leaveButton) {
                        return;
                    }

                    boolean left =
                            groupMemberDAO.leaveGroup(
                                    groupId,
                                    currentUserId
                            );

                    if (!left) {

                        showError(
                                "Leave Group",
                                "Unable to leave group",
                                "You could not leave this group."
                        );

                        return;
                    }

                    if (socketClient != null
                            && socketClient.isConnected()) {

                        socketClient
                                .notifyGroupMemberLeft(
                                        groupId,
                                        currentUserId
                                );
                    }

                    showInformation(
                            "Left Group",
                            "You have left "
                                    + groupName
                                    + "."
                    );

                    if (stage != null) {
                        stage.close();
                    }
                });
    }

    // =========================================================
    // ADD MEMBER DIALOG
    // =========================================================

    private void showAddMemberDialog() {

        if (currentUserId
                != groupCreatorId) {

            showWarning(
                    "Permission Denied",
                    "Only the group admin can add members."
            );

            return;
        }

        AddMemberController controller =
                new AddMemberController(
                        groupId,
                        groupName,
                        connection,
                        this,
                        socketClient
                );

        controller.show();
    }

    // =========================================================
    // WARNING ALERT
    // =========================================================

    private void showWarning(
            String title,
            String message) {

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
    // INFORMATION ALERT
    // =========================================================

    private void showInformation(
            String title,
            String message) {

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
    // ERROR ALERT
    // =========================================================

    private void showError(
            String title,
            String header,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                header
        );

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }
}