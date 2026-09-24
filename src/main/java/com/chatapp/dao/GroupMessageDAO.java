package com.chatapp.dao;

import com.chatapp.database.DatabaseConnection;
import com.chatapp.model.GroupMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GroupMessageDAO {

    // Save a group message
    public boolean saveMessage(GroupMessage message) {

        String sql = """
                INSERT INTO group_messages
                (group_id, sender_id, message, is_read)
                VALUES (?, ?, ?, FALSE)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    message.getGroupId()
            );

            statement.setInt(
                    2,
                    message.getSenderId()
            );

            statement.setString(
                    3,
                    message.getMessage()
            );

            int rowsInserted =
                    statement.executeUpdate();

            return rowsInserted > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Error saving group message: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // Load group chat history
    public List<GroupMessage> getGroupMessages(
            int groupId
    ) {

        List<GroupMessage> messages =
                new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    group_id,
                    sender_id,
                    message,
                    sent_at
                FROM group_messages
                WHERE group_id = ?
                ORDER BY sent_at ASC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    groupId
            );

            ResultSet resultSet =
                    statement.executeQuery();

            while (resultSet.next()) {

                Timestamp timestamp =
                        resultSet.getTimestamp(
                                "sent_at"
                        );

                GroupMessage message =
                        new GroupMessage(
                                resultSet.getInt("id"),
                                resultSet.getInt("group_id"),
                                resultSet.getInt("sender_id"),
                                resultSet.getString("message"),
                                timestamp != null
                                        ? timestamp.toLocalDateTime()
                                        : null
                        );

                messages.add(message);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error loading group messages: "
                            + e.getMessage()
            );
        }

        return messages;
    }

    // Get unread messages in a group
    public int getUnreadCount(
            int groupId,
            int userId
    ) {

        /*
         * A message is unread for a user when:
         *
         * 1. It belongs to the selected group.
         * 2. The sender is not the current user.
         * 3. is_read is FALSE.
         *
         * NOTE:
         * This simple implementation uses the shared
         * is_read column. Later, we can improve this
         * with a separate group_message_reads table
         * so every group member has an independent
         * read status.
         */

        String sql = """
                SELECT COUNT(*)
                FROM group_messages
                WHERE group_id = ?
                AND sender_id <> ?
                AND is_read = FALSE
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    groupId
            );

            statement.setInt(
                    2,
                    userId
            );

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                return resultSet.getInt(1);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error getting group unread count: "
                            + e.getMessage()
            );
        }

        return 0;
    }

    // Mark group messages as read
    public void markMessagesAsRead(
            int groupId,
            int userId
    ) {

        String sql = """
                UPDATE group_messages
                SET is_read = TRUE
                WHERE group_id = ?
                AND sender_id <> ?
                AND is_read = FALSE
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    groupId
            );

            statement.setInt(
                    2,
                    userId
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            System.out.println(
                    "Error marking group messages as read: "
                            + e.getMessage()
            );
        }
    }
}