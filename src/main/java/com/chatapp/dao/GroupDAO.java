package com.chatapp.dao;

import com.chatapp.database.DatabaseConnection;
import com.chatapp.model.Group;
import com.chatapp.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {

    // =========================================================
    // CREATE GROUP
    // =========================================================

    public Group createGroup(
            String groupName,
            int createdBy
    ) {

        String sql = """
                INSERT INTO chat_groups
                (group_name, created_by)
                VALUES (?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             java.sql.Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(1, groupName);
            statement.setInt(2, createdBy);

            int rowsInserted =
                    statement.executeUpdate();

            if (rowsInserted > 0) {

                ResultSet keys =
                        statement.getGeneratedKeys();

                if (keys.next()) {

                    int groupId =
                            keys.getInt(1);

                    Group group =
                            new Group(
                                    groupId,
                                    groupName,
                                    createdBy,
                                    null
                            );

                    addMember(
                            groupId,
                            createdBy
                    );

                    return group;
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error creating group: "
                            + e.getMessage()
            );
        }

        return null;
    }


    // =========================================================
    // ADD MEMBER
    // =========================================================

    public boolean addMember(
            int groupId,
            int userId
    ) {

        String sql = """
                INSERT INTO group_members
                (group_id, user_id)
                VALUES (?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, groupId);
            statement.setInt(2, userId);

            int rowsInserted =
                    statement.executeUpdate();

            return rowsInserted > 0;

        } catch (SQLException e) {

            if (e.getErrorCode() == 1062) {

                return false;
            }

            System.out.println(
                    "Error adding group member: "
                            + e.getMessage()
            );

            return false;
        }
    }


    // =========================================================
    // ADD MEMBER TO EXISTING GROUP
    // =========================================================

    public boolean addMemberToExistingGroup(
            int groupId,
            int userId
    ) {

        String sql = """
                INSERT INTO group_members
                (group_id, user_id)
                VALUES (?, ?)
                """;

        System.out.println(
                "================================="
        );

        System.out.println(
                "Adding member to existing group..."
        );

        System.out.println(
                "Group ID: " + groupId
        );

        System.out.println(
                "User ID: " + userId
        );

        System.out.println(
                "================================="
        );

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, groupId);
            statement.setInt(2, userId);

            int rowsInserted =
                    statement.executeUpdate();

            System.out.println(
                    "Rows inserted: "
                            + rowsInserted
            );

            if (rowsInserted > 0) {

                System.out.println(
                        "Member added successfully!"
                );

                return true;
            }

        } catch (SQLException e) {

            if (e.getErrorCode() == 1062) {

                System.out.println(
                        "User is already a member "
                                + "of this group."
                );

                return false;
            }

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "ERROR ADDING GROUP MEMBER"
            );

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "SQL Error Code: "
                            + e.getErrorCode()
            );

            System.out.println(
                    "SQL State: "
                            + e.getSQLState()
            );

            System.out.println(
                    "Message: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }

        return false;
    }


    // =========================================================
    // REMOVE MEMBER
    // =========================================================

    public boolean removeMember(
            int groupId,
            int userId
    ) {

        String sql = """
                DELETE FROM group_members
                WHERE group_id = ?
                AND user_id = ?
                """;

        System.out.println(
                "================================="
        );

        System.out.println(
                "Trying to remove group member..."
        );

        System.out.println(
                "Group ID: " + groupId
        );

        System.out.println(
                "User ID: " + userId
        );

        System.out.println(
                "================================="
        );

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, groupId);
            statement.setInt(2, userId);

            int rowsDeleted =
                    statement.executeUpdate();

            System.out.println(
                    "Rows deleted: "
                            + rowsDeleted
            );

            if (rowsDeleted > 0) {

                System.out.println(
                        "Member removed successfully!"
                );

                return true;
            }

            System.out.println(
                    "No matching group member found."
            );

            return false;

        } catch (SQLException e) {

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "ERROR REMOVING GROUP MEMBER"
            );

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "SQL Error Code: "
                            + e.getErrorCode()
            );

            System.out.println(
                    "SQL State: "
                            + e.getSQLState()
            );

            System.out.println(
                    "Message: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }


    // =========================================================
    // DELETE GROUP
    // =========================================================

    public boolean deleteGroup(
            int groupId,
            int userId
    ) {

        System.out.println(
                "================================="
        );

        System.out.println(
                "Trying to delete group..."
        );

        System.out.println(
                "Group ID: " + groupId
        );

        System.out.println(
                "User ID: " + userId
        );

        System.out.println(
                "================================="
        );

        String checkSql = """
                SELECT created_by
                FROM chat_groups
                WHERE id = ?
                """;

        String deleteMessagesSql = """
                DELETE FROM group_messages
                WHERE group_id = ?
                """;

        String deleteMembersSql = """
                DELETE FROM group_members
                WHERE group_id = ?
                """;

        String deleteGroupSql = """
                DELETE FROM chat_groups
                WHERE id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            // -------------------------------------------------
            // CHECK GROUP ADMIN
            // -------------------------------------------------

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 checkSql
                         )) {

                statement.setInt(1, groupId);

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    if (!resultSet.next()) {

                        System.out.println(
                                "Group not found."
                        );

                        return false;
                    }

                    int createdBy =
                            resultSet.getInt(
                                    "created_by"
                            );

                    if (createdBy != userId) {

                        System.out.println(
                                "Permission denied. "
                                        + "Only the group admin "
                                        + "can delete the group."
                        );

                        return false;
                    }
                }
            }


            // -------------------------------------------------
            // START TRANSACTION
            // -------------------------------------------------

            connection.setAutoCommit(false);

            try {

                // -------------------------------------------------
                // DELETE GROUP MESSAGES
                // -------------------------------------------------

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     deleteMessagesSql
                             )) {

                    statement.setInt(
                            1,
                            groupId
                    );

                    int rowsDeleted =
                            statement.executeUpdate();

                    System.out.println(
                            "Group messages deleted: "
                                    + rowsDeleted
                    );
                }


                // -------------------------------------------------
                // DELETE GROUP MEMBERS
                // -------------------------------------------------

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     deleteMembersSql
                             )) {

                    statement.setInt(
                            1,
                            groupId
                    );

                    int rowsDeleted =
                            statement.executeUpdate();

                    System.out.println(
                            "Group members deleted: "
                                    + rowsDeleted
                    );
                }


                // -------------------------------------------------
                // DELETE GROUP
                // -------------------------------------------------

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     deleteGroupSql
                             )) {

                    statement.setInt(
                            1,
                            groupId
                    );

                    int rowsDeleted =
                            statement.executeUpdate();

                    if (rowsDeleted > 0) {

                        connection.commit();

                        System.out.println(
                                "Group deleted successfully!"
                        );

                        return true;
                    }

                    connection.rollback();

                    System.out.println(
                            "Group could not be deleted."
                    );

                    return false;
                }

            } catch (SQLException e) {

                connection.rollback();

                System.out.println(
                        "Transaction rolled back."
                );

                throw e;

            } finally {

                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "ERROR DELETING GROUP"
            );

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "SQL Error Code: "
                            + e.getErrorCode()
            );

            System.out.println(
                    "SQL State: "
                            + e.getSQLState()
            );

            System.out.println(
                    "Message: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }


    // =========================================================
    // RENAME GROUP
    // =========================================================

    public boolean renameGroup(
            int groupId,
            int userId,
            String newGroupName
    ) {

        System.out.println(
                "================================="
        );

        System.out.println(
                "Trying to rename group..."
        );

        System.out.println(
                "Group ID: " + groupId
        );

        System.out.println(
                "User ID: " + userId
        );

        System.out.println(
                "New Group Name: "
                        + newGroupName
        );

        System.out.println(
                "================================="
        );


        // -------------------------------------------------
        // VALIDATE GROUP NAME
        // -------------------------------------------------

        if (newGroupName == null
                || newGroupName.trim().isEmpty()) {

            System.out.println(
                    "Group name cannot be empty."
            );

            return false;
        }


        String sql = """
                UPDATE chat_groups
                SET group_name = ?
                WHERE id = ?
                AND created_by = ?
                """;


        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    newGroupName.trim()
            );

            statement.setInt(
                    2,
                    groupId
            );

            statement.setInt(
                    3,
                    userId
            );


            int rowsUpdated =
                    statement.executeUpdate();


            if (rowsUpdated > 0) {

                System.out.println(
                        "Group renamed successfully!"
                );

                return true;
            }


            System.out.println(
                    "Group could not be renamed."
            );

            System.out.println(
                    "Either the group does not exist "
                            + "or the user is not the admin."
            );

            return false;


        } catch (SQLException e) {

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "ERROR RENAMING GROUP"
            );

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "SQL Error Code: "
                            + e.getErrorCode()
            );

            System.out.println(
                    "SQL State: "
                            + e.getSQLState()
            );

            System.out.println(
                    "Message: "
                            + e.getMessage()
            );

            e.printStackTrace();

            return false;
        }
    }


    // =========================================================
    // GET ALL GROUPS
    // =========================================================

    public List<Group> getAllGroups() {

        List<Group> groups =
                new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    group_name,
                    created_by,
                    created_at
                FROM chat_groups
                ORDER BY group_name
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                Timestamp timestamp =
                        resultSet.getTimestamp(
                                "created_at"
                        );

                Group group =
                        new Group(
                                resultSet.getInt("id"),
                                resultSet.getString(
                                        "group_name"
                                ),
                                resultSet.getInt(
                                        "created_by"
                                ),
                                timestamp != null
                                        ? timestamp.toLocalDateTime()
                                        : null
                        );

                groups.add(group);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error loading groups: "
                            + e.getMessage()
            );
        }

        return groups;
    }


    // =========================================================
    // GET GROUPS FOR USER
    // =========================================================

    public List<Group> getGroupsForUser(
            int userId
    ) {

        List<Group> groups =
                new ArrayList<>();

        String sql = """
                SELECT
                    g.id,
                    g.group_name,
                    g.created_by,
                    g.created_at
                FROM chat_groups g
                INNER JOIN group_members gm
                    ON g.id = gm.group_id
                WHERE gm.user_id = ?
                ORDER BY g.group_name
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            ResultSet resultSet =
                    statement.executeQuery();

            while (resultSet.next()) {

                Timestamp timestamp =
                        resultSet.getTimestamp(
                                "created_at"
                        );

                Group group =
                        new Group(
                                resultSet.getInt("id"),
                                resultSet.getString(
                                        "group_name"
                                ),
                                resultSet.getInt(
                                        "created_by"
                                ),
                                timestamp != null
                                        ? timestamp.toLocalDateTime()
                                        : null
                        );

                groups.add(group);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error loading user groups: "
                            + e.getMessage()
            );
        }

        return groups;
    }


    // =========================================================
    // GET GROUP MEMBERS
    // =========================================================

    public List<User> getGroupMembers(
            int groupId
    ) {

        List<User> members =
                new ArrayList<>();

        String sql = """
                SELECT
                    u.id,
                    u.username,
                    u.email,
                    u.password
                FROM users u
                INNER JOIN group_members gm
                    ON u.id = gm.user_id
                WHERE gm.group_id = ?
                ORDER BY u.username
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, groupId);

            ResultSet resultSet =
                    statement.executeQuery();

            while (resultSet.next()) {

                User user =
                        new User(
                                resultSet.getInt("id"),
                                resultSet.getString(
                                        "username"
                                ),
                                resultSet.getString(
                                        "email"
                                ),
                                resultSet.getString(
                                        "password"
                                )
                        );

                members.add(user);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error loading group members: "
                            + e.getMessage()
            );
        }

        return members;
    }


    // =========================================================
    // CHECK GROUP MEMBERSHIP
    // =========================================================

    public boolean isMember(
            int groupId,
            int userId
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM group_members
                WHERE group_id = ?
                AND user_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, groupId);
            statement.setInt(2, userId);

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                return resultSet.getInt(1) > 0;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error checking group membership: "
                            + e.getMessage()
            );
        }

        return false;
    }
}