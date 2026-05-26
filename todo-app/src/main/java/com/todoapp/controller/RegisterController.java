package com.todoapp.controller;

import com.todoapp.MainApp;
import com.todoapp.dao.UserDAO;
import com.todoapp.model.User;
import com.todoapp.util.PasswordUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the Registration screen (register.fxml).
 */
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @FXML private TextField     fullNameField;
    @FXML private TextField     usernameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         errorLabel;
    @FXML private Label         successLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }

    // -----------------------------------------------------------------------
    // Event handlers
    // -----------------------------------------------------------------------

    @FXML
    private void handleRegister() {
        clearMessages();

        String fullName  = fullNameField.getText().trim();
        String username  = usernameField.getText().trim();
        String email     = emailField.getText().trim();
        String password  = passwordField.getText();
        String confirm   = confirmPasswordField.getText();

        // ---- Validation ----
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("All fields are required.");
            return;
        }
        if (username.length() < 3 || username.length() > 50) {
            showError("Username must be between 3 and 50 characters.");
            return;
        }
        if (!email.matches("^[\\w.%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showError("Please enter a valid email address.");
            return;
        }
        if (password.length() < 8) {
            showError("Password must be at least 8 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            confirmPasswordField.clear();
            return;
        }

        // ---- Duplicate checks ----
        if (userDAO.findByUsername(username).isPresent()) {
            showError("Username '" + username + "' is already taken.");
            return;
        }
        if (userDAO.findByEmail(email).isPresent()) {
            showError("An account with that email already exists.");
            return;
        }

        // ---- Create user ----
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(PasswordUtil.hash(password)); // BCrypt here

        int id = userDAO.createUser(newUser);
        if (id > 0) {
            logger.info("Registered new user id={} username='{}'", id, username);
            showSuccess("Account created! Redirecting to login…");
            // Small delay so user sees the success message, then navigate
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> MainApp.loadScene("login"));
            }).start();
        } else {
            showError("Registration failed. Please try again.");
        }
    }

    @FXML
    private void handleGoToLogin() {
        MainApp.loadScene("login");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void showSuccess(String msg) {
        successLabel.setText(msg);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }

    private void clearMessages() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }
}
