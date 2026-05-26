package com.todoapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a to-do task belonging to a {@link User}.
 */
public class Task {

    /** Possible status values – kept as constants to avoid typos. */
    public static final String STATUS_PENDING     = "PENDING";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED   = "COMPLETED";

    /** Priority levels. */
    public static final String PRIORITY_LOW    = "LOW";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_HIGH   = "HIGH";

    private int id;
    private int userId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String category;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    public Task() {
        this.status   = STATUS_PENDING;
        this.priority = PRIORITY_MEDIUM;
    }

    public Task(int userId, String title, String description,
                String priority, String category, LocalDate dueDate) {
        this.userId      = userId;
        this.title       = title;
        this.description = description;
        this.status      = STATUS_PENDING;
        this.priority    = priority;
        this.category    = category;
        this.dueDate     = dueDate;
    }

    // -----------------------------------------------------------------------
    // Convenience helpers
    // -----------------------------------------------------------------------

    public boolean isCompleted()   { return STATUS_COMPLETED.equals(status); }
    public boolean isOverdue()     { return dueDate != null && !isCompleted() && dueDate.isBefore(LocalDate.now()); }

    // -----------------------------------------------------------------------
    // Getters & Setters
    // -----------------------------------------------------------------------

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getUserId()                      { return userId; }
    public void setUserId(int userId)           { this.userId = userId; }

    public String getTitle()                    { return title; }
    public void setTitle(String title)          { this.title = title; }

    public String getDescription()              { return description; }
    public void setDescription(String d)        { this.description = d; }

    public String getStatus()                   { return status; }
    public void setStatus(String status)        { this.status = status; }

    public String getPriority()                 { return priority; }
    public void setPriority(String priority)    { this.priority = priority; }

    public String getCategory()                 { return category; }
    public void setCategory(String category)    { this.category = category; }

    public LocalDate getDueDate()               { return dueDate; }
    public void setDueDate(LocalDate d)         { this.dueDate = d; }

    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime t)   { this.createdAt = t; }

    public LocalDateTime getUpdatedAt()         { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)   { this.updatedAt = t; }

    @Override
    public String toString() {
        return "Task{id=" + id + ", title='" + title + "', status='" + status + "'}";
    }
}
