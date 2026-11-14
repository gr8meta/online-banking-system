USE online_banking_db;

-- Sample Customer 1
INSERT INTO customers (customer_id, full_name, date_of_birth, gender, email, mobile, address, city, state, pin_code, status)
VALUES (1, 'Rahul Verma', '1998-05-15', 'Male', 'rahul.verma@example.com', '9876543210', '123 Tech Park, Sector 62', 'Noida', 'Uttar Pradesh', '201301', 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name=full_name;

-- Sample Customer 1 Login (Password: Password@123)
INSERT INTO users (user_id, customer_id, username, password, role, account_locked, failed_attempts)
VALUES (2, 1, 'rahulverma', '$2b$10$Dz9uzH9pamhUIENGPnZTOOF7jfbIxmQyBTzI7nPKE8xw6d3GwzeIW', 'ROLE_CUSTOMER', FALSE, 0)
ON DUPLICATE KEY UPDATE username=username;

-- Sample Account 1
INSERT INTO accounts (account_id, customer_id, account_number, account_type, balance, status)
VALUES (1, 1, '1001001001', 'SAVINGS', 25000.00, 'ACTIVE')
ON DUPLICATE KEY UPDATE balance=balance;

-- Sample Customer 2
INSERT INTO customers (customer_id, full_name, date_of_birth, gender, email, mobile, address, city, state, pin_code, status)
VALUES (2, 'Priya Sharma', '2000-08-20', 'Female', 'priya.sharma@example.com', '9811223344', '45 Green Avenue, Connaught Place', 'New Delhi', 'Delhi', '110001', 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name=full_name;

-- Sample Customer 2 Login (Password: Password@123)
INSERT INTO users (user_id, customer_id, username, password, role, account_locked, failed_attempts)
VALUES (3, 2, 'priyasharma', '$2b$10$Dz9uzH9pamhUIENGPnZTOOF7jfbIxmQyBTzI7nPKE8xw6d3GwzeIW', 'ROLE_CUSTOMER', FALSE, 0)
ON DUPLICATE KEY UPDATE username=username;

-- Sample Account 2
INSERT INTO accounts (account_id, customer_id, account_number, account_type, balance, status)
VALUES (2, 2, '1002002002', 'SAVINGS', 15000.00, 'ACTIVE')
ON DUPLICATE KEY UPDATE balance=balance;
