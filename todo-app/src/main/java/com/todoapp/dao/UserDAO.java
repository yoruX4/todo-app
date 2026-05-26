package com.todoapp.dao;

import com.todoapp.model.User;
import com.todoapp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Data Access Object for {@link User} entities.
 * All SQL is parameterised to prevent SQL injection.
 */
public class UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    // -----------------------------------------------------------------------
    // Create
    // -----------------------------------------------------------------------

    /**
     * Inserts a new user into the database.
     *
     * @param user user with {@code passwordHash} already set via BCrypt
     * @return the generated auto-increment ID, or -1 on failure
     */
    public int createUser(User user) {
        final String sql = """
            INSERT INTO users (username, email, password_hash, full_name, created_at, updated_at)
            VALUES (?, ?, ?, ?, NOW(), NOW())
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getFullName());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    logger.info("Created user id={} username={}", id, user.getUsername());
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to create user: {}", user.getUsername(), e);
        }
        return -1;
    }

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    /**
     * Finds a user by their username (used during login).
     */
    public Optional<User> findByUsername(String username) {
        final String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
        }
        return Optional.empty();
    }

    /**
     * Finds a user by their email (used for registration duplicate check).
     */
    public Optional<User> findByEmail(String email) {
        final String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by email: {}", email, e);
        }
        return Optional.empty();
    }

    /**
     * Finds a user by their primary key.
     */
    public Optional<User> findById(int id) {
        final String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by id: {}", id, e);
        }
        return Optional.empty();
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    /**
     * Updates the user's profile information (full name, email).
     */
    public boolean updateProfile(User user) {
        final String sql = """
            UPDATE users
               SET full_name = ?, email = ?, updated_at = NOW()
             WHERE id = ?
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setInt(3, user.getId());
            int rows = ps.executeUpdate();
            logger.info("Updated profile for user id={}, rows={}", user.getId(), rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Failed to update user profile id={}", user.getId(), e);
            return false;
        }
    }

    /**
     * Updates only the password hash (used in the change-password flow).
     */
    public boolean updatePassword(int userId, String newPasswordHash) {
        final String sql = "UPDATE users SET password_hash = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            logger.info("Password updated for user id={}", userId);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Failed to update password for user id={}", userId, e);
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Delete
    // -----------------------------------------------------------------------

    /**
     * Deletes a user and (via CASCADE) all their tasks.
     */
    public boolean deleteUser(int userId) {
        final String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            int rows = ps.executeUpdate();
            logger.warn("Deleted user id={}, rows={}", userId, rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete user id={}", userId, e);
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /** Maps the current ResultSet row to a {@link User} object. */
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) u.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) u.setUpdatedAt(updated.toLocalDateTime());

        return u;
    }
}
