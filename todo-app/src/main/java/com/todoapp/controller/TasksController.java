package com.todoapp.controller;

import com.todoapp.MainApp;
import com.todoapp.dao.TaskDAO;
import com.todoapp.model.Task;
import com.todoapp.model.User;
import com.todoapp.util.AlertUtil;
import com.todoapp.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the Task Manager screen (tasks.fxml).
 *
 * Features:
 *   - Display all tasks in a TableView
 *   - Filter by status
 *   - Search by keyword
 *   - Create / Edit / Delete tasks via inline dialog
 *   - Toggle task completion
 */
public class TasksController {

    private static final Logger logger = LoggerFactory.getLogger(TasksController.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // ---- Sidebar navigation ----
    @FXML private void handleGoToDashboard() { MainApp.loadScene("dashboard"); }
    @FXML private void handleGoToTasks()     { /* already here */ }
    @FXML private void handleGoToProfile()   { MainApp.loadScene("profile"); }
    @FXML private void handleLogout() {
        if (AlertUtil.showConfirmation("Logout", "Are you sure you want to log out?")) {
            SessionManager.clearSession();
            MainApp.loadScene("login");
        }
    }

    // ---- Table ----
    @FXML private TableView<Task>               taskTable;
    @FXML private TableColumn<Task, String>     colTitle;
    @FXML private TableColumn<Task, String>     colCategory;
    @FXML private TableColumn<Task, String>     colPriority;
    @FXML private TableColumn<Task, String>     colStatus;
    @FXML private TableColumn<Task, String>     colDueDate;
    @FXML private TableColumn<Task, Void>       colActions;

    // ---- Filter / Search ----
    @FXML private ComboBox<String>  filterCombo;
    @FXML private TextField         searchField;
    @FXML private Label             taskCountLabel;

    private final TaskDAO taskDAO = new TaskDAO();
    private final ObservableList<Task> taskList = FXCollections.observableArrayList();
    private User currentUser;

    // -----------------------------------------------------------------------
    // Initialisation
    // -----------------------------------------------------------------------

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            MainApp.loadScene("login");
            return;
        }

