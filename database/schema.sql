CREATE DATABASE IF NOT EXISTS online_banking_db;
USE online_banking_db;

-- 1. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    mobile VARCHAR(15) NOT NULL UNIQUE,
    address TEXT NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    pin_code VARCHAR(10) NOT NULL,
    status ENUM('ACTIVE', 'BLOCKED', 'DEACTIVATED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_customer_email (email),
    INDEX idx_customer_status (status)
) ENGINE=InnoDB;

-- 2. Users / Authentication Table
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ROLE_CUSTOMER', 'ROLE_ADMIN') NOT NULL,
    account_locked BOOLEAN DEFAULT FALSE,
    failed_attempts INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    INDEX idx_user_username (username)
) ENGINE=InnoDB;

-- 3. Bank Accounts Table
CREATE TABLE IF NOT EXISTS accounts (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type ENUM('SAVINGS', 'CURRENT', 'SALARY') DEFAULT 'SAVINGS',
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status ENUM('ACTIVE', 'SUSPENDED', 'CLOSED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT,
    INDEX idx_account_num (account_number)
) ENGINE=InnoDB;

-- 4. Beneficiaries Table
CREATE TABLE IF NOT EXISTS beneficiaries (
    beneficiary_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    beneficiary_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    ifsc_code VARCHAR(15) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    UNIQUE KEY uk_cust_beneficiary (customer_id, account_number)
) ENGINE=InnoDB;

-- 5. Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_id VARCHAR(36) NOT NULL UNIQUE,
    sender_account_id BIGINT NULL,
    receiver_account_id BIGINT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER') NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    description VARCHAR(255),
    status ENUM('SUCCESS', 'FAILED', 'PENDING', 'BLOCKED') DEFAULT 'SUCCESS',
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_account_id) REFERENCES accounts(account_id),
    FOREIGN KEY (receiver_account_id) REFERENCES accounts(account_id),
    INDEX idx_trx_date (transaction_date),
    INDEX idx_trx_status (status)
) ENGINE=InnoDB;

-- 6. Complaints Table
CREATE TABLE IF NOT EXISTS complaints (
    complaint_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    transaction_id BIGINT NULL,
    subject VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'REJECTED') DEFAULT 'OPEN',
    admin_response TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 7. Transaction Block Requests Table
CREATE TABLE IF NOT EXISTS transaction_block_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    admin_remarks TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id)
) ENGINE=InnoDB;

-- 8. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    action VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
