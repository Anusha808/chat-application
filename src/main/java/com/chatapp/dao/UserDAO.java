package com.chatapp.dao;

import com.chatapp.database.DatabaseConnection;
import com.chatapp.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // ==========================================
    // REGISTER USER
    // ==========================================

    public boolean registerUser(User user) {

        String sql = """
                INSERT INTO users (username, email, password)
                VALUES (?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    user.getUsername()
            );

            statement.setString(
                    2,
                    user.getEmail()
            );

            statement.setString(
                    3,
                    user.getPassword()
            );

            int rowsInserted =
                    statement.executeUpdate();

            return rowsInserted > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Registration failed: "
                            + e.getMessage()
            );

            return false;
        }
    }


    // ==========================================
    // LOGIN USER
    // ==========================================

    public User loginUser(
            String username,
            String password
    ) {

        String sql = """
                SELECT id, username, email, password
                FROM users
                WHERE username = ? AND password = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    username
            );

            statement.setString(
                    2,
                    password
            );

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                return new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("email"),
                        resultSet.getString("password")
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Login failed: "
                            + e.getMessage()
            );
        }

        return null;
    }


    // ==========================================
    // FIND USER BY USERNAME
    // ==========================================

    public User findUserByUsername(
            String username
    ) {

        String sql = """
                SELECT id, username, email, password
                FROM users
                WHERE username = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    username
            );

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                return new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("email"),
                        resultSet.getString("password")
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error finding user: "
                            + e.getMessage()
            );
        }

        return null;
    }


    // ==========================================
    // GET ALL USERS
    // ==========================================

    public java.util.List<User> getAllUsers() {

        java.util.List<User> users =
                new java.util.ArrayList<>();

        String sql = """
                SELECT id, username, email, password
                FROM users
                ORDER BY username
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                User user = new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("email"),
                        resultSet.getString("password")
                );

                users.add(user);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error loading users: "
                            + e.getMessage()
            );
        }

        return users;
    }


    // ==========================================
    // UPDATE USER PROFILE
    // ==========================================

    public boolean updateUserProfile(
            int userId,
            String username,
            String email
    ) {

        String sql = """
                UPDATE users
                SET username = ?, email = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    username
            );

            statement.setString(
                    2,
                    email
            );

            statement.setInt(
                    3,
                    userId
            );

            int rowsUpdated =
                    statement.executeUpdate();

            return rowsUpdated > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Profile update failed: "
                            + e.getMessage()
            );

            return false;
        }
    }


    // ==========================================
    // CHANGE PASSWORD
    // ==========================================

    public boolean changePassword(
            int userId,
            String currentPassword,
            String newPassword
    ) {

        String sql = """
                UPDATE users
                SET password = ?
                WHERE id = ? AND password = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    newPassword
            );

            statement.setInt(
                    2,
                    userId
            );

            statement.setString(
                    3,
                    currentPassword
            );

            int rowsUpdated =
                    statement.executeUpdate();

            return rowsUpdated > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Password change failed: "
                            + e.getMessage()
            );

            return false;
        }
    }
}