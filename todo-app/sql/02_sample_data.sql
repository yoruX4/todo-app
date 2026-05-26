-- =========================================================
--  TaskFlow – Sample Data Script
--
--  Run AFTER 01_create_database.sql.
--  Usage:  mysql -u root -p todoapp < 02_sample_data.sql
--
--  NOTE: Password hashes below are BCrypt hashes of:
--    alice   → "password123"
--    bob     → "mypassword"
--    charlie → "charlie123"
--
--  Generate fresh hashes with:
--    https://bcrypt-generator.com  (cost factor 12)
-- =========================================================

USE todoapp;

-- ---- Sample users ----
-- IMPORTANT: These are BCrypt hashes (cost=12). The plain-text
-- passwords are documented here for demo purposes ONLY and must
-- NEVER appear in production data.

INSERT INTO users (username, email, password_hash, full_name) VALUES
(
    'alice',
    'alice@example.com',
    -- plain: password123
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBdXIG9/Zf.Zam',
    'Alice Johnson'
),
(
    'bob',
    'bob@example.com',
    -- plain: mypassword
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Bob Smith'
),
(
    'charlie',
    'charlie@example.com',
    -- plain: charlie123
    '$2a$12$8N1JyMMQg0BjDH7VWWQw2OFqrMlLfN.eN5K9IdXtBm0JrQ0kXR0Vy',
    'Charlie Davis'
);

-- ---- Sample tasks for alice (id=1) ----
INSERT INTO tasks (user_id, title, description, status, priority, category, due_date) VALUES
(1, 'Set up project repository',
    'Initialise Git repo, add README and .gitignore',
    'COMPLETED', 'HIGH', 'Work', CURDATE() - INTERVAL 5 DAY),

(1, 'Design database schema',
    'Create ERD and write SQL creation scripts for users and tasks tables',
    'COMPLETED', 'HIGH', 'Work', CURDATE() - INTERVAL 3 DAY),

(1, 'Implement login screen',
    'Build JavaFX login UI with username/password validation',
    'IN_PROGRESS', 'HIGH', 'Work', CURDATE() + INTERVAL 2 DAY),

(1, 'Write unit tests',
    'Cover DAO layer and password utility with JUnit tests',
    'PENDING', 'MEDIUM', 'Work', CURDATE() + INTERVAL 7 DAY),

(1, 'Buy groceries',
    'Milk, eggs, bread, coffee, fruit',
    'PENDING', 'LOW', 'Personal', CURDATE() + INTERVAL 1 DAY),

(1, 'Read "Clean Code"',
    'Finish chapters 5-8 this week',
    'IN_PROGRESS', 'MEDIUM', 'Study', CURDATE() + INTERVAL 4 DAY),

(1, 'Overdue report',
    'Monthly expense report – already past due!',
    'PENDING', 'HIGH', 'Work', CURDATE() - INTERVAL 2 DAY);

-- ---- Sample tasks for bob (id=2) ----
INSERT INTO tasks (user_id, title, description, status, priority, category, due_date) VALUES
(2, 'Morning workout',
    '30 min cardio + stretching',
    'COMPLETED', 'MEDIUM', 'Health', CURDATE()),

(2, 'Prepare presentation',
    'Q3 results deck for Thursday board meeting',
    'IN_PROGRESS', 'HIGH', 'Work', CURDATE() + INTERVAL 3 DAY),

(2, 'Call dentist',
    'Schedule annual check-up',
    'PENDING', 'LOW', 'Personal', CURDATE() + INTERVAL 10 DAY);

-- ---- Sample tasks for charlie (id=3) ----
INSERT INTO tasks (user_id, title, description, status, priority, category, due_date) VALUES
(3, 'Learn JavaFX',
    'Complete online tutorial series on JavaFX 21',
    'IN_PROGRESS', 'HIGH', 'Study', CURDATE() + INTERVAL 14 DAY),

(3, 'Fix bug #42',
    'NullPointerException in TaskDAO.findById when result set is empty',
    'PENDING', 'HIGH', 'Work', CURDATE() + INTERVAL 1 DAY),

(3, 'Team lunch',
    'Book restaurant for Friday team outing',
    'PENDING', 'LOW', 'Personal', CURDATE() + INTERVAL 5 DAY);

SELECT CONCAT('Inserted ', COUNT(*), ' sample tasks.') AS result FROM tasks;
SELECT CONCAT('Inserted ', COUNT(*), ' sample users.') AS result FROM users;
