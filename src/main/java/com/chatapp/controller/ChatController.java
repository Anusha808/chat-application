package com.chatapp.controller;

import com.chatapp.client.ChatSocketClient;
import com.chatapp.dao.GroupDAO;
import com.chatapp.dao.GroupMessageDAO;
import com.chatapp.dao.MessageDAO;
import com.chatapp.dao.UserDAO;
import com.chatapp.database.DatabaseConnection;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.sql.Connection;

import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class ChatController {


    // =========================================================
    // FXML COMPONENTS
    // =========================================================

    @FXML
    private Label welcomeLabel;

    @FXML
    private TextField searchField;

    @FXML
    private Button clearSearchButton;

    @FXML
    private ListView<String> userListView;

    @FXML
    private ListView<String> groupListView;

    @FXML
    private TextField groupSearchField;

    @FXML
    private Button clearGroupSearchButton;

    @FXML
    private ListView<String> messageListView;

    @FXML
    private Label chatTitleLabel;

    @FXML
    private TextField messageField;


    // =========================================================
    // FILE / IMAGE ATTACHMENT
    // =========================================================

    @FXML
    private Button attachmentButton;

    @FXML
    private Button membersButton;


    // =========================================================
    // EMOJI SUPPORT
    // =========================================================

    @FXML
    private Button emojiButton;

    private Popup emojiPopup;


    // =========================================================
    // MESSAGE SEARCH
    // =========================================================

    @FXML
    private TextField messageSearchField;

    @FXML
    private Button clearMessageSearchButton;


    // =========================================================
    // USER INFORMATION
    // =========================================================

    private String username;

    private User currentUser;

    private User selectedUser;

    private Group selectedGroup;


    // =========================================================
    // CURRENT PRIVATE MESSAGES
    // =========================================================

    private List<Message> currentPrivateMessages =
            new ArrayList<>();


    // =========================================================
    // DISPLAYED PRIVATE MESSAGES
    // =========================================================

    private List<Message> displayedPrivateMessages =
            new ArrayList<>();


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
        // USER SEARCH
        // =====================================================

        if (searchField != null) {

            searchField.textProperty()
                    .addListener(
                            (observable,
                             oldValue,
                             newValue) -> {

                                loadUsers();
                            }
                    );
        }


        // =====================================================
        // MESSAGE SEARCH
        // =====================================================

        if (messageSearchField != null) {

            messageSearchField.textProperty()
                    .addListener(
                            (observable,
                             oldValue,
                             newValue) -> {

                                filterMessages();
                            }
                    );
        }


        // =====================================================
        // GROUP SEARCH
        // =====================================================

        if (groupSearchField != null) {

            groupSearchField.textProperty()
                    .addListener(
                            (observable,
                             oldValue,
                             newValue) -> {

                                loadGroups();
                            }
                    );
        }


        // =====================================================
        // PRIVATE USER SELECTION
        // =====================================================

        userListView.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) -> {

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
                        (observable,
                         oldValue,
                         newValue) -> {

                            if (newValue != null) {

                                openGroupChat(
                                        newValue
                                );
                            }
                        }
                );
    }


    // =========================================================
    // EMOJI SUPPORT
    // =========================================================

    @FXML
    private void handleEmoji() {

        if (emojiPopup != null
                && emojiPopup.isShowing()) {

            emojiPopup.hide();

            return;
        }


        emojiPopup = new Popup();

        emojiPopup.setAutoHide(true);

        emojiPopup.setAutoFix(true);


        VBox emojiContainer =
                new VBox(8);

        emojiContainer.setPadding(
                new Insets(10)
        );

        emojiContainer.setAlignment(
                Pos.CENTER
        );

        emojiContainer.setStyle(
                "-fx-background-color: white;"
                        + "-fx-border-color: #d1d5db;"
                        + "-fx-border-width: 1;"
                        + "-fx-background-radius: 10;"
                        + "-fx-border-radius: 10;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 10, 0, 0, 3);"
        );


        String[] emojis = {

                "😀", "😃", "😄", "😁",
                "😆", "😅", "😂", "🤣",

                "😊", "😇", "🙂", "🙃",
                "😉", "😌", "😍", "🥰",

                "😘", "😗", "😙", "😚",
                "😋", "😛", "😝", "😜",

                "🤪", "🤨", "🧐", "🤓",
                "😎", "🤩", "🥳", "😏",

                "😒", "😞", "😔", "😟",
                "😕", "🙁", "☹️", "😣",

                "😖", "😫", "😩", "🥺",
                "😢", "😭", "😤", "😠",

                "😡", "🤬", "🤯", "😳",
                "🥵", "🥶", "😱", "😨",

                "😰", "😥", "😓", "🤗",
                "🤔", "🫣", "🤭", "🤫",

                "🤥", "😶", "😐", "😑",
                "😬", "🙄", "😯", "😦",

                "😧", "😮", "😲", "🥱",
                "😴", "🤤", "😪", "😵",

                "🤐", "🥴", "🤢", "🤮",

                "👍", "👎", "👌", "✌️",
                "🤞", "🤟", "🤘", "🤙",

                "👈", "👉", "👆", "👇",
                "☝️", "✋", "🤚", "🖐️",

                "🖖", "👏", "🙌", "👐",
                "🤲", "🙏", "🤝", "💪",

                "👋", "💅",

                "❤️", "🧡", "💛", "💚",
                "💙", "💜", "🖤", "🤍",

                "🤎", "💔", "💕", "💞",
                "💓", "💗", "💖", "💘",

                "💝", "💟",

                "🔥", "✨", "⭐", "🌟",
                "💫", "💥", "💯", "🎉",

                "🎊", "🎁", "🎈", "🥳",

                "🌸", "🌹", "🌺", "🌻",
                "🌼", "🌷", "🌱", "🍀",

                "🌈", "☀️", "🌙", "⭐",

                "🍎", "🍊", "🍋", "🍌",
                "🍉", "🍇", "🍓", "🍒",

                "🍑", "🍍", "🥝", "🍕",
                "🍔", "🍟", "🍿", "🍩",

                "🍪", "🍫", "🎂", "🍰",
                "☕", "🍵",

                "⚽", "🏀", "🏈", "⚾",
                "🎾", "🏆", "🎮", "🎯",

                "🎵", "🎶", "🎸", "🎧",

                "💻", "🖥️", "⌨️", "🖱️",
                "📱", "📷", "📚", "📖",

                "💡", "🔔", "📌", "✏️"
        };


        HBox currentRow =
                new HBox(3);

        currentRow.setAlignment(
                Pos.CENTER
        );


        int count = 0;


        for (String emoji : emojis) {

            Button emojiChoice =
                    new Button(
                            emoji
                    );


            emojiChoice.setPrefWidth(
                    40
            );

            emojiChoice.setPrefHeight(
                    40
            );


            emojiChoice.setStyle(
                    "-fx-background-color: transparent;"
                            + "-fx-font-size: 24px;"
                            + "-fx-font-family: 'Segoe UI Emoji';"
                            + "-fx-background-radius: 8px;"
                            + "-fx-cursor: hand;"
            );


            emojiChoice.setOnMouseEntered(
                    event ->
                            emojiChoice.setStyle(
                                    "-fx-background-color: #eef2ff;"
                                            + "-fx-font-size: 24px;"
                                            + "-fx-font-family: 'Segoe UI Emoji';"
                                            + "-fx-background-radius: 8px;"
                                            + "-fx-cursor: hand;"
                            )
            );


            emojiChoice.setOnMouseExited(
                    event ->
                            emojiChoice.setStyle(
                                    "-fx-background-color: transparent;"
                                            + "-fx-font-size: 24px;"
                                            + "-fx-font-family: 'Segoe UI Emoji';"
                                            + "-fx-background-radius: 8px;"
                                            + "-fx-cursor: hand;"
                            )
            );


            emojiChoice.setOnAction(
                    event -> {

                        insertEmoji(
                                emoji
                        );

                        emojiPopup.hide();
                    }
            );


            currentRow
                    .getChildren()
                    .add(
                            emojiChoice
                    );


            count++;


            if (count == 8) {

                emojiContainer
                        .getChildren()
                        .add(
                                currentRow
                        );


                currentRow =
                        new HBox(3);

                currentRow.setAlignment(
                        Pos.CENTER
                );

                count = 0;
            }
        }


        if (!currentRow
                .getChildren()
                .isEmpty()) {

            emojiContainer
                    .getChildren()
                    .add(
                            currentRow
                    );
        }


        emojiPopup
                .getContent()
                .add(
                        emojiContainer
                );


        if (emojiButton != null
                && emojiButton.getScene() != null) {

            javafx.geometry.Point2D point =
                    emojiButton.localToScreen(
                            0,
                            0
                    );


            if (point != null) {

                double popupX =
                        point.getX();

                double popupY =
                        point.getY()
                                - 430;


                if (popupY < 10) {

                    popupY = 10;
                }


                emojiPopup.show(
                        emojiButton,
                        popupX,
                        popupY
                );
            }
        }
    }


    // =========================================================
    // INSERT EMOJI
    // =========================================================

    private void insertEmoji(
            String emoji
    ) {

        if (messageField == null) {

            return;
        }


        int caretPosition =
                messageField
                        .getCaretPosition();


        String currentText =
                messageField
                        .getText();


        if (currentText == null) {

            currentText = "";
        }


        String newText =
                currentText.substring(
                        0,
                        caretPosition
                )
                        + emoji
                        + currentText.substring(
                        caretPosition
                );


        messageField.setText(
                newText
        );


        messageField.positionCaret(
                caretPosition
                        + emoji.length()
        );


        messageField.requestFocus();
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
                                    setContextMenu(null);

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
                                    setContextMenu(null);

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


                                Message currentMessage =
                                        null;


                                if (selectedGroup == null
                                        && getIndex() >= 0
                                        && getIndex()
                                        < displayedPrivateMessages
                                        .size()) {

                                    currentMessage =
                                            displayedPrivateMessages
                                                    .get(
                                                            getIndex()
                                                    );
                                }


                                boolean hasAttachment =
                                        currentMessage != null
                                                && currentMessage
                                                .getFileName() != null
                                                && !currentMessage
                                                .getFileName()
                                                .isBlank();


                                VBox bubble =
                                        new VBox(5);


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


                                if (hasAttachment) {

                                    String filePath =
                                            currentMessage
                                                    .getFilePath();

                                    String fileType =
                                            currentMessage
                                                    .getFileType();


                                    if (fileType != null
                                            && fileType.startsWith(
                                            "image/"
                                    )) {

                                        try {

                                            File imageFile =
                                                    new File(
                                                            filePath
                                                    );


                                            if (imageFile.exists()) {

                                                Image image =
                                                        new Image(
                                                                imageFile
                                                                        .toURI()
                                                                        .toString(),
                                                                280,
                                                                220,
                                                                true,
                                                                true
                                                        );


                                                ImageView imageView =
                                                        new ImageView(
                                                                image
                                                        );


                                                imageView.setFitWidth(
                                                        280
                                                );

                                                imageView.setFitHeight(
                                                        220
                                                );

                                                imageView.setPreserveRatio(
                                                        true
                                                );

                                                imageView.setSmooth(
                                                        true
                                                );


                                                bubble
                                                        .getChildren()
                                                        .add(
                                                                imageView
                                                        );


                                                Label fileNameLabel =
                                                        new Label(
                                                                "🖼 "
                                                                        + currentMessage
                                                                        .getFileName()
                                                        );


                                                fileNameLabel.setStyle(
                                                        "-fx-font-size: 11px;"
                                                                + "-fx-text-fill: #64748b;"
                                                );


                                                bubble
                                                        .getChildren()
                                                        .add(
                                                                fileNameLabel
                                                        );

                                            } else {

                                                Label fileLabel =
                                                        new Label(
                                                                "🖼 "
                                                                        + currentMessage
                                                                        .getFileName()
                                                                        + " (file unavailable)"
                                                        );

                                                bubble
                                                        .getChildren()
                                                        .add(
                                                                fileLabel
                                                        );
                                            }

                                        } catch (Exception e) {

                                            Label fileLabel =
                                                    new Label(
                                                            "🖼 "
                                                                    + currentMessage
                                                                    .getFileName()
                                                    );

                                            bubble
                                                    .getChildren()
                                                    .add(
                                                            fileLabel
                                                    );
                                        }

                                    } else {

                                        Label fileLabel =
                                                new Label(
                                                        "📎 "
                                                                + currentMessage
                                                                .getFileName()
                                                );


                                        fileLabel.setStyle(
                                                "-fx-font-size: 14px;"
                                                        + "-fx-font-weight: bold;"
                                        );


                                        bubble
                                                .getChildren()
                                                .add(
                                                        fileLabel
                                                );
                                    }

                                } else {

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


                                    bubble
                                            .getChildren()
                                            .add(
                                                    messageLabel
                                            );
                                }


                                Label timeLabel =
                                        new Label(
                                                time
                                        );


                                timeLabel
                                        .getStyleClass()
                                        .add(
                                                "message-time"
                                        );


                                bubble
                                        .getChildren()
                                        .add(
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


                                if (isSentByMe
                                        && selectedGroup == null
                                        && currentUser != null
                                        && currentMessage != null) {

                                    ContextMenu contextMenu =
                                            new ContextMenu();


                                    final Message messageForContext =
                                            currentMessage;


                                    if (!hasAttachment) {

                                        MenuItem editItem =
                                                new MenuItem(
                                                        "✏ Edit Message"
                                                );


                                        editItem.setOnAction(
                                                event ->
                                                        handleEditMessage(
                                                                messageForContext
                                                        )
                                        );


                                        contextMenu
                                                .getItems()
                                                .add(
                                                        editItem
                                                );
                                    }


                                    MenuItem deleteItem =
                                            new MenuItem(
                                                    "🗑 Delete Message"
                                            );


                                    deleteItem.setOnAction(
                                            event ->
                                                    handleDeleteMessage(
                                                            messageForContext
                                                    )
                                    );


                                    contextMenu
                                            .getItems()
                                            .add(
                                                    deleteItem
                                            );


                                    setContextMenu(
                                            contextMenu
                                    );

                                } else {

                                    setContextMenu(
                                            null
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
    // EDIT MESSAGE
    // =========================================================

    private void handleEditMessage(
            Message message
    ) {

        if (message == null
                || currentUser == null) {

            return;
        }


        if (message.getSenderId()
                != currentUser.getId()) {

            Alert alert =
                    new Alert(
                            Alert.AlertType.WARNING
                    );

            alert.setTitle(
                    "Edit Message"
            );

            alert.setHeaderText(
                    null
            );

            alert.setContentText(
                    "You can edit only your own messages."
            );

            alert.showAndWait();

            return;
        }


        TextInputDialog dialog =
                new TextInputDialog(
                        message.getMessage()
                );


        dialog.setTitle(
                "Edit Message"
        );

        dialog.setHeaderText(
                "Edit your message"
        );

        dialog.setContentText(
                "Message:"
        );


        Optional<String> result =
                dialog.showAndWait();


        if (result.isEmpty()) {

            return;
        }


        String newMessage =
                result.get().trim();


        if (newMessage.isEmpty()) {

            Alert alert =
                    new Alert(
                            Alert.AlertType.WARNING
                    );

            alert.setTitle(
                    "Invalid Message"
            );

            alert.setHeaderText(
                    null
            );

            alert.setContentText(
                    "Message cannot be empty."
            );

            alert.showAndWait();

            return;
        }


        if (newMessage.equals(
                message.getMessage()
        )) {

            return;
        }


        boolean updated =
                messageDAO.updateMessage(
                        message.getId(),
                        currentUser.getId(),
                        newMessage
                );


        if (updated) {

            loadChatHistory();

        } else {

            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alert.setTitle(
                    "Edit Message"
            );

            alert.setHeaderText(
                    "Could not edit message"
            );

            alert.setContentText(
                    "The message could not be updated."
            );

            alert.showAndWait();
        }
    }


    // =========================================================
    // DELETE MESSAGE
    // =========================================================

    private void handleDeleteMessage(
            Message message
    ) {

        if (message == null
                || currentUser == null) {

            return;
        }


        if (message.getSenderId()
                != currentUser.getId()) {

            Alert alert =
                    new Alert(
                            Alert.AlertType.WARNING
                    );

            alert.setTitle(
                    "Delete Message"
            );

            alert.setHeaderText(
                    null
            );

            alert.setContentText(
                    "You can delete only your own messages."
            );

            alert.showAndWait();

            return;
        }


        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        confirmation.setTitle(
                "Delete Message"
        );

        confirmation.setHeaderText(
                "Delete this message?"
        );

        confirmation.setContentText(
                "This message will be permanently deleted."
        );


        Optional<ButtonType> result =
                confirmation.showAndWait();


        if (result.isEmpty()
                || result.get() != ButtonType.OK) {

            return;
        }


        boolean deleted =
                messageDAO.deleteMessage(
                        message.getId(),
                        currentUser.getId()
                );


        if (deleted) {

            loadChatHistory();

        } else {

            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alert.setTitle(
                    "Delete Message"
            );

            alert.setHeaderText(
                    "Could not delete message"
            );

            alert.setContentText(
                    "The message could not be deleted."
            );

            alert.showAndWait();
        }
    }


    // =========================================================
    // FILE / IMAGE ATTACHMENT
    // =========================================================

    @FXML
    private void handleAttachment() {

        if (currentUser == null) {

            showAttachmentAlert(
                    "Please login first."
            );

            return;
        }


        if (selectedUser == null) {

            showAttachmentAlert(
                    "Please select a user first."
            );

            return;
        }


        if (!socketClient.isConnected()) {

            showAttachmentAlert(
                    "Chat server is not connected."
            );

            return;
        }


        FileChooser fileChooser =
                new FileChooser();


        fileChooser.setTitle(
                "Select File or Image"
        );


        FileChooser.ExtensionFilter images =
                new FileChooser.ExtensionFilter(
                        "Images",
                        "*.png",
                        "*.jpg",
                        "*.jpeg",
                        "*.gif",
                        "*.bmp",
                        "*.webp"
                );


        FileChooser.ExtensionFilter documents =
                new FileChooser.ExtensionFilter(
                        "Documents",
                        "*.pdf",
                        "*.doc",
                        "*.docx",
                        "*.txt",
                        "*.xlsx",
                        "*.xls",
                        "*.ppt",
                        "*.pptx"
                );


        FileChooser.ExtensionFilter allFiles =
                new FileChooser.ExtensionFilter(
                        "All Files",
                        "*.*"
                );


        fileChooser.getExtensionFilters()
                .addAll(
                        images,
                        documents,
                        allFiles
                );


        File selectedFile =
                fileChooser.showOpenDialog(
                        messageField
                                .getScene()
                                .getWindow()
                );


        if (selectedFile == null) {

            return;
        }


        try {

            if (!selectedFile.exists()
                    || !selectedFile.isFile()) {

                showAttachmentAlert(
                        "Selected file could not be found."
                );

                return;
            }


            String originalFileName =
                    selectedFile.getName();


            String fileType =
                    Files.probeContentType(
                            selectedFile.toPath()
                    );


            if (fileType == null) {

                fileType =
                        "application/octet-stream";
            }


            Path uploadDirectory =
                    Paths.get(
                            "uploads"
                    );


            if (!Files.exists(
                    uploadDirectory
            )) {

                Files.createDirectories(
                        uploadDirectory
                );
            }


            String uniqueFileName =
                    System.currentTimeMillis()
                            + "_"
                            + originalFileName;


            Path destination =
                    uploadDirectory.resolve(
                            uniqueFileName
                    );


            Files.copy(
                    selectedFile.toPath(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );


            String absoluteFilePath =
                    destination
                            .toAbsolutePath()
                            .normalize()
                            .toString();


            Message message =
                    new Message(
                            currentUser.getId(),
                            selectedUser.getId(),
                            "",
                            originalFileName,
                            absoluteFilePath,
                            fileType
                    );


            boolean saved =
                    messageDAO.saveMessage(
                            message
                    );


            if (!saved) {

                showAttachmentAlert(
                        "Could not save the attachment."
                );

                return;
            }


            socketClient.sendFileMessage(
                    selectedUser.getUsername(),
                    originalFileName,
                    fileType,
                    absoluteFilePath
            );


            loadChatHistory();

            messageField.requestFocus();


        } catch (IOException e) {

            e.printStackTrace();

            showAttachmentAlert(
                    "Unable to copy or send the selected file."
            );
        }
    }


    // =========================================================
    // ATTACHMENT ALERT
    // =========================================================

    private void showAttachmentAlert(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(
                "Attachment"
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
    // SET CURRENT USER
    // =========================================================

    public void setCurrentUser(
            User user
    ) {

        if (user == null) {

            System.out.println(
                    "Current user cannot be null."
            );

            return;
        }


        this.currentUser =
                user;


        this.username =
                user.getUsername();


        if (welcomeLabel != null) {

            welcomeLabel.setText(
                    "Welcome, "
                            + user.getUsername()
                            + "!"
            );
        }


        loadUsers();

        loadGroups();

        connectToSocketServer();
    }


    // =========================================================
    // SET USERNAME
    // =========================================================

    public void setUsername(
            String username
    ) {

        this.username =
                username;


        welcomeLabel.setText(
                "Welcome, "
                        + username
                        + "!"
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

        if (userListView == null) {

            return;
        }


        List<User> users =
                userDAO.getAllUsers();


        String searchText = "";


        if (searchField != null
                && searchField.getText() != null) {

            searchText =
                    searchField.getText()
                            .trim()
                            .toLowerCase();
        }


        final String finalSearchText =
                searchText;


        List<String> usernames =
                users.stream()

                        .filter(
                                user ->
                                        user.getUsername() != null
                                                && !user.getUsername()
                                                .equals(
                                                        username
                                                )
                        )

                        .filter(
                                user -> {

                                    if (finalSearchText.isEmpty()) {

                                        return true;
                                    }


                                    return user.getUsername()
                                            .toLowerCase()
                                            .contains(
                                                    finalSearchText
                                            );
                                }
                        )

                        .map(user -> {

                            String name =
                                    user.getUsername();


                            if (onlineUsers.contains(
                                    name
                            )) {

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
    // CLEAR USER SEARCH
    // =========================================================

    @FXML
    private void handleClearSearch() {

        if (searchField != null) {

            searchField.clear();
        }


        userListView
                .getSelectionModel()
                .clearSelection();


        loadUsers();
    }


    // =========================================================
    // FILTER MESSAGES
    // =========================================================

    private void filterMessages() {

        if (selectedUser == null
                || currentUser == null
                || messageListView == null) {

            return;
        }


        String searchText = "";


        if (messageSearchField != null
                && messageSearchField.getText() != null) {

            searchText =
                    messageSearchField.getText()
                            .trim()
                            .toLowerCase();
        }


        final String finalSearchText =
                searchText;


        List<Message> filteredMessages =
                currentPrivateMessages.stream()
                        .filter(message -> {

                            if (finalSearchText.isEmpty()) {

                                return true;
                            }


                            String text =
                                    message.getMessage();

                            String fileName =
                                    message.getFileName();


                            boolean textMatches =
                                    text != null
                                            && text
                                            .toLowerCase()
                                            .contains(
                                                    finalSearchText
                                            );


                            boolean fileMatches =
                                    fileName != null
                                            && fileName
                                            .toLowerCase()
                                            .contains(
                                                    finalSearchText
                                            );


                            return textMatches
                                    || fileMatches;
                        })
                        .toList();


        displayedPrivateMessages =
                new ArrayList<>(
                        filteredMessages
                );


        List<String> chatMessages =
                filteredMessages.stream()
                        .map(message -> {

                            String sender;


                            if (message.getSenderId()
                                    == currentUser.getId()) {

                                sender =
                                        "You";

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


                            String displayMessage;


                            if (message.getFileName() != null
                                    && !message.getFileName()
                                    .isBlank()) {

                                displayMessage =
                                        "📎 "
                                                + message
                                                .getFileName();

                            } else {

                                displayMessage =
                                        message.getMessage();
                            }


                            return sender
                                    + "|"
                                    + displayMessage
                                    + "|"
                                    + time;
                        })
                        .toList();


        messageListView.setItems(
                FXCollections.observableArrayList(
                        chatMessages
                )
        );


        messageListView.refresh();


        if (!chatMessages.isEmpty()) {

            messageListView.scrollTo(
                    chatMessages.size() - 1
            );
        }
    }


    // =========================================================
    // CLEAR MESSAGE SEARCH
    // =========================================================

    @FXML
    private void handleClearMessageSearch() {

        if (messageSearchField != null) {

            messageSearchField.clear();
        }


        filterMessages();
    }


    // =========================================================
    // LOAD GROUPS
    // =========================================================

    @FXML
    private void handleClearGroupSearch() {

        if (groupSearchField != null) {

            groupSearchField.clear();
        }


        groupListView
                .getSelectionModel()
                .clearSelection();

        loadGroups();
    }


    private void loadGroups() {

        if (currentUser == null) {

            return;
        }


        List<Group> groups =
                groupDAO.getGroupsForUser(
                        currentUser.getId()
                );


        String searchText = "";

        if (groupSearchField != null
                && groupSearchField.getText() != null) {

            searchText =
                    groupSearchField.getText()
                            .trim()
                            .toLowerCase();
        }


        final String finalSearchText =
                searchText;


        List<String> groupNames =
                groups.stream()
                        .map(Group::getGroupName)
                        .filter(groupName -> {

                            if (groupName == null) {

                                return false;
                            }

                            if (finalSearchText.isEmpty()) {

                                return true;
                            }

                            return groupName
                                    .toLowerCase()
                                    .contains(
                                            finalSearchText
                                    );
                        })
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

        if (serverMessage.startsWith(
                "ONLINE_USERS|"
        )) {

            updateOnlineUsers(
                    serverMessage
            );

            return;
        }


        if (serverMessage.startsWith(
                "FILE_MESSAGE|"
        )) {

            handleIncomingFileMessage(
                    serverMessage
            );

            return;
        }


        if (serverMessage.startsWith(
                "GROUP_CREATED|"
        )) {

            Platform.runLater(() -> {

                loadGroups();
            });

            return;
        }


        if (serverMessage.startsWith(
                "GROUP_MEMBER_ADDED|"
        )) {

            Platform.runLater(() -> {

                loadGroups();
            });

            return;
        }


        if (serverMessage.startsWith(
                "GROUP_MEMBER_REMOVED|"
        )) {

            Platform.runLater(() -> {

                loadGroups();
            });

            return;
        }


        if (serverMessage.startsWith(
                "GROUP_MEMBER_LEFT|"
        )) {

            Platform.runLater(() -> {

                loadGroups();
            });

            return;
        }


        if (serverMessage.startsWith(
                "GROUP_DELETED|"
        )) {

            Platform.runLater(() -> {

                loadGroups();
            });

            return;
        }


        if (serverMessage.startsWith(
                "GROUP_RENAMED|"
        )) {

            Platform.runLater(() -> {

                loadGroups();
            });

            return;
        }


        if (serverMessage.startsWith(
                "GROUP_MESSAGE|"
        )) {

            handleIncomingGroupMessage(
                    serverMessage
            );

            return;
        }


        if (serverMessage.equals(
                "GROUP_MESSAGE_SENT"
        )) {

            return;
        }


        if (serverMessage.equals(
                "GROUP_MEMBER_ADDED_SUCCESS"
        )) {

            return;
        }


        if (serverMessage.equals(
                "MESSAGE_SENT"
        )) {

            return;
        }


        if (serverMessage.equals(
                "FILE_SENT"
        )) {

            return;
        }


        if (serverMessage.startsWith(
                "USER_OFFLINE|"
        )) {

            System.out.println(
                    serverMessage
            );

            return;
        }


        if (serverMessage.startsWith(
                "ERROR|"
        )) {

            System.out.println(
                    serverMessage
            );

            return;
        }


        handleIncomingMessage(
                serverMessage
        );
    }


    // =========================================================
    // HANDLE INCOMING FILE MESSAGE
    // =========================================================

    private void handleIncomingFileMessage(
            String serverMessage
    ) {

        try {

            String[] parts =
                    serverMessage.split(
                            "\\|",
                            5
                    );


            if (parts.length < 5) {

                System.out.println(
                        "Invalid incoming file message."
                );

                return;
            }


            final String senderUsername =
                    parts[1].trim();


            final String fileName =
                    parts[2].trim();


            final String fileType =
                    parts[3].trim();


            final String filePath =
                    parts[4].trim();


            File file =
                    new File(
                            filePath
                    );


            if (!file.exists()) {

                System.out.println(
                        "Received file path does not exist: "
                                + filePath
                );

                return;
            }


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
                            "Received file from "
                                    + senderUsername
                                    + ": "
                                    + fileName
                    );
                }
            });


            System.out.println(
                    "================================="
            );

            System.out.println(
                    "FILE MESSAGE RECEIVED"
            );

            System.out.println(
                    "From: "
                            + senderUsername
            );

            System.out.println(
                    "File: "
                            + fileName
            );

            System.out.println(
                    "Type: "
                            + fileType
            );

            System.out.println(
                    "Path: "
                            + filePath
            );

            System.out.println(
                    "================================="
            );


        } catch (Exception e) {

            e.printStackTrace();

            System.out.println(
                    "Error handling incoming file: "
                            + e.getMessage()
            );
        }
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


        if (messageSearchField != null) {

            messageSearchField.clear();
        }


        membersButton.setVisible(
                false
        );

        membersButton.setManaged(
                false
        );


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


        groupListView
                .getSelectionModel()
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


        if (messageSearchField != null) {

            messageSearchField.clear();
        }


        membersButton.setVisible(
                true
        );

        membersButton.setManaged(
                true
        );


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


        userListView
                .getSelectionModel()
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


        currentPrivateMessages =
                new ArrayList<>(
                        messages
                );


        filterMessages();
    }


    // =========================================================
    // LOAD GROUP CHAT HISTORY
    // =========================================================

    private void loadGroupChatHistory() {

        if (currentUser == null
                || selectedGroup == null) {

            return;
        }


        currentPrivateMessages.clear();

        displayedPrivateMessages.clear();


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

                                sender =
                                        "You";

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


        messageListView.refresh();


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


        final int finalGroupId =
                groupId;


        String senderUsername =
                parts[2];


        String messageText =
                parts[3];


        Platform.runLater(() -> {

            boolean isCurrentGroup =
                    selectedGroup != null
                            && selectedGroup.getId()
                            == finalGroupId;


            if (isCurrentGroup) {

                loadGroupChatHistory();


                groupMessageDAO.markMessagesAsRead(
                        finalGroupId,
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


        for (User user :
                users) {

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

            Alert alert =
                    new Alert(
                            Alert.AlertType.WARNING
                    );


            alert.setTitle(
                    "No Group Selected"
            );


            alert.setHeaderText(
                    null
            );


            alert.setContentText(
                    "Please select a group first."
            );


            alert.showAndWait();


            return;
        }


        try {

            Connection connection =
                    DatabaseConnection.getConnection();


            GroupMembersController controller =
                    new GroupMembersController(
                            selectedGroup.getId(),
                            selectedGroup.getGroupName(),
                            selectedGroup.getCreatedBy(),
                            currentUser.getId(),
                            connection,
                            socketClient
                    );


            controller.show();


        } catch (Exception e) {

            e.printStackTrace();


            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );


            alert.setTitle(
                    "Group Members"
            );


            alert.setHeaderText(
                    "Could not open group members"
            );


            alert.setContentText(
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Unknown error."
            );


            alert.showAndWait();
        }
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


        Set<Integer> existingMemberIds =
                new HashSet<>();


        for (User member :
                currentMembers) {

            existingMemberIds.add(
                    member.getId()
            );
        }


        List<User> allUsers =
                userDAO.getAllUsers();


        List<User> availableUsers =
                new ArrayList<>();


        for (User user :
                allUsers) {

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


        if (availableUsers.isEmpty()) {

            Label noUsersLabel =
                    new Label(
                            "All users are already members."
                    );


            noUsersLabel.setStyle(
                    "-fx-text-fill: #64748b;"
            );


            usersBox
                    .getChildren()
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


                usersBox
                        .getChildren()
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


        content
                .getChildren()
                .addAll(
                        titleLabel,
                        scrollPane
                );


        dialog.getDialogPane()
                .setContent(
                        content
                );


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


                socketClient.notifyGroupMemberAdded(
                        selectedGroup.getId(),
                        user.getId()
                );
            }
        }


        loadGroups();


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


        dialogPane
                .getButtonTypes()
                .addAll(
                        createButton,
                        cancelButton
                );


        Label groupNameLabel =
                new Label(
                        "Group Name"
                );


        TextField groupNameField =
                new TextField();


        groupNameField.setPromptText(
                "Enter group name"
        );


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


        for (User user :
                users) {

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


        content
                .getChildren()
                .addAll(
                        groupNameLabel,
                        groupNameField,
                        membersLabel,
                        scrollPane
                );


        dialogPane.setContent(
                content
        );


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


                        List<User> selectedMembers =
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
    // OPEN PROFILE
    // =========================================================

    @FXML
    private void handleProfile() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/profile.fxml"
                            )
                    );


            Parent root =
                    loader.load();


            ProfileController profileController =
                    loader.getController();


            profileController.setUser(
                    currentUser
            );


            Scene scene =
                    new Scene(
                            root
                    );


            if (getClass()
                    .getResource(
                            "/css/profile.css"
                    ) != null) {

                scene.getStylesheets().add(
                        getClass()
                                .getResource(
                                        "/css/profile.css"
                                )
                                .toExternalForm()
                );
            }


            Stage stage =
                    (Stage)
                            welcomeLabel
                                    .getScene()
                                    .getWindow();


            stage.setTitle(
                    "Chat Application - My Profile"
            );


            stage.setScene(
                    scene
            );


            stage.show();


        } catch (Exception e) {

            System.out.println(
                    "Unable to open profile page."
            );


            e.printStackTrace();
        }
    }


    // =========================================================
    // OPEN SETTINGS
    // =========================================================

    @FXML
    private void handleSettings() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/fxml/settings.fxml"
                            )
                    );


            Parent root =
                    loader.load();


            Scene scene =
                    new Scene(
                            root
                    );


            if (getClass()
                    .getResource(
                            "/css/settings.css"
                    ) != null) {

                scene.getStylesheets().add(
                        getClass()
                                .getResource(
                                        "/css/settings.css"
                                )
                                .toExternalForm()
                );
            }


            Stage stage =
                    (Stage)
                            welcomeLabel
                                    .getScene()
                                    .getWindow();


            stage.setTitle(
                    "Chat Application - Settings"
            );


            stage.setScene(
                    scene
            );


            stage.show();


        } catch (Exception e) {

            System.out.println(
                    "Unable to open settings page."
            );


            e.printStackTrace();


            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );


            alert.setTitle(
                    "Settings Error"
            );


            alert.setHeaderText(
                    "Unable to open Settings"
            );


            alert.setContentText(
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Unknown error."
            );


            alert.showAndWait();
        }
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @FXML
    private void handleLogout() {

        if (emojiPopup != null
                && emojiPopup.isShowing()) {

            emojiPopup.hide();
        }


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
                    new Scene(
                            root
                    );


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


            stage.setScene(
                    scene
            );


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