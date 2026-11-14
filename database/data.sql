USE online_banking_db;

-- 1. Default Admin Credentials (Password: Admin@123)
-- NOTE: this hash is a genuine BCrypt hash of "Admin@123" (verified with bcrypt).
-- The app itself no longer depends on this file - see DataInitializer.java,
-- which seeds the same admin account automatically on startup. This script
-- is kept for anyone who wants to seed the database directly with MySQL.
INSERT INTO users (username, password, role, account_locked, failed_attempts) 
VALUES ('admin', '$2b$10$RdZym/FOSwHflWmLpKJk3ObItwa.wj/eQuKvZ.asCBkVSuFfOqclW', 'ROLE_ADMIN', FALSE, 0)
ON DUPLICATE KEY UPDATE username=username;
