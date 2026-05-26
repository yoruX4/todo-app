package com.todoapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.todoapp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * Main application entry point.
 * Initialises the JavaFX stage and loads the Login screen first.
 */
public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    /** Shared primary stage so controllers can swap scenes. */
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("TaskFlow — To-Do App");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(620);
        primaryStage.setResizable(true);

        // Load login screen on startup
        loadScene("login");

        primaryStage.show();
        logger.info("Application started successfully.");
    }

    /**
     * Switches the current scene by name.
     *
     * @param sceneName one of: "login", "register", "dashboard", "tasks", "profile"
     */
    public static void loadScene(String sceneName) {
        try {
            String fxmlPath = "/fxml/" + sceneName + ".fxml";
            Parent root = FXMLLoader.load(
                Objects.requireNonNull(MainApp.class.getResource(fxmlPath),
                    "FXML not found: " + fxmlPath)
            );

            // Determine window dimensions based on scene
            double width  = sceneName.equals("login") || sceneName.equals("register") ? 480 : 1100;
            double height = sceneName.equals("login") || sceneName.equals("register") ? 620  : 700;

            Scene scene = new Scene(root, width, height);

            // Attach global stylesheet
            scene.getStylesheets().add(
                Objects.requireNonNull(MainApp.class.getResource("/css/styles.css")).toExternalForm()
            );

            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();

        } catch (IOException | NullPointerException e) {
            logger.error("Failed to load scene: {}", sceneName, e);
        }
    }

    /** Returns the primary stage (used by controllers needing dialog owners). */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void stop() {
        // Gracefully close the connection pool on exit
        DatabaseManager.close();
        logger.info("Application stopped. Connection pool closed.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
