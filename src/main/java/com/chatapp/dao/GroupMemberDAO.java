package com.chatapp.dao;

import com.chatapp.model.GroupMember;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GroupMemberDAO {

    private final Connection connection;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public GroupMemberDAO(Connection connection) {
        this.connection = connection;
    }


    // =========================================================
    // GET GROUP MEMBERS
    // =========================================================

    public List<GroupMember> getGroupMembers(int groupId) {

        List<GroupMember> members =
                new ArrayList<>();

        String sql = """
                SELECT
                    gm.id,
                    gm.group_id,
                    gm.user_id,
                    u.username,
                    u.email,
                    gm.joined_at
                FROM group_members gm
                INNER JOIN users u
                    ON gm.user_id = u.id
                WHERE gm.group_id = ?
                ORDER BY u.username ASC
                """;

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, groupId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    GroupMember member =
                            new GroupMember();

                    member.setId(
                            rs.getInt("id")
                    );

                    member.setGroupId(
                            rs.getInt("group_id")
                    );

                    member.setUserId(
                            rs.getInt("user_id")
                    );

                    member.setUsername(
                            rs.getString("username")
                    );

                    member.setEmail(
                            rs.getString("email")
                    );

                    Timestamp timestamp =
                            rs.getTimestamp("joined_at");

                    if (timestamp != null) {

                        member.setJoinedAt(
                                timestamp.toLocalDateTime()
                        );
                    }

                    members.add(member);
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error loading group members: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return members;
    }


    // =========================================================
    // CHECK WHETHER USER IS A MEMBER
    // =========================================================

    public boolean isMember(
            int groupId,
            int userId) {

        String sql = """
                SELECT COUNT(*)
                FROM group_members
                WHERE group_id = ?
                AND user_id = ?
                """;

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            ps.setInt(2, userId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error checking group membership: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return false;
    }


    // =========================================================
    // ADD MEMBER
    // =========================================================

    public boolean addMember(
            int groupId,
            int userId) {

        // -----------------------------------------------------
        // PREVENT DUPLICATE MEMBER
        // -----------------------------------------------------

        if (isMember(groupId, userId)) {

            System.out.println(
                    "User is already a member of this group."
            );

            return false;
        }


        String sql = """
                INSERT INTO group_members
                (group_id, user_id)
                VALUES (?, ?)
                """;

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            ps.setInt(2, userId);

            int rows =
                    ps.executeUpdate();

            if (rows > 0) {

                System.out.println(
                        "Member added successfully."
                );

                return true;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error adding group member: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return false;
    }


    // =========================================================
    // REMOVE MEMBER
    // =========================================================

    public boolean removeMember(
            int groupId,
            int userId) {

        String sql = """
                DELETE FROM group_members
                WHERE group_id = ?
                AND user_id = ?
                """;

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            ps.setInt(2, userId);

            int rows =
                    ps.executeUpdate();

            if (rows > 0) {

                System.out.println(
                        "Member removed successfully."
                );

                return true;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error removing group member: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return false;
    }


    // =========================================================
    // LEAVE GROUP
    // =========================================================

    public boolean leaveGroup(
            int groupId,
            int userId) {

        String sql = """
                DELETE FROM group_members
                WHERE group_id = ?
                AND user_id = ?
                """;

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            ps.setInt(2, userId);

            int rows =
                    ps.executeUpdate();

            if (rows > 0) {

                System.out.println(
                        "User left the group successfully."
                );

                return true;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error leaving group: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return false;
    }


    // =========================================================
    // GET MEMBER COUNT
    // =========================================================

    public int getMemberCount(
            int groupId) {

        String sql = """
                SELECT COUNT(*)
                FROM group_members
                WHERE group_id = ?
                """;

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, groupId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error getting member count: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return 0;
    }


    // =========================================================
    // GET ALL USERS WHO ARE NOT GROUP MEMBERS
    // =========================================================

    public List<GroupMember> getNonMembers(
            int groupId) {

        List<GroupMember> users =
                new ArrayList<>();

        String sql = """
                SELECT
                    u.id,
                    u.username,
                    u.email
                FROM users u
                WHERE u.id NOT IN (
                    SELECT gm.user_id
                    FROM group_members gm
                    WHERE gm.group_id = ?
                )
                ORDER BY u.username ASC
                """;

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, groupId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    GroupMember user =
                            new GroupMember();

                    user.setUserId(
                            rs.getInt("id")
                    );

                    user.setUsername(
                            rs.getString("username")
                    );

                    user.setEmail(
                            rs.getString("email")
                    );

                    users.add(user);
                }
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error loading non-members: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return users;
    }
}