        setupTable();
        setupFilter();
        loadTasks();
    }

    private void setupTable() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colDueDate.setCellValueFactory(data -> {
            LocalDate d = data.getValue().getDueDate();
            return new SimpleStringProperty(d == null ? "—" : d.format(DATE_FMT));
        });

        // Colour-code priority
        colPriority.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                getStyleClass().removeAll("priority-high","priority-medium","priority-low");
                if (!empty && item != null) {
                    getStyleClass().add("priority-" + item.toLowerCase());
                }
            }
        });

        // Colour-code status
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item.replace("_", " "));
                getStyleClass().removeAll("status-pending","status-in-progress","status-completed");
                getStyleClass().add("status-" + item.toLowerCase().replace("_","-"));
            }
        });

        // Actions column: Edit + Delete buttons
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button toggleBtn = new Button("✓");
            private final HBox box = new HBox(4, toggleBtn, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-edit");
                deleteBtn.getStyleClass().add("btn-delete");
                toggleBtn.getStyleClass().add("btn-toggle");

                editBtn.setOnAction(e -> openTaskDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                toggleBtn.setOnAction(e -> handleToggle(getTableView().getItems().get(getIndex())));
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        taskTable.setItems(taskList);
        taskTable.setPlaceholder(new Label("No tasks found. Click 'New Task' to add one!"));
    }

    private void setupFilter() {
        filterCombo.getItems().addAll("ALL", "PENDING", "IN_PROGRESS", "COMPLETED");
        filterCombo.setValue("ALL");
        filterCombo.setOnAction(e -> applyFilter());

        searchField.textProperty().addListener((obs, old, text) -> applyFilter());
    }

    // -----------------------------------------------------------------------
    // Data loading & filtering
    // -----------------------------------------------------------------------

    private void loadTasks() {
        List<Task> tasks = taskDAO.findAllByUser(currentUser.getId());
        taskList.setAll(tasks);
        updateCount();
    }

    private void applyFilter() {
        String status  = filterCombo.getValue();
        String keyword = searchField.getText().trim().toLowerCase();

        List<Task> all = taskDAO.findAllByUser(currentUser.getId());

        List<Task> filtered = all.stream()
            .filter(t -> "ALL".equals(status) || t.getStatus().equals(status))
            .filter(t -> keyword.isEmpty()
                || t.getTitle().toLowerCase().contains(keyword)
                || (t.getDescription() != null && t.getDescription().toLowerCase().contains(keyword))
                || (t.getCategory() != null && t.getCategory().toLowerCase().contains(keyword)))
            .toList();

        taskList.setAll(filtered);
        updateCount();
    }

    private void updateCount() {
        taskCountLabel.setText(taskList.size() + " task(s)");
    }

    // -----------------------------------------------------------------------
    // CRUD handlers
    // -----------------------------------------------------------------------

    @FXML
    private void handleNewTask() {
        openTaskDialog(null);
    }

    private void handleDelete(Task task) {
        if (AlertUtil.showConfirmation("Delete Task",
                "Delete \"" + task.getTitle() + "\"? This cannot be undone.")) {
            if (taskDAO.deleteTask(task.getId(), currentUser.getId())) {
                loadTasks();
                logger.info("Deleted task id={}", task.getId());
            } else {
                AlertUtil.showError("Error", "Could not delete task.");
            }
        }
    }

    private void handleToggle(Task task) {
        taskDAO.toggleComplete(task.getId(), currentUser.getId());
        loadTasks();
    }

    // -----------------------------------------------------------------------
    // Task dialog (Create / Edit)
    // -----------------------------------------------------------------------

    /**
     * Opens a modal dialog for creating or editing a task.
     *
     * @param task null → create mode; non-null → edit mode
     */
    private void openTaskDialog(Task task) {
        boolean isEdit = task != null;
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(MainApp.getPrimaryStage());
        dialog.setTitle(isEdit ? "Edit Task" : "New Task");
        dialog.setResizable(false);

        // ---- Form fields ----
        TextField titleField       = new TextField(isEdit ? task.getTitle() : "");
        TextArea  descArea         = new TextArea(isEdit && task.getDescription() != null ? task.getDescription() : "");
        TextField categoryField    = new TextField(isEdit && task.getCategory() != null ? task.getCategory() : "");
        ComboBox<String> priorityCB = new ComboBox<>();
        ComboBox<String> statusCB   = new ComboBox<>();
        DatePicker duePicker        = new DatePicker(isEdit ? task.getDueDate() : null);

        priorityCB.getItems().addAll(Task.PRIORITY_LOW, Task.PRIORITY_MEDIUM, Task.PRIORITY_HIGH);
        priorityCB.setValue(isEdit ? task.getPriority() : Task.PRIORITY_MEDIUM);

        statusCB.getItems().addAll(Task.STATUS_PENDING, Task.STATUS_IN_PROGRESS, Task.STATUS_COMPLETED);
        statusCB.setValue(isEdit ? task.getStatus() : Task.STATUS_PENDING);

        descArea.setWrapText(true);
        descArea.setPrefRowCount(3);

        titleField.setPromptText("Task title *");
        categoryField.setPromptText("e.g. Work, Personal, Study");

        // ---- Layout ----
        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Title *"),    0, 0); grid.add(titleField,    1, 0);
        grid.add(new Label("Description"),0, 1); grid.add(descArea,      1, 1);
        grid.add(new Label("Category"),   0, 2); grid.add(categoryField, 1, 2);
        grid.add(new Label("Priority"),   0, 3); grid.add(priorityCB,    1, 3);
        grid.add(new Label("Status"),     0, 4); grid.add(statusCB,      1, 4);
        grid.add(new Label("Due Date"),   0, 5); grid.add(duePicker,     1, 5);

        ColumnConstraints col0 = new ColumnConstraints(90);
        ColumnConstraints col1 = new ColumnConstraints(280);
        grid.getColumnConstraints().addAll(col0, col1);

        Label errorLbl = new Label();
        errorLbl.getStyleClass().add("error-label");

        Button saveBtn   = new Button(isEdit ? "Save Changes" : "Create Task");
        Button cancelBtn = new Button("Cancel");
        saveBtn.getStyleClass().add("btn-primary");
        cancelBtn.getStyleClass().add("btn-secondary");

        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        buttons.setPadding(new Insets(0, 0, 10, 20));

        VBox root = new VBox(grid, errorLbl, buttons);
        root.setSpacing(4);

        // ---- Actions ----
        cancelBtn.setOnAction(e -> dialog.close());

        saveBtn.setOnAction(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                errorLbl.setText("Title is required.");
                return;
            }

            if (isEdit) {
                task.setTitle(title);
                task.setDescription(descArea.getText().trim());
                task.setCategory(categoryField.getText().trim());
                task.setPriority(priorityCB.getValue());
                task.setStatus(statusCB.getValue());
                task.setDueDate(duePicker.getValue());

                if (taskDAO.updateTask(task)) {
                    dialog.close();
                    loadTasks();
                } else {
                    errorLbl.setText("Update failed. Please try again.");
                }
            } else {
                Task newTask = new Task(
                    currentUser.getId(),
                    title,
                    descArea.getText().trim(),
                    priorityCB.getValue(),
                    categoryField.getText().trim(),
                    duePicker.getValue()
                );
                newTask.setStatus(statusCB.getValue());

                int id = taskDAO.createTask(newTask);
                if (id > 0) {
                    dialog.close();
                    loadTasks();
                } else {
                    errorLbl.setText("Failed to create task. Please try again.");
                }
            }
        });

        Scene scene = new Scene(root, 440, 400);
        scene.getStylesheets().add(
            getClass().getResource("/css/styles.css").toExternalForm()
        );
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
