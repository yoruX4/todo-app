-- =========================================================
--  TaskFlow – Database Creation Script
--  Database: MySQL 8.0+
--
--  Run this FIRST to create the schema.
--  Usage:  mysql -u root -p < 01_create_database.sql
-- =========================================================

-- Create the database
CREATE DATABASE IF NOT EXISTS todoapp
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE todoapp;

-- ---- users table ----
CREATE TABLE IF NOT EXISTS users (
    id            INT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,   -- BCrypt hash ONLY
    full_name     VARCHAR(100) NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_username (username),
    UNIQUE KEY uq_email    (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---- tasks table ----
CREATE TABLE IF NOT EXISTS tasks (
    id          INT          NOT NULL AUTO_INCREMENT,
    user_id     INT          NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      ENUM('PENDING','IN_PROGRESS','COMPLETED') NOT NULL DEFAULT 'PENDING',
    priority    ENUM('LOW','MEDIUM','HIGH')               NOT NULL DEFAULT 'MEDIUM',
    category    VARCHAR(100),
    due_date    DATE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE    -- deleting a user removes all their tasks
        ON UPDATE CASCADE,

    INDEX idx_tasks_user_id (user_id),
    INDEX idx_tasks_status  (status),
    INDEX idx_tasks_due_date(due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Confirm
SELECT 'Database and tables created successfully.' AS result;
