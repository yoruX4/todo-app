-- =========================================================
--  TaskFlow – Sample Data Script
--  Database: PostgreSQL 18
--
--  Run AFTER 01_create_database.sql.
--
--  INSTRUCTIONS:
--  1. In pgAdmin, make sure you're in the "todoapp" database
--  2. Paste this entire script and click the ⚡ Run button
--
--  Sample login credentials:
--    alice   → password123
--    bob     → mypassword
--    charlie → charlie123
-- =========================================================

-- ---- Sample users ----
INSERT INTO users (username, email, password_hash, full_name) VALUES
(
    'alice',
    'alice@example.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBdXIG9/Zf.Zam',
    'Alice Johnson'
),
(
    'bob',
    'bob@example.com',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Bob Smith'
),
(
    'charlie',
    'charlie@example.com',
    '$2a$12$8N1JyMMQg0BjDH7VWWQw2OFqrMlLfN.eN5K9IdXtBm0JrQ0kXR0Vy',
    'Charlie Davis'
);

-- ---- Sample tasks for alice ----
INSERT INTO tasks (user_id, title, description, status, priority, category, due_date) VALUES
(1, 'Set up project repository',
    'Initialise Git repo, add README and .gitignore',
    'COMPLETED', 'HIGH', 'Work', CURRENT_DATE - INTERVAL '5 days'),

(1, 'Design database schema',
    'Create ERD and write SQL creation scripts for users and tasks tables',
    'COMPLETED', 'HIGH', 'Work', CURRENT_DATE - INTERVAL '3 days'),

(1, 'Implement login screen',
    'Build JavaFX login UI with username/password validation',
    'IN_PROGRESS', 'HIGH', 'Work', CURRENT_DATE + INTERVAL '2 days'),

(1, 'Write unit tests',
    'Cover DAO layer and password utility with JUnit tests',
    'PENDING', 'MEDIUM', 'Work', CURRENT_DATE + INTERVAL '7 days'),

(1, 'Buy groceries',
    'Milk, eggs, bread, coffee, fruit',
    'PENDING', 'LOW', 'Personal', CURRENT_DATE + INTERVAL '1 day'),

(1, 'Read Clean Code',
    'Finish chapters 5-8 this week',
    'IN_PROGRESS', 'MEDIUM', 'Study', CURRENT_DATE + INTERVAL '4 days'),

(1, 'Overdue report',
    'Monthly expense report - already past due!',
    'PENDING', 'HIGH', 'Work', CURRENT_DATE - INTERVAL '2 days');

-- ---- Sample tasks for bob ----
INSERT INTO tasks (user_id, title, description, status, priority, category, due_date) VALUES
(2, 'Morning workout',
    '30 min cardio + stretching',
    'COMPLETED', 'MEDIUM', 'Health', CURRENT_DATE),

(2, 'Prepare presentation',
    'Q3 results deck for Thursday board meeting',
    'IN_PROGRESS', 'HIGH', 'Work', CURRENT_DATE + INTERVAL '3 days'),

(2, 'Call dentist',
    'Schedule annual check-up',
    'PENDING', 'LOW', 'Personal', CURRENT_DATE + INTERVAL '10 days');

-- ---- Sample tasks for charlie ----
INSERT INTO tasks (user_id, title, description, status, priority, category, due_date) VALUES
(3, 'Learn JavaFX',
    'Complete online tutorial series on JavaFX 21',
    'IN_PROGRESS', 'HIGH', 'Study', CURRENT_DATE + INTERVAL '14 days'),

(3, 'Fix bug #42',
    'NullPointerException in TaskDAO.findById when result set is empty',
    'PENDING', 'HIGH', 'Work', CURRENT_DATE + INTERVAL '1 day'),

(3, 'Team lunch',
    'Book restaurant for Friday team outing',
    'PENDING', 'LOW', 'Personal', CURRENT_DATE + INTERVAL '5 days');

-- Confirm
SELECT CONCAT('Inserted ', COUNT(*), ' sample tasks.') AS result FROM tasks;
SELECT CONCAT('Inserted ', COUNT(*), ' sample users.') AS result FROM users;
