package com.todoapp.dao;

import com.todoapp.model.Task;
import com.todoapp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for {@link Task} entities.
 * Supports full CRUD plus filtering and statistics queries.
 */
public class TaskDAO {

    private static final Logger logger = LoggerFactory.getLogger(TaskDAO.class);

    // -----------------------------------------------------------------------
    // Create
    // -----------------------------------------------------------------------

    /**
     * Inserts a new task and returns the generated ID.
     */
    public int createTask(Task task) {
        final String sql = """
            INSERT INTO tasks (user_id, title, description, status, priority, category, due_date, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, task.getUserId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setString(4, task.getStatus());
            ps.setString(5, task.getPriority());
            ps.setString(6, task.getCategory());
            ps.setObject(7, task.getDueDate()); // null-safe
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    logger.info("Created task id={} title='{}'", id, task.getTitle());
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to create task: {}", task.getTitle(), e);
        }
        return -1;
    }

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    /** Returns all tasks belonging to the given user, newest first. */
    public List<Task> findAllByUser(int userId) {
        final String sql = """
            SELECT * FROM tasks
             WHERE user_id = ?
             ORDER BY created_at DESC
            """;
        return queryTasks(sql, userId);
    }

    /** Returns tasks filtered by status for a user. */
    public List<Task> findByUserAndStatus(int userId, String status) {
        final String sql = """
            SELECT * FROM tasks
             WHERE user_id = ? AND status = ?
             ORDER BY due_date ASC, priority DESC
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, status);
            return extractTasks(ps.executeQuery());
        } catch (SQLException e) {
            logger.error("Error querying tasks by status", e);
            return List.of();
        }
    }

    /** Returns tasks with a title/description matching the search term. */
    public List<Task> searchTasks(int userId, String keyword) {
        final String sql = """
            SELECT * FROM tasks
             WHERE user_id = ?
               AND (title LIKE ? OR description LIKE ? OR category LIKE ?)
             ORDER BY created_at DESC
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            ps.setInt(1, userId);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            return extractTasks(ps.executeQuery());
        } catch (SQLException e) {
            logger.error("Error searching tasks", e);
            return List.of();
        }
    }

    /** Finds a single task by its primary key. */
    public Optional<Task> findById(int taskId) {
        final String sql = "SELECT * FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            List<Task> results = extractTasks(ps.executeQuery());
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (SQLException e) {
            logger.error("Error finding task by id={}", taskId, e);
            return Optional.empty();
        }
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    /** Updates all editable fields of an existing task. */
    public boolean updateTask(Task task) {
        final String sql = """
            UPDATE tasks
               SET title = ?, description = ?, status = ?, priority = ?,
                   category = ?, due_date = ?, updated_at = NOW()
             WHERE id = ? AND user_id = ?
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getStatus());
            ps.setString(4, task.getPriority());
            ps.setString(5, task.getCategory());
            ps.setObject(6, task.getDueDate());
            ps.setInt(7, task.getId());
            ps.setInt(8, task.getUserId());

            int rows = ps.executeUpdate();
            logger.info("Updated task id={}, rows={}", task.getId(), rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Failed to update task id={}", task.getId(), e);
            return false;
        }
    }

    /** Quickly toggles a task's completion status. */
    public boolean toggleComplete(int taskId, int userId) {
        final String sql = """
            UPDATE tasks
               SET status = CASE
                   WHEN status = 'COMPLETED' THEN 'PENDING'
                   ELSE 'COMPLETED'
               END,
               updated_at = NOW()
             WHERE id = ? AND user_id = ?
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to toggle task id={}", taskId, e);
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Delete
    // -----------------------------------------------------------------------

    /** Deletes a task owned by the specified user (prevents cross-user deletion). */
    public boolean deleteTask(int taskId, int userId) {
        final String sql = "DELETE FROM tasks WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            logger.info("Deleted task id={}, rows={}", taskId, rows);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete task id={}", taskId, e);
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Statistics helpers (used by dashboard)
    // -----------------------------------------------------------------------

    public int countByUserAndStatus(int userId, String status) {
        final String sql = "SELECT COUNT(*) FROM tasks WHERE user_id = ? AND status = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, status);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            logger.error("Error counting tasks", e);
            return 0;
        }
    }

    public int countOverdue(int userId) {
        final String sql = """
            SELECT COUNT(*) FROM tasks
             WHERE user_id = ? AND status != 'COMPLETED' AND due_date < CURDATE()
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            logger.error("Error counting overdue tasks", e);
            return 0;
        }
    }

    public int countTotal(int userId) {
        final String sql = "SELECT COUNT(*) FROM tasks WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private List<Task> queryTasks(String sql, int userId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return extractTasks(ps.executeQuery());
        } catch (SQLException e) {
            logger.error("Error querying tasks for user id={}", userId, e);
            return new ArrayList<>();
        }
    }

    private List<Task> extractTasks(ResultSet rs) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        while (rs.next()) {
            tasks.add(mapRow(rs));
        }
        return tasks;
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setStatus(rs.getString("status"));
        t.setPriority(rs.getString("priority"));
        t.setCategory(rs.getString("category"));

        Date due = rs.getDate("due_date");
        if (due != null) t.setDueDate(due.toLocalDate());

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) t.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) t.setUpdatedAt(updated.toLocalDateTime());

        return t;
    }
}
