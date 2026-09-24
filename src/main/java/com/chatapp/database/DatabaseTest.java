package com.chatapp.database;

import java.sql.Connection;

public class DatabaseTest {

    public static void main(String[] args) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            System.out.println("=================================");
            System.out.println("MySQL Connection Successful!");
            System.out.println("Database: chat_application");
            System.out.println("=================================");

        } catch (Exception e) {

            System.out.println("MySQL Connection Failed!");
            e.printStackTrace();
        }
    }
}