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
                (sender_id, receiver_id, message)
                VALUES (?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    message.getSenderId()
            );

            statement.setInt(
                    2,
                    message.getReceiverId()
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
                    "Error saving message: "
                            + e.getMessage()
            );

            return false;
        }
    }

    // Get private chat history
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
                       sent_at
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

                Message message = new Message(
                        resultSet.getInt("id"),
                        resultSet.getInt("sender_id"),
                        resultSet.getInt("receiver_id"),
                        resultSet.getString("message"),
                        timestamp.toLocalDateTime()
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
}