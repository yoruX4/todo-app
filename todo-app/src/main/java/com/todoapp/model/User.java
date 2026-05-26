package com.todoapp.model;

import java.time.LocalDateTime;

/**
 * Represents an application user.
 * The {@code passwordHash} field stores the BCrypt hash – never the plain password.
 */
public class User {

    private int id;
    private String username;
    private String email;
    private String passwordHash; // BCrypt hash only – NEVER plain text
    private String fullName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    public User() {}

    public User(int id, String username, String email, String fullName,
                String passwordHash, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id           = id;
        this.username     = username;
        this.email        = email;
        this.fullName     = fullName;
        this.passwordHash = passwordHash;
        this.createdAt    = createdAt;
        this.updatedAt    = updatedAt;
    }

    // -----------------------------------------------------------------------
    // Getters & Setters
    // -----------------------------------------------------------------------

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public String getUsername()             { return username; }
    public void setUsername(String u)       { this.username = u; }

    public String getEmail()                { return email; }
    public void setEmail(String e)          { this.email = e; }

    public String getPasswordHash()         { return passwordHash; }
    public void setPasswordHash(String h)   { this.passwordHash = h; }

    public String getFullName()             { return fullName; }
    public void setFullName(String n)       { this.fullName = n; }

    public LocalDateTime getCreatedAt()     { return createdAt; }
    public void setCreatedAt(LocalDateTime t){ this.createdAt = t; }

    public LocalDateTime getUpdatedAt()     { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t){ this.updatedAt = t; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}
