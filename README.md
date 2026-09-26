# 💬 Chat Application
A desktop-based real-time chat application developed using **JavaFX**, **Java**, **MySQL**, and **Socket Programming**.

The application provides private messaging, group chats, user management, message search, file/image sharing, emoji support, profile management, settings, and other modern chat features.

## 📌 Project Overview
The **Chat Application** is designed to provide a simple and user-friendly platform for communication between users.

Users can register and log in to the application, chat privately with other users, create and manage groups, send files and images, use emojis, search users and messages, edit or delete messages, and manage their profile and application settings.

The application uses a **client-server architecture** with socket communication for real-time messaging.

## ✨ Features

### 🔐 Authentication
- User Registration
- User Login
- Username validation
- Password confirmation
- Logout functionality

### 💬 Private Chat
- One-to-one messaging
- Real-time message communication
- Online / Offline user status
- User conversation list
- Unread message count

### 👥 Group Management
- Create groups
- View groups
- Group chat
- Add group members
- Remove group members
- View group members
- Leave group
- Delete group
- Group admin controls
- Search groups

### 🔎 Search
- Search users
- Search groups
- Search messages
- Clear search functionality

### ✏️ Message Management
- Send messages
- Edit messages
- Delete messages
- Message timestamps
- Unread message tracking

### 📎 File and Image Sharing
- Upload files
- Share images
- Display shared images in chat
- File information handling

### 😊 Emoji Support
- Emoji picker
- Multiple emoji categories
- Insert emojis directly into messages
  
### 👤 Profile Management
- View profile
- Edit profile
- Update username
- Update email
- Change password
- Online status display
- User ID display
  
### ⚙ Settings
- Notification settings
- Message sound settings
- Dark mode option
- Online status settings
- Privacy settings
- Chat preferences

### 🎨 User Interface
- Modern JavaFX interface
- Lavender / Purple theme
- Responsive chat layout
- Scrollable user list
- Scrollable group list
- Search bars
- Styled message bubbles
- Modern buttons and controls
## 🛠️ Technologies Used

- **Java**
- **JavaFX**
- **FXML**
- **CSS**
- **MySQL**
- **JDBC**
- **Socket Programming**
- **Maven**
- **Git & GitHub**

## 🏗️ Project Structure
```text
chat-application/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── chatapp/
│   │   │           ├── client/
│   │   │           ├── controller/
│   │   │           ├── dao/
│   │   │           ├── database/
│   │   │           ├── model/
│   │   │           └── server/
│   │   │
│   │   └── resources/
│   │       ├── css/
│   │       └── fxml/
│   │
│   └── test/
│
├── pom.xml
└── README.md
