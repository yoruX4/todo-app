package com.todoapp.controller;

import com.todoapp.MainApp;
import com.todoapp.dao.TaskDAO;
import com.todoapp.model.Task;
import com.todoapp.model.User;
import com.todoapp.util.AlertUtil;
import com.todoapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the Dashboard screen (dashboard.fxml).
 *
 * Shows summary statistics: total, pending, completed, overdue tasks.
 * Provides navigation to the Task Manager and Profile screens.
 */
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @FXML private Label welcomeLabel;
    @FXML private Label totalTasksLabel;
    @FXML private Label pendingTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label overdueTasksLabel;
    @FXML private Label inProgressTasksLabel;

    private final TaskDAO taskDAO = new TaskDAO();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            // Session expired – redirect to login
            MainApp.loadScene("login");
            return;
        }

        welcomeLabel.setText("Welcome back, " + user.getFullName() + "!");
        loadStats(user.getId());
        logger.info("Dashboard loaded for user id={}", user.getId());
    }

    // -----------------------------------------------------------------------
    // Event handlers (sidebar navigation)
    // -----------------------------------------------------------------------

    @FXML private void handleGoToDashboard() { MainApp.loadScene("dashboard"); }
    @FXML private void handleGoToTasks()     { MainApp.loadScene("tasks"); }
    @FXML private void handleGoToProfile()   { MainApp.loadScene("profile"); }

    @FXML
    private void handleLogout() {
        if (AlertUtil.showConfirmation("Logout", "Are you sure you want to log out?")) {
            SessionManager.clearSession();
            logger.info("User logged out.");
            MainApp.loadScene("login");
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void loadStats(int userId) {
        int total     = taskDAO.countTotal(userId);
        int pending   = taskDAO.countByUserAndStatus(userId, Task.STATUS_PENDING);
        int inProg    = taskDAO.countByUserAndStatus(userId, Task.STATUS_IN_PROGRESS);
        int completed = taskDAO.countByUserAndStatus(userId, Task.STATUS_COMPLETED);
        int overdue   = taskDAO.countOverdue(userId);

        totalTasksLabel.setText(String.valueOf(total));
        pendingTasksLabel.setText(String.valueOf(pending));
        inProgressTasksLabel.setText(String.valueOf(inProg));
        completedTasksLabel.setText(String.valueOf(completed));
        overdueTasksLabel.setText(String.valueOf(overdue));
    }
}
