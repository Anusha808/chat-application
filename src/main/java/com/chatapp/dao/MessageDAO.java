package com.chatapp.dao;

import com.chatapp.database.DatabaseConnection;
import com.chatapp.model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    // Save a new message
    public boolean saveMessage(Message message) {

        String sql = """
                INSERT INTO messages
                (
                    sender_id,
                    receiver_id,
                    message,
                    file_name,
                    file_path,
                    file_type,
                    is_read
                )
                VALUES (?, ?, ?, ?, ?, ?, FALSE)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, message.getSenderId());
            statement.setInt(2, message.getReceiverId());
            statement.setString(3, message.getMessage());

            statement.setString(4, message.getFileName());
            statement.setString(5, message.getFilePath());
            statement.setString(6, message.getFileType());

            int rowsInserted =
                    statement.executeUpdate();

            return rowsInserted > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Error saving message: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // Load chat history
    public List<Message> getChatHistory(
            int userId,
            int otherUserId
    ) {

        List<Message> messages =
                new ArrayList<>();

        String sql = """
                SELECT id,
                       sender_id,
                       receiver_id,
                       message,
                       sent_at,
                       file_name,
                       file_path,
                       file_type
                FROM messages
                WHERE
                    (sender_id = ? AND receiver_id = ?)
                    OR
                    (sender_id = ? AND receiver_id = ?)
                ORDER BY sent_at ASC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, otherUserId);
            statement.setInt(3, otherUserId);
            statement.setInt(4, userId);

            ResultSet resultSet =
                    statement.executeQuery();

            while (resultSet.next()) {

                Timestamp timestamp =
                        resultSet.getTimestamp("sent_at");

                Message message =
                        new Message(
                                resultSet.getInt("id"),
                                resultSet.getInt("sender_id"),
                                resultSet.getInt("receiver_id"),
                                resultSet.getString("message"),
                                timestamp.toLocalDateTime(),
                                resultSet.getString("file_name"),
                                resultSet.getString("file_path"),
                                resultSet.getString("file_type")
                        );

                messages.add(message);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error loading chat history: "
                            + e.getMessage()
            );
        }

        return messages;
    }

    // Get unread message count
    public int getUnreadCount(
            int receiverId,
            int senderId
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM messages
                WHERE receiver_id = ?
                AND sender_id = ?
                AND is_read = FALSE
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, receiverId);
            statement.setInt(2, senderId);

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error getting unread count: "
                            + e.getMessage()
            );
        }

        return 0;
    }

    // Mark messages as read
    public void markMessagesAsRead(
            int receiverId,
            int senderId
    ) {

        String sql = """
                UPDATE messages
                SET is_read = TRUE
                WHERE receiver_id = ?
                AND sender_id = ?
                AND is_read = FALSE
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, receiverId);
            statement.setInt(2, senderId);

            statement.executeUpdate();

        } catch (SQLException e) {

            System.out.println(
                    "Error marking messages as read: "
                            + e.getMessage()
            );
        }
    }

    // Edit an existing message
    // A user can edit only their own message
    public boolean updateMessage(
            int messageId,
            int senderId,
            String newMessage
    ) {

        String sql = """
                UPDATE messages
                SET message = ?
                WHERE id = ?
                AND sender_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, newMessage);
            statement.setInt(2, messageId);
            statement.setInt(3, senderId);

            int rowsUpdated =
                    statement.executeUpdate();

            return rowsUpdated > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Error updating message: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // Delete an existing message
    // A user can delete only their own message
    public boolean deleteMessage(
            int messageId,
            int senderId
    ) {

        String sql = """
                DELETE FROM messages
                WHERE id = ?
                AND sender_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, messageId);
            statement.setInt(2, senderId);

            int rowsDeleted =
                    statement.executeUpdate();

            return rowsDeleted > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Error deleting message: "
                            + e.getMessage()
            );

            return false;
        }
    }
}