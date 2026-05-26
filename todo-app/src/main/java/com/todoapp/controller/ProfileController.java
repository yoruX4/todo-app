package com.todoapp.controller;

import com.todoapp.MainApp;
import com.todoapp.dao.UserDAO;
import com.todoapp.model.User;
import com.todoapp.util.AlertUtil;
import com.todoapp.util.PasswordUtil;
import com.todoapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the Profile / Settings screen (profile.fxml).
 *
 * Allows the user to:
 *   - Update their display name and email
 *   - Change their password (requires current password verification)
 *   - Delete their account
 */
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    // ---- Sidebar ----
    @FXML private void handleGoToDashboard() { MainApp.loadScene("dashboard"); }
    @FXML private void handleGoToTasks()     { MainApp.loadScene("tasks"); }
    @FXML private void handleGoToProfile()   { /* already here */ }
    @FXML private void handleLogout() {
        if (AlertUtil.showConfirmation("Logout", "Are you sure you want to log out?")) {
            SessionManager.clearSession();
            MainApp.loadScene("login");
        }
    }

    // ---- Profile form ----
    @FXML private TextField     fullNameField;
    @FXML private TextField     emailField;
    @FXML private TextField     usernameField; // read-only
    @FXML private Label         profileStatusLabel;

    // ---- Password form ----
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         passwordStatusLabel;

    private final UserDAO userDAO = new UserDAO();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            MainApp.loadScene("login");
            return;
        }

        // Pre-populate fields
        fullNameField.setText(currentUser.getFullName());
        emailField.setText(currentUser.getEmail());
        usernameField.setText(currentUser.getUsername());
        usernameField.setEditable(false); // username cannot change

        profileStatusLabel.setVisible(false);
        passwordStatusLabel.setVisible(false);
    }

    // -----------------------------------------------------------------------
    // Profile update
    // -----------------------------------------------------------------------

    @FXML
    private void handleUpdateProfile() {
        profileStatusLabel.setVisible(false);

        String fullName = fullNameField.getText().trim();
        String email    = emailField.getText().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            showProfileStatus("Name and email are required.", true);
            return;
        }
        if (!email.matches("^[\\w.%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showProfileStatus("Please enter a valid email address.", true);
            return;
        }

        // Check if email is taken by another user
        userDAO.findByEmail(email).ifPresent(other -> {
            if (other.getId() != currentUser.getId()) {
                showProfileStatus("That email is already in use by another account.", true);
            }
        });

        currentUser.setFullName(fullName);
        currentUser.setEmail(email);

        if (userDAO.updateProfile(currentUser)) {
            SessionManager.setCurrentUser(currentUser); // refresh session
            showProfileStatus("Profile updated successfully!", false);
            logger.info("Profile updated for user id={}", currentUser.getId());
        } else {
            showProfileStatus("Failed to update profile. Please try again.", true);
        }
    }

    // -----------------------------------------------------------------------
    // Password change
    // -----------------------------------------------------------------------

    @FXML
    private void handleChangePassword() {
        passwordStatusLabel.setVisible(false);

        String current = currentPasswordField.getText();
        String newPass  = newPasswordField.getText();
        String confirm  = confirmPasswordField.getText();

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showPasswordStatus("All password fields are required.", true);
            return;
        }
        if (!PasswordUtil.verify(current, currentUser.getPasswordHash())) {
            showPasswordStatus("Current password is incorrect.", true);
            currentPasswordField.clear();
            return;
        }
        if (newPass.length() < 8) {
            showPasswordStatus("New password must be at least 8 characters.", true);
            return;
        }
        if (!newPass.equals(confirm)) {
            showPasswordStatus("New passwords do not match.", true);
            newPasswordField.clear();
            confirmPasswordField.clear();
            return;
        }

        String newHash = PasswordUtil.hash(newPass);
        if (userDAO.updatePassword(currentUser.getId(), newHash)) {
            currentUser.setPasswordHash(newHash);
            SessionManager.setCurrentUser(currentUser);
            showPasswordStatus("Password changed successfully!", false);
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            logger.info("Password changed for user id={}", currentUser.getId());
        } else {
            showPasswordStatus("Failed to change password. Please try again.", true);
        }
    }

    // -----------------------------------------------------------------------
    // Account deletion
    // -----------------------------------------------------------------------

    @FXML
    private void handleDeleteAccount() {
        boolean confirmed = AlertUtil.showConfirmation(
            "Delete Account",
            "⚠ This will permanently delete your account and ALL your tasks.\n\nThis action cannot be undone. Continue?"
        );
        if (!confirmed) return;

        // Second confirmation
        boolean doubleConfirmed = AlertUtil.showConfirmation(
            "Final Confirmation",
            "Are you absolutely sure? All data will be lost forever."
        );
        if (!doubleConfirmed) return;

        int userId = currentUser.getId();
        SessionManager.clearSession();

        if (userDAO.deleteUser(userId)) {
            logger.warn("Account deleted for user id={}", userId);
            AlertUtil.showInfo("Account Deleted", "Your account has been permanently deleted.");
            MainApp.loadScene("login");
        } else {
            AlertUtil.showError("Error", "Failed to delete account. Please try again.");
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void showProfileStatus(String msg, boolean isError) {
        profileStatusLabel.setText(msg);
        profileStatusLabel.getStyleClass().removeAll("success-label","error-label");
        profileStatusLabel.getStyleClass().add(isError ? "error-label" : "success-label");
        profileStatusLabel.setVisible(true);
    }

    private void showPasswordStatus(String msg, boolean isError) {
        passwordStatusLabel.setText(msg);
        passwordStatusLabel.getStyleClass().removeAll("success-label","error-label");
        passwordStatusLabel.getStyleClass().add(isError ? "error-label" : "success-label");
        passwordStatusLabel.setVisible(true);
    }
}
