-- =========================================================
--  TaskFlow – Database Creation Script
--  Database: PostgreSQL 18
--
--  INSTRUCTIONS:
--  1. In pgAdmin, right-click "todoapp" database → Query Tool
--  2. Paste this entire script and click the ⚡ Run button
-- =========================================================

-- ---- users table ----
CREATE TABLE IF NOT EXISTS users (
    id            SERIAL       PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ---- tasks table ----
CREATE TABLE IF NOT EXISTS tasks (
    id          SERIAL       PRIMARY KEY,
    user_id     INT          NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                             CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED')),
    priority    VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM'
                             CHECK (priority IN ('LOW','MEDIUM','HIGH')),
    category    VARCHAR(100),
    due_date    DATE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ---- Indexes ----
CREATE INDEX IF NOT EXISTS idx_tasks_user_id  ON tasks (user_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status   ON tasks (status);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks (due_date);

-- Confirm
SELECT 'Database and tables created successfully.' AS result;
