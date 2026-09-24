package com.chatapp.controller;

import com.chatapp.client.ChatSocketClient;
import com.chatapp.dao.GroupDAO;
import com.chatapp.dao.GroupMessageDAO;
import com.chatapp.dao.MessageDAO;
import com.chatapp.dao.UserDAO;
import com.chatapp.model.Group;
import com.chatapp.model.GroupMessage;
import com.chatapp.model.Message;
import com.chatapp.model.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @FXML
    private Button membersButton;


    // =========================================================
    // USER INFORMATION
    // =========================================================

    private String username;

    private User currentUser;

    private User selectedUser;

    private Group selectedGroup;


    // =========================================================
    // DATABASE OBJECTS
    // =========================================================

    private final UserDAO userDAO =
            new UserDAO();

    private final MessageDAO messageDAO =
            new MessageDAO();

    private final GroupDAO groupDAO =
            new GroupDAO();

    private final GroupMessageDAO groupMessageDAO =
            new GroupMessageDAO();


    // =========================================================
    // SOCKET CLIENT
    // =========================================================

    private final ChatSocketClient socketClient =
            new ChatSocketClient();


    // =========================================================
    // ONLINE USERS
    // =========================================================

    private final Set<String> onlineUsers =
            new HashSet<>();


    // =========================================================
    // TIME FORMAT
    // =========================================================

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("hh:mm a");


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    private void initialize() {

        setupUserListCellFactory();

        setupMessageCellFactory();


        // =====================================================
        // PRIVATE USER SELECTION
        // =====================================================

        userListView.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {

                            if (newValue != null) {

                                String selectedUsername =
                                        newValue
                                                .replace(
                                                        "[ONLINE]",
                                                        ""
                                                )
                                                .replace(
                                                        "[OFFLINE]",
                                                        ""
                                                )
                                                .trim();

                                openPrivateChat(
                                        selectedUsername
                                );
                            }
                        }
                );


        // =====================================================
        // GROUP SELECTION
        // =====================================================

        groupListView.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {

                            if (newValue != null) {

                                openGroupChat(
                                        newValue
                                );
                            }
                        }
                );
    }


    // =========================================================
    // USER LIST CELL DESIGN
    // =========================================================

    private void setupUserListCellFactory() {

        userListView.setCellFactory(
                listView ->
                        new ListCell<String>() {

                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty
                                        || item == null) {

                                    setText(null);

                                    setGraphic(null);

                                    return;
                                }

                                String userName =
                                        item
                                                .replace(
                                                        "[ONLINE]",
                                                        ""
                                                )
                                                .replace(
                                                        "[OFFLINE]",
                                                        ""
                                                )
                                                .trim();

                                boolean isOnline =
                                        item.startsWith(
                                                "[ONLINE]"
                                        );

                                Circle statusCircle =
                                        new Circle(5);

                                if (isOnline) {

                                    statusCircle.setStyle(
                                            "-fx-fill: #22c55e;"
                                    );

                                } else {

                                    statusCircle.setStyle(
                                            "-fx-fill: #9ca3af;"
                                    );
                                }

                                Label nameLabel =
                                        new Label(
                                                userName
                                        );

                                nameLabel.setStyle(
                                        "-fx-font-size: 14px;"
                                                + "-fx-text-fill: #111827;"
                                );

                                int unread = 0;

                                if (currentUser != null) {

                                    User otherUser =
                                            userDAO.findUserByUsername(
                                                    userName
                                            );

                                    if (otherUser != null) {

                                        unread =
                                                messageDAO.getUnreadCount(
                                                        currentUser.getId(),
                                                        otherUser.getId()
                                                );
                                    }
                                }

                                Label unreadLabel =
                                        new Label();

                                if (unread > 0) {

                                    unreadLabel.setText(
                                            String.valueOf(
                                                    unread
                                            )
                                    );

                                    unreadLabel.setStyle(
                                            "-fx-background-color: #ef4444;"
                                                    + "-fx-text-fill: white;"
                                                    + "-fx-font-size: 11px;"
                                                    + "-fx-font-weight: bold;"
                                                    + "-fx-background-radius: 20px;"
                                                    + "-fx-padding: 3px 7px;"
                                    );
                                }

                                HBox container =
                                        new HBox(10);

                                container.setAlignment(
                                        Pos.CENTER_LEFT
                                );

                                HBox spacer =
                                        new HBox();

                                HBox.setHgrow(
                                        spacer,
                                        Priority.ALWAYS
                                );

                                container
                                        .getChildren()
                                        .addAll(
                                                statusCircle,
                                                nameLabel,
                                                spacer
                                        );

                                if (unread > 0) {

                                    container
                                            .getChildren()
                                            .add(
                                                    unreadLabel
                                            );
                                }

                                setText(null);

                                setGraphic(
                                        container
                                );
                            }
                        }
        );
    }


    // =========================================================
    // MESSAGE BUBBLE DESIGN
    // =========================================================

    private void setupMessageCellFactory() {

        messageListView.setCellFactory(
                listView ->
                        new ListCell<String>() {

                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty
                                        || item == null) {

                                    setText(null);

                                    setGraphic(null);

                                    return;
                                }

                                String[] parts =
                                        item.split(
                                                "\\|",
                                                3
                                        );

                                if (parts.length < 3) {

                                    setText(item);

                                    setGraphic(null);

                                    return;
                                }

                                String sender =
                                        parts[0];

                                String message =
                                        parts[1];

                                String time =
                                        parts[2];

                                boolean isSentByMe =
                                        sender.equals(
                                                "You"
                                        );

                                Label messageLabel =
                                        new Label(
                                                message
                                        );

                                messageLabel.setWrapText(
                                        true
                                );

                                messageLabel.setMaxWidth(
                                        400
                                );

                                messageLabel
                                        .getStyleClass()
                                        .add(
                                                isSentByMe
                                                        ? "sent-message"
                                                        : "received-message"
                                        );

                                Label timeLabel =
                                        new Label(
                                                time
                                        );

                                timeLabel
                                        .getStyleClass()
                                        .add(
                                                "message-time"
                                        );

                                VBox bubble =
                                        new VBox(3);

                                bubble
                                        .getStyleClass()
                                        .add(
                                                isSentByMe
                                                        ? "sent-bubble"
                                                        : "received-bubble"
                                        );

                                if (selectedGroup != null
                                        && !isSentByMe) {

                                    Label senderLabel =
                                            new Label(
                                                    sender
                                            );

                                    senderLabel.setStyle(
                                            "-fx-font-size: 11px;"
                                                    + "-fx-font-weight: bold;"
                                                    + "-fx-text-fill: #2563eb;"
                                    );

                                    bubble
                                            .getChildren()
                                            .add(
                                                    senderLabel
                                            );
                                }

                                bubble
                                        .getChildren()
                                        .addAll(
                                                messageLabel,
                                                timeLabel
                                        );

                                HBox container =
                                        new HBox();

                                container.setMaxWidth(
                                        Double.MAX_VALUE
                                );

                                if (isSentByMe) {

                                    container.setAlignment(
                                            Pos.CENTER_RIGHT
                                    );

                                } else {

                                    container.setAlignment(
                                            Pos.CENTER_LEFT
                                    );
                                }

                                container
                                        .getChildren()
                                        .add(
                                                bubble
                                        );

                                setText(null);

                                setGraphic(
                                        container
                                );
                            }
                        }
        );
    }


    // =========================================================
    // SET USERNAME
    // =========================================================

    public void setUsername(
            String username
    ) {

        this.username = username;

        welcomeLabel.setText(
                "Welcome, " + username + "!"
        );

        currentUser =
                userDAO.findUserByUsername(
                        username
                );

        loadUsers();

        loadGroups();

        connectToSocketServer();
    }


    // =========================================================
    // LOAD USERS
    // =========================================================

    private void loadUsers() {

        List<User> users =
                userDAO.getAllUsers();

        List<String> usernames =
                users.stream()

                        .filter(
                                user ->
                                        !user.getUsername()
                                                .equals(
                                                        username
                                                )
                        )

                        .map(user -> {

                            String name =
                                    user.getUsername();

                            if (onlineUsers.contains(name)) {

                                return "[ONLINE] "
                                        + name;

                            } else {

                                return "[OFFLINE] "
                                        + name;
                            }
                        })

                        .toList();

        userListView.setItems(
                FXCollections.observableArrayList(
                        usernames
                )
        );
    }


    // =========================================================
    // LOAD GROUPS
    // =========================================================

    private void loadGroups() {

        if (currentUser == null) {

            return;
        }

        List<Group> groups =
                groupDAO.getGroupsForUser(
                        currentUser.getId()
                );

        List<String> groupNames =
                groups.stream()

                        .map(
                                Group::getGroupName
                        )

                        .toList();

        groupListView.setItems(
                FXCollections.observableArrayList(
                        groupNames
                )
        );
    }


    // =========================================================
    // CONNECT TO SOCKET SERVER
    // =========================================================

    private void connectToSocketServer() {

        boolean connected =
                socketClient.connect(
                        username
                );

        if (connected) {

            System.out.println(
                    "Connected to real-time chat server."
            );

            socketClient.setMessageListener(
                    this::handleServerMessage
            );

        } else {

            System.out.println(
                    "Could not connect to chat server."
            );
        }
    }


    // =========================================================
    // HANDLE SERVER MESSAGE
    // =========================================================

    private void handleServerMessage(
            String serverMessage
    ) {

        // =====================================================
        // ONLINE USERS
        // =====================================================

        if (serverMessage.startsWith(
                "ONLINE_USERS|"
        )) {

            updateOnlineUsers(
                    serverMessage
            );

            return;
        }


        // =====================================================
        // GROUP CREATED
        // =====================================================

        if (serverMessage.startsWith(
                "GROUP_CREATED|"
        )) {

            Platform.runLater(() -> {

                System.out.println(
                        "New group created. Refreshing groups..."
                );

                loadGroups();
            });

            return;
        }


        // =====================================================
        // GROUP MEMBER ADDED
        // =====================================================

        if (serverMessage.startsWith(
                "GROUP_MEMBER_ADDED|"
        )) {

            Platform.runLater(() -> {

                System.out.println(
                        "New member added to group. "
                                + "Refreshing groups..."
                );

                loadGroups();
            });

            return;
        }


        // =====================================================
        // GROUP MESSAGE
        // =====================================================

        if (serverMessage.startsWith(
                "GROUP_MESSAGE|"
        )) {

            handleIncomingGroupMessage(
                    serverMessage
            );

            return;
        }


        // =====================================================
        // GROUP MESSAGE SENT
        // =====================================================

        if (serverMessage.equals(
                "GROUP_MESSAGE_SENT"
        )) {

            return;
        }


        // =====================================================
        // GROUP MEMBER ADDED SUCCESS
        // =====================================================

        if (serverMessage.equals(
                "GROUP_MEMBER_ADDED_SUCCESS"
        )) {

            return;
        }


        // =====================================================
        // MESSAGE SENT
        // =====================================================

        if (serverMessage.equals(
                "MESSAGE_SENT"
        )) {

            return;
        }


        // =====================================================
        // USER OFFLINE
        // =====================================================

        if (serverMessage.startsWith(
                "USER_OFFLINE|"
        )) {

            System.out.println(
                    serverMessage
            );

            return;
        }


        // =====================================================
        // ERROR
        // =====================================================

        if (serverMessage.startsWith(
                "ERROR|"
        )) {

            System.out.println(
                    serverMessage
            );

            return;
        }


        // =====================================================
        // NORMAL PRIVATE MESSAGE
        // =====================================================

        handleIncomingMessage(
                serverMessage
        );
    }


    // =========================================================
    // UPDATE ONLINE USERS
    // =========================================================

    private void updateOnlineUsers(
            String serverMessage
    ) {

        String usersPart =
                serverMessage.substring(
                        "ONLINE_USERS|".length()
                );

        Set<String> updatedUsers =
                new HashSet<>();

        if (!usersPart.isBlank()) {

            String[] users =
                    usersPart.split(",");

            for (String user : users) {

                if (!user.isBlank()) {

                    updatedUsers.add(
                            user.trim()
                    );
                }
            }
        }

        Platform.runLater(() -> {

            onlineUsers.clear();

            onlineUsers.addAll(
                    updatedUsers
            );

            loadUsers();
        });
    }


    // =========================================================
    // OPEN PRIVATE CHAT
    // =========================================================

    private void openPrivateChat(
            String selectedUsername
    ) {

        selectedGroup = null;

        membersButton.setVisible(false);
        membersButton.setManaged(false);

        selectedUser =
                userDAO.findUserByUsername(
                        selectedUsername
                );

        if (selectedUser == null) {

            return;
        }

        if (currentUser != null) {

            messageDAO.markMessagesAsRead(
                    currentUser.getId(),
                    selectedUser.getId()
            );
        }

        chatTitleLabel.setText(
                "Chat with "
                        + selectedUser.getUsername()
        );

        groupListView.getSelectionModel()
                .clearSelection();

        loadUsers();

        loadChatHistory();
    }


    // =========================================================
    // OPEN GROUP CHAT
    // =========================================================

    private void openGroupChat(
            String groupName
    ) {

        selectedUser = null;

        selectedGroup = null;

        membersButton.setVisible(true);
        membersButton.setManaged(true);

        List<Group> groups =
                groupDAO.getGroupsForUser(
                        currentUser.getId()
                );

        for (Group group : groups) {

            if (group.getGroupName()
                    .equals(groupName)) {

                selectedGroup = group;

                break;
            }
        }

        if (selectedGroup == null) {

            return;
        }

        chatTitleLabel.setText(
                "Group: "
                        + selectedGroup.getGroupName()
        );

        userListView.getSelectionModel()
                .clearSelection();

        loadGroupChatHistory();

        groupMessageDAO.markMessagesAsRead(
                selectedGroup.getId(),
                currentUser.getId()
        );

        System.out.println(
                "Selected group: "
                        + selectedGroup.getGroupName()
        );
    }


    // =========================================================
    // LOAD PRIVATE CHAT HISTORY
    // =========================================================

    private void loadChatHistory() {

        if (currentUser == null
                || selectedUser == null) {

            return;
        }

        List<Message> messages =
                messageDAO.getChatHistory(
                        currentUser.getId(),
                        selectedUser.getId()
                );

        List<String> chatMessages =
                messages.stream()

                        .map(message -> {

                            String sender;

                            if (message.getSenderId()
                                    == currentUser.getId()) {

                                sender = "You";

                            } else {

                                sender =
                                        selectedUser
                                                .getUsername();
                            }

                            String time = "";

                            if (message.getSentAt() != null) {

                                time =
                                        message.getSentAt()
                                                .format(
                                                        TIME_FORMAT
                                                );
                            }

                            return sender
                                    + "|"
                                    + message.getMessage()
                                    + "|"
                                    + time;
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


    // =========================================================
    // LOAD GROUP CHAT HISTORY
    // =========================================================

    private void loadGroupChatHistory() {

        if (currentUser == null
                || selectedGroup == null) {

            return;
        }

        List<GroupMessage> messages =
                groupMessageDAO.getGroupMessages(
                        selectedGroup.getId()
                );

        List<String> groupMessages =
                messages.stream()

                        .map(message -> {

                            String sender;

                            if (message.getSenderId()
                                    == currentUser.getId()) {

                                sender = "You";

                            } else {

                                sender =
                                        getUsernameById(
                                                message.getSenderId()
                                        );
                            }

                            String time = "";

                            if (message.getSentAt() != null) {

                                time =
                                        message.getSentAt()
                                                .format(
                                                        TIME_FORMAT
                                                );
                            }

                            return sender
                                    + "|"
                                    + message.getMessage()
                                    + "|"
                                    + time;
                        })

                        .toList();

        messageListView.setItems(
                FXCollections.observableArrayList(
                        groupMessages
                )
        );

        if (!groupMessages.isEmpty()) {

            messageListView.scrollTo(
                    groupMessages.size() - 1
            );
        }
    }


    // =========================================================
    // SEND MESSAGE
    // =========================================================

    @FXML
    private void handleSendMessage() {

        if (selectedGroup != null) {

            sendGroupMessage();

            return;
        }

        if (selectedUser != null) {

            sendPrivateMessage();

            return;
        }

        chatTitleLabel.setText(
                "Please select a user or group first"
        );
    }


    // =========================================================
    // SEND PRIVATE MESSAGE
    // =========================================================

    private void sendPrivateMessage() {

        String text =
                messageField.getText().trim();

        if (text.isEmpty()) {

            return;
        }

        if (!socketClient.isConnected()) {

            System.out.println(
                    "Socket is not connected."
            );

            return;
        }

        socketClient.sendMessage(
                selectedUser.getUsername(),
                text
        );

        Message message =
                new Message(
                        currentUser.getId(),
                        selectedUser.getId(),
                        text
                );

        boolean saved =
                messageDAO.saveMessage(
                        message
                );

        if (saved) {

            messageField.clear();

            loadChatHistory();

        } else {

            System.out.println(
                    "Message could not be saved."
            );
        }
    }


    // =========================================================
    // SEND GROUP MESSAGE
    // =========================================================

    private void sendGroupMessage() {

        String text =
                messageField.getText().trim();

        if (text.isEmpty()) {

            return;
        }

        if (selectedGroup == null) {

            return;
        }

        if (!socketClient.isConnected()) {

            System.out.println(
                    "Socket is not connected."
            );

            return;
        }

        GroupMessage message =
                new GroupMessage(
                        selectedGroup.getId(),
                        currentUser.getId(),
                        text
                );

        boolean saved =
                groupMessageDAO.saveMessage(
                        message
                );

        if (!saved) {

            System.out.println(
                    "Group message could not be saved."
            );

            return;
        }

        socketClient.sendGroupMessage(
                selectedGroup.getId(),
                text
        );

        messageField.clear();
    }


    // =========================================================
    // HANDLE INCOMING PRIVATE MESSAGE
    // =========================================================

    private void handleIncomingMessage(
            String incomingMessage
    ) {

        String[] parts =
                incomingMessage.split(
                        "\\|",
                        2
                );

        if (parts.length < 2) {

            return;
        }

        String senderUsername =
                parts[0];

        String messageText =
                parts[1];

        Platform.runLater(() -> {

            boolean isCurrentChat =
                    selectedUser != null
                            && selectedUser
                                    .getUsername()
                                    .equals(
                                            senderUsername
                                    );

            if (isCurrentChat) {

                User sender =
                        userDAO.findUserByUsername(
                                senderUsername
                        );

                if (sender != null
                        && currentUser != null) {

                    messageDAO.markMessagesAsRead(
                            currentUser.getId(),
                            sender.getId()
                    );
                }

                loadChatHistory();

                loadUsers();

            } else {

                loadUsers();

                System.out.println(
                        "Unread message from "
                                + senderUsername
                                + ": "
                                + messageText
                );
            }
        });
    }


    // =========================================================
    // HANDLE INCOMING GROUP MESSAGE
    // =========================================================

    private void handleIncomingGroupMessage(
            String serverMessage
    ) {

        String[] parts =
                serverMessage.split(
                        "\\|",
                        4
                );

        if (parts.length < 4) {

            return;
        }

        int groupId;

        try {

            groupId =
                    Integer.parseInt(
                            parts[1]
                    );

        } catch (NumberFormatException e) {

            System.out.println(
                    "Invalid group ID."
            );

            return;
        }

        String senderUsername =
                parts[2];

        String messageText =
                parts[3];

        Platform.runLater(() -> {

            boolean isCurrentGroup =
                    selectedGroup != null
                            && selectedGroup.getId()
                                    == groupId;

            if (isCurrentGroup) {

                loadGroupChatHistory();

                groupMessageDAO.markMessagesAsRead(
                        groupId,
                        currentUser.getId()
                );

            } else {

                System.out.println(
                        "New group message from "
                                + senderUsername
                                + ": "
                                + messageText
                );
            }
        });
    }


    // =========================================================
    // GET USERNAME BY ID
    // =========================================================

    private String getUsernameById(
            int userId
    ) {

        List<User> users =
                userDAO.getAllUsers();

        for (User user : users) {

            if (user.getId() == userId) {

                return user.getUsername();
            }
        }

        return "User";
    }


    // =========================================================
    // VIEW GROUP MEMBERS
    // =========================================================

    @FXML
    private void handleViewMembers() {

        if (selectedGroup == null) {

            return;
        }

        List<User> members =
                groupDAO.getGroupMembers(
                        selectedGroup.getId()
                );

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "Group Members"
        );

        dialog.setHeaderText(
                "Members of "
                        + selectedGroup.getGroupName()
        );

        ButtonType addMemberButton =
                new ButtonType(
                        "ADD MEMBER",
                        ButtonBar.ButtonData.LEFT
                );

        ButtonType closeButton =
                new ButtonType(
                        "CLOSE",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        addMemberButton,
                        closeButton
                );

        VBox membersBox =
                new VBox(10);

        membersBox.setPadding(
                new Insets(15)
        );

        for (User member : members) {

            HBox memberRow =
                    new HBox(10);

            memberRow.setAlignment(
                    Pos.CENTER_LEFT
            );

            Circle profileCircle =
                    new Circle(18);

            profileCircle.setStyle(
                    "-fx-fill: #4f8fe8;"
            );

            String firstLetter =
                    member.getUsername()
                            .isEmpty()
                            ? "?"
                            : member.getUsername()
                                    .substring(0, 1)
                                    .toUpperCase();

            Label initial =
                    new Label(
                            firstLetter
                    );

            initial.setStyle(
                    "-fx-text-fill: white;"
                            + "-fx-font-weight: bold;"
            );

            StackPane avatar =
                    new StackPane(
                            profileCircle,
                            initial
                    );

            Label usernameLabel =
                    new Label(
                            member.getUsername()
                    );

            usernameLabel.setStyle(
                    "-fx-font-size: 14px;"
                            + "-fx-font-weight: bold;"
                            + "-fx-text-fill: #172033;"
            );

            if (currentUser != null
                    && member.getId()
                    == currentUser.getId()) {

                Label youLabel =
                        new Label("(You)");

                youLabel.setStyle(
                        "-fx-text-fill: #64748b;"
                                + "-fx-font-size: 12px;"
                );

                memberRow.getChildren()
                        .addAll(
                                avatar,
                                usernameLabel,
                                youLabel
                        );

            } else {

                memberRow.getChildren()
                        .addAll(
                                avatar,
                                usernameLabel
                        );
            }

            membersBox.getChildren()
                    .add(memberRow);
        }

        if (members.isEmpty()) {

            Label emptyLabel =
                    new Label(
                            "No members found."
                    );

            emptyLabel.setStyle(
                    "-fx-text-fill: #64748b;"
            );

            membersBox.getChildren()
                    .add(emptyLabel);
        }

        ScrollPane scrollPane =
                new ScrollPane(
                        membersBox
                );

        scrollPane.setFitToWidth(
                true
        );

        scrollPane.setPrefHeight(
                300
        );

        scrollPane.setPrefWidth(
                300
        );

        dialog.getDialogPane()
                .setContent(
                        scrollPane
                );


        // =====================================================
        // ADD MEMBER BUTTON
        // =====================================================

        Button addButton =
                (Button)
                        dialog.getDialogPane()
                                .lookupButton(
                                        addMemberButton
                                );

        addButton.setOnAction(event -> {

            dialog.close();

            showAddMemberDialog();

        });


        // =====================================================
        // ONLY GROUP CREATOR CAN ADD MEMBERS
        // =====================================================

        boolean isGroupCreator =
                currentUser != null
                        && selectedGroup != null
                        && currentUser.getId()
                                == selectedGroup.getCreatedBy();

        addButton.setDisable(
                !isGroupCreator
        );

        if (!isGroupCreator) {

            addButton.setTooltip(
                    new javafx.scene.control.Tooltip(
                            "Only the group creator can add members."
                    )
            );
        }


        dialog.showAndWait();
    }


    // =========================================================
    // SHOW ADD MEMBER DIALOG
    // =========================================================

    private void showAddMemberDialog() {

        if (selectedGroup == null
                || currentUser == null) {

            return;
        }

        List<User> currentMembers =
                groupDAO.getGroupMembers(
                        selectedGroup.getId()
                );


        // =====================================================
        // GET EXISTING MEMBER IDs
        // =====================================================

        Set<Integer> existingMemberIds =
                new HashSet<>();

        for (User member : currentMembers) {

            existingMemberIds.add(
                    member.getId()
            );
        }


        // =====================================================
        // GET AVAILABLE USERS
        // =====================================================

        List<User> allUsers =
                userDAO.getAllUsers();

        List<User> availableUsers =
                new ArrayList<>();

        for (User user : allUsers) {

            if (!existingMemberIds.contains(
                    user.getId()
            )) {

                availableUsers.add(
                        user
                );
            }
        }


        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "Add Member"
        );

        dialog.setHeaderText(
                "Add member to "
                        + selectedGroup.getGroupName()
        );


        ButtonType addButtonType =
                new ButtonType(
                        "ADD",
                        ButtonBar.ButtonData.OK_DONE
                );

        ButtonType cancelButtonType =
                new ButtonType(
                        "CANCEL",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        addButtonType,
                        cancelButtonType
                );


        VBox content =
                new VBox(10);

        content.setPadding(
                new Insets(20)
        );

        content.setPrefWidth(
                320
        );


        Label titleLabel =
                new Label(
                        "Select a user to add:"
                );

        titleLabel.setStyle(
                "-fx-font-size: 14px;"
                        + "-fx-font-weight: bold;"
        );


        VBox usersBox =
                new VBox(8);


        List<CheckBox> checkBoxes =
                new ArrayList<>();


        // =====================================================
        // NO AVAILABLE USERS
        // =====================================================

        if (availableUsers.isEmpty()) {

            Label noUsersLabel =
                    new Label(
                            "All users are already members."
                    );

            noUsersLabel.setStyle(
                    "-fx-text-fill: #64748b;"
            );

            usersBox.getChildren()
                    .add(
                            noUsersLabel
                    );

        } else {

            for (User user :
                    availableUsers) {

                CheckBox checkBox =
                        new CheckBox(
                                user.getUsername()
                        );

                checkBox.setUserData(
                        user
                );

                checkBox.setStyle(
                        "-fx-font-size: 14px;"
                );

                checkBoxes.add(
                        checkBox
                );

                usersBox.getChildren()
                        .add(
                                checkBox
                        );
            }
        }


        ScrollPane scrollPane =
                new ScrollPane(
                        usersBox
                );

        scrollPane.setFitToWidth(
                true
        );

        scrollPane.setPrefHeight(
                180
        );

        scrollPane.setMaxHeight(
                180
        );


        content.getChildren()
                .addAll(
                        titleLabel,
                        scrollPane
                );


        dialog.getDialogPane()
                .setContent(
                        content
                );


        // =====================================================
        // ADD SELECTED USER
        // =====================================================

        dialog.setResultConverter(
                button -> {

                    if (button == addButtonType) {

                        List<User> selectedUsers =
                                new ArrayList<>();


                        for (CheckBox checkBox :
                                checkBoxes) {

                            if (checkBox.isSelected()) {

                                User user =
                                        (User)
                                                checkBox
                                                        .getUserData();

                                selectedUsers.add(
                                        user
                                );
                            }
                        }


                        if (selectedUsers.isEmpty()) {

                            Alert alert =
                                    new Alert(
                                            Alert.AlertType.WARNING
                                    );

                            alert.setTitle(
                                    "No Member Selected"
                            );

                            alert.setHeaderText(
                                    null
                            );

                            alert.setContentText(
                                    "Please select at least one user."
                            );

                            alert.showAndWait();

                            return null;
                        }


                        addMembersToExistingGroup(
                                selectedUsers
                        );
                    }

                    return button;
                }
        );


        dialog.showAndWait();
    }


    // =========================================================
    // ADD MEMBERS TO EXISTING GROUP
    // =========================================================

    private void addMembersToExistingGroup(
            List<User> selectedUsers
    ) {

        if (selectedGroup == null
                || currentUser == null) {

            return;
        }


        int addedMembers = 0;


        for (User user :
                selectedUsers) {

            boolean added =
                    groupDAO.addMemberToExistingGroup(
                            selectedGroup.getId(),
                            user.getId()
                    );


            if (added) {

                addedMembers++;


                // =================================================
                // NOTIFY SERVER
                // =================================================

                socketClient.notifyGroupMemberAdded(
                        selectedGroup.getId(),
                        user.getId()
                );
            }
        }


        // =====================================================
        // REFRESH CURRENT GROUP
        // =====================================================

        loadGroups();


        // =====================================================
        // SHOW RESULT
        // =====================================================

        if (addedMembers > 0) {

            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setTitle(
                    "Member Added"
            );

            alert.setHeaderText(
                    "Member added successfully!"
            );

            alert.setContentText(
                    addedMembers
                            + " member(s) added to "
                            + selectedGroup.getGroupName()
            );

            alert.showAndWait();

        } else {

            Alert alert =
                    new Alert(
                            Alert.AlertType.WARNING
                    );

            alert.setTitle(
                    "Member Not Added"
            );

            alert.setHeaderText(
                    null
            );

            alert.setContentText(
                    "No new members were added."
            );

            alert.showAndWait();
        }
    }


    // =========================================================
    // CREATE GROUP WITH MEMBERS
    // =========================================================

    @FXML
    private void handleCreateGroup() {

        if (currentUser == null) {

            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alert.setTitle(
                    "Error"
            );

            alert.setHeaderText(
                    null
            );

            alert.setContentText(
                    "No logged-in user found."
            );

            alert.showAndWait();

            return;
        }


        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "Create New Group"
        );

        dialog.setHeaderText(
                "Create a new chat group"
        );

        DialogPane dialogPane =
                dialog.getDialogPane();

        ButtonType createButton =
                new ButtonType(
                        "CREATE",
                        ButtonBar.ButtonData.OK_DONE
                );

        ButtonType cancelButton =
                new ButtonType(
                        "CANCEL",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        dialogPane.getButtonTypes()
                .addAll(
                        createButton,
                        cancelButton
                );


        // =====================================================
        // GROUP NAME
        // =====================================================

        Label groupNameLabel =
                new Label(
                        "Group Name"
                );

        TextField groupNameField =
                new TextField();

        groupNameField.setPromptText(
                "Enter group name"
        );


        // =====================================================
        // MEMBER SELECTION
        // =====================================================

        Label membersLabel =
                new Label(
                        "Select Members"
                );

        VBox membersBox =
                new VBox(8);

        membersBox.setPadding(
                new Insets(5)
        );

        List<CheckBox> memberCheckBoxes =
                new ArrayList<>();

        List<User> users =
                userDAO.getAllUsers();


        for (User user : users) {

            if (user.getId()
                    == currentUser.getId()) {

                continue;
            }

            CheckBox checkBox =
                    new CheckBox(
                            user.getUsername()
                    );

            checkBox.setUserData(
                    user
            );

            checkBox.setStyle(
                    "-fx-font-size: 14px;"
            );

            memberCheckBoxes.add(
                    checkBox
            );

            membersBox
                    .getChildren()
                    .add(
                            checkBox
                    );
        }


        if (memberCheckBoxes.isEmpty()) {

            Label noUsersLabel =
                    new Label(
                            "No other users available."
                    );

            noUsersLabel.setStyle(
                    "-fx-text-fill: #6b7280;"
            );

            membersBox
                    .getChildren()
                    .add(
                            noUsersLabel
                    );
        }


        ScrollPane scrollPane =
                new ScrollPane(
                        membersBox
                );

        scrollPane.setFitToWidth(
                true
        );

        scrollPane.setPrefHeight(
                150
        );

        scrollPane.setMaxHeight(
                150
        );


        VBox content =
                new VBox(10);

        content.setPadding(
                new Insets(20)
        );

        content.setPrefWidth(
                350
        );

        content.getChildren()
                .addAll(
                        groupNameLabel,
                        groupNameField,
                        membersLabel,
                        scrollPane
                );

        dialogPane.setContent(
                content
        );


        // =====================================================
        // CREATE BUTTON
        // =====================================================

        dialog.setResultConverter(
                button -> {

                    if (button == createButton) {

                        String groupName =
                                groupNameField
                                        .getText()
                                        .trim();


                        if (groupName.isEmpty()) {

                            Alert alert =
                                    new Alert(
                                            Alert.AlertType.WARNING
                                    );

                            alert.setTitle(
                                    "Invalid Group Name"
                            );

                            alert.setHeaderText(
                                    null
                            );

                            alert.setContentText(
                                    "Please enter a group name."
                            );

                            alert.showAndWait();

                            return null;
                        }


                        List<User>
                                selectedMembers =
                                new ArrayList<>();


                        for (CheckBox checkBox :
                                memberCheckBoxes) {

                            if (checkBox.isSelected()) {

                                User user =
                                        (User)
                                                checkBox
                                                        .getUserData();

                                selectedMembers.add(
                                        user
                                );
                            }
                        }


                        createGroupWithMembers(
                                groupName,
                                selectedMembers
                        );
                    }

                    return button;
                }
        );


        dialog.showAndWait();
    }


    // =========================================================
    // CREATE GROUP AND ADD MEMBERS
    // =========================================================

    private void createGroupWithMembers(
            String groupName,
            List<User> selectedMembers
    ) {

        if (currentUser == null) {

            return;
        }


        try {

            Group group =
                    groupDAO.createGroup(
                            groupName,
                            currentUser.getId()
                    );


            if (group == null) {

                Alert alert =
                        new Alert(
                                Alert.AlertType.ERROR
                        );

                alert.setTitle(
                        "Create Group Failed"
                );

                alert.setHeaderText(
                        null
                );

                alert.setContentText(
                        "Could not create the group."
                );

                alert.showAndWait();

                return;
            }


            int addedMembers = 0;


            for (User user :
                    selectedMembers) {

                boolean added =
                        groupDAO.addMember(
                                group.getId(),
                                user.getId()
                        );

                if (added) {

                    addedMembers++;
                }
            }


            loadGroups();


            socketClient.notifyGroupCreated(
                    group.getId()
            );


            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setTitle(
                    "Group Created"
            );

            alert.setHeaderText(
                    "Group created successfully!"
            );

            alert.setContentText(
                    "Group: "
                            + groupName
                            + "\n\n"
                            + "Members added: "
                            + addedMembers
                            + "\n"
                            + "Creator: "
                            + currentUser.getUsername()
            );

            alert.showAndWait();


        } catch (Exception e) {

            e.printStackTrace();

            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alert.setTitle(
                    "Error"
            );

            alert.setHeaderText(
                    "Could not create group"
            );

            alert.setContentText(
                    e.getMessage()
            );

            alert.showAndWait();
        }
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @FXML
    private void handleLogout() {

        socketClient.disconnect();


        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/login.fxml"
                            )
                    );


            Parent root =
                    loader.load();


            Scene scene =
                    new Scene(root);


            scene.getStylesheets().add(
                    getClass()
                            .getResource(
                                    "/css/login.css"
                            )
                            .toExternalForm()
            );


            Stage stage =
                    (Stage)
                            messageField
                                    .getScene()
                                    .getWindow();


            stage.setTitle(
                    "Chat Application - Login"
            );


            stage.setScene(scene);


            stage.setWidth(
                    400
            );


            stage.setHeight(
                    500
            );


            stage.setResizable(
                    false
            );


            stage.show();


            System.out.println(
                    "User logged out: "
                            + username
            );


        } catch (Exception e) {

            System.out.println(
                    "Unable to return to login page."
            );

            e.printStackTrace();
        }
    }
}