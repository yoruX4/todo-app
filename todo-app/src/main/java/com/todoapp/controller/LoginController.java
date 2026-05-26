package com.todoapp.controller;

import com.todoapp.MainApp;
import com.todoapp.dao.UserDAO;
import com.todoapp.model.User;
import com.todoapp.util.AlertUtil;
import com.todoapp.util.PasswordUtil;
import com.todoapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Controller for the Login screen (login.fxml).
 *
 * Responsibilities:
 *   - Validate form inputs
 *   - Authenticate user via BCrypt password check
 *   - Redirect to Dashboard on success
 *   - Navigate to Register screen
 */
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         errorLabel;
    @FXML private ProgressIndicator spinner;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        if (spinner != null) spinner.setVisible(false);

        // Allow Enter key to trigger login from either field
        passwordField.setOnKeyPressed(this::handleEnterKey);
        usernameField.setOnKeyPressed(this::handleEnterKey);
    }

    // -----------------------------------------------------------------------
    // Event handlers
    // -----------------------------------------------------------------------

    @FXML
    private void handleLogin() {
        clearError();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Basic validation
        if (username.isEmpty()) {
            showError("Please enter your username.");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password.");
            passwordField.requestFocus();
            return;
        }

        // Disable UI during authentication
        setFormDisabled(true);

        Optional<User> userOpt = userDAO.findByUsername(username);

        if (userOpt.isEmpty() || !PasswordUtil.verify(password, userOpt.get().getPasswordHash())) {
            showError("Invalid username or password.");
            setFormDisabled(false);
            passwordField.clear();
            passwordField.requestFocus();
            logger.warn("Failed login attempt for username='{}'", username);
            return;
        }

        // Success – store session and navigate
        SessionManager.setCurrentUser(userOpt.get());
        logger.info("User '{}' logged in successfully.", username);
        MainApp.loadScene("dashboard");
    }

    @FXML
    private void handleGoToRegister() {
        MainApp.loadScene("register");
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
    }

    private void setFormDisabled(boolean disabled) {
        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
        loginButton.setDisable(disabled);
        if (spinner != null) spinner.setVisible(disabled);
    }
}
