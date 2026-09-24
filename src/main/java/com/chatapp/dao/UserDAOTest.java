package com.chatapp.dao;

import com.chatapp.model.User;

public class UserDAOTest {

    public static void main(String[] args) {

        UserDAO userDAO = new UserDAO();

        User user = new User(
                "anusha",
                "anusha@gmail.com",
                "12345"
        );

        boolean registered = userDAO.registerUser(user);

        if (registered) {
            System.out.println("=================================");
            System.out.println("User Registration Successful!");
            System.out.println("=================================");
        } else {
            System.out.println("User Registration Failed!");
        }

        User loggedInUser = userDAO.loginUser(
                "anusha",
                "12345"
        );

        if (loggedInUser != null) {

            System.out.println("=================================");
            System.out.println("Login Successful!");
            System.out.println("Username: " + loggedInUser.getUsername());
            System.out.println("Email: " + loggedInUser.getEmail());
            System.out.println("=================================");

        } else {

            System.out.println("Login Failed!");
        }
    }
}