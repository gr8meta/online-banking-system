# Academic Project Report & Technical Specification
## ONLINE BANKING MANAGEMENT SYSTEM
**Degree / Course:** Master / Bachelor of Computer Applications (MCA / BCA)  
**Domain:** Enterprise Web Applications, FinTech & Distributed Systems  
**Academic Year:** 2025–2026  

---

## Abstract
The **Online Banking Management System** is a robust, full-stack enterprise web application engineered to digitize core banking operations, eliminate physical branch overhead, and deliver secure financial services to retail customers and bank administrators. Built on a resilient three-tier architecture utilizing **Java 21 / Spring Boot 3.3** (with an instant plug-and-play Python demo runtime), **Spring Security 6 with JWT**, **MySQL 8 / SQLite3** with strict ACID transactions, and a responsive **Bootstrap 5** frontend, the system facilitates self-service account creation, instant funds transfer, deposit and withdrawal simulations, beneficiary management, fraud flagging, customer grievance redressal, and real-time administrative auditing.

---

## 1. Introduction & Project Scope

### 1.1 Background
Conventional banking systems suffer from logistical bottlenecks, including physical paperwork, queue congestion at branches, delays in intra-bank clearing, and limited accessibility outside business hours. Online banking bridges this gap by providing an always-available, fault-tolerant platform for real-time fund processing and account maintenance.

### 1.2 Objectives
* **Automation:** Automate customer onboarding, account generation, and financial ledger bookkeeping.
* **ACID Compliance:** Ensure Atomicity, Consistency, Isolation, and Durability in all financial balance mutations.
* **Stateless Security:** Implement tamper-evident, encrypted authentication utilizing JSON Web Tokens (JWT) and salted password hashing (BCrypt / PBKDF2).
* **Role-Based Access Control (RBAC):** Provide separated dashboards and capabilities for standard `ROLE_CUSTOMER` and privileged `ROLE_ADMIN` users.
* **Dispute & Grievance Workflow:** Offer an integrated mechanism for reporting unauthorized debits, freezing accounts, and reviewing customer complaints.

### 1.3 Scope of the System
* **In-Scope:**
  * Self-service customer registration with automated 10-digit account generation.
  * Real-time intra-bank fund transfer between accounts with balance verification and concurrency locking.
  * Simulation of ATM/Cash withdrawals and branch deposits.
  * Detailed account statement generation with transaction search and printable receipt view.
  * Beneficiary directory management (CRUD).
  * Customer dispute ticketing (Complaints & Transaction Block requests).
  * Administrative management: customer account freeze/unfreeze, system-wide transaction ledger, complaint resolution, and audit logs.
* **Out-of-Scope (Future Enhancements):**
  * Live inter-bank payment gateway (NEFT / RTGS / UPI) integrations.
  * Real-time SMS / WhatsApp OTP delivery via third-party telecom APIs.
  * AI-based predictive fraud scoring.

---

## 2. System Requirements Specification (SRS)

### 2.1 Hardware Requirements
* **Processor:** 2.0 GHz Dual-Core or higher (Intel Core i3/i5/i7/i9 or AMD Ryzen).
* **RAM:** Minimum 4 GB (8 GB recommended for running IDE and database services).
* **Storage:** 500 MB free hard drive space.
* **Network:** Localhost network interface (127.0.0.1) for offline evaluation, or LAN/WAN for distributed deployment.

### 2.2 Software Requirements
* **Operating System:** Platform-independent (Windows 10/11, macOS, Ubuntu/Debian Linux).
* **Enterprise Stack:**
  * Runtime: Java Development Kit (JDK) 21 LTS or 17 LTS.
  * Framework: Spring Boot 3.3+, Spring Data JPA, Spring Security 6.
  * Database: MySQL Server 8.0+ / MariaDB.
  * Build Tool: Apache Maven 3.9+.
* **Portable Demo Stack (Zero-Dependency Option):**
  * Python 3.10+ (using standard library `http.server`, `sqlite3`, `hashlib`, `hmac`).
* **Client / Browser:** Any modern web browser (Google Chrome 110+, Mozilla Firefox, Microsoft Edge, Safari).

---

## 3. System Architecture & High-Level Design

### 3.1 Architectural Pattern
The application follows the **3-Tier Enterprise Architecture**:
1. **Presentation Tier (Frontend):**
   * Responsive HTML5, CSS3, ES6+ JavaScript, Bootstrap 5.3, Bootstrap Icons.
   * Client-side routing, asynchronous API communication via Fetch API, JWT token injection via HTTP `Authorization: Bearer <token>` headers.
2. **Application / Business Logic Tier (Backend):**
   * REST Controllers (`@RestController`) parsing incoming payloads and validating inputs via Jakarta Validation (`@Valid`).
   * Service Layer (`@Service`) enforcing business rules, account status checks, balance calculations, and `@Transactional` isolation.
   * Spring Security Filter Chain intercepting every request, verifying JWT signatures, and enforcing Role-Based Access Control.
3. **Data Tier (Database):**
   * Relational Schema (MySQL 8 / SQLite3) with Foreign Key constraints (`ON DELETE CASCADE` / `RESTRICT`), B-Tree indexes on account numbers, usernames, and transaction dates.
   * Transaction isolation (`READ COMMITTED` or `SERIALIZABLE`) to eliminate race conditions like dirty reads or double spending.

```
+-------------------------------------------------------------------------+
|                           PRESENTATION LAYER                            |
|  [Customer Dashboard]    [Admin Portal]    [Transfers]    [Statements]  |
+-------------------------------------------------------------------------+
                                    |
                            HTTP / REST (JSON)
                            JWT Authentication
                                    v
+-------------------------------------------------------------------------+
|                         APPLICATION / LOGIC LAYER                       |
|   +-----------------------+     +-----------------------------------+   |
|   |  Security / JWT Filter | --> | Controller Layer (Endpoints)      |   |
|   +-----------------------+     +-----------------------------------+   |
|                                                  |                      |
|                                                  v                      |
|                                 +-----------------------------------+   |
|                                 | Service Layer (Business Logic)    |   |
|                                 | - Balance Verification            |   |
|                                 | - Atomic @Transactional Transfers |   |
|                                 | - Account Lockout Policies        |   |
|                                 +-----------------------------------+   |
+-------------------------------------------------------------------------+
                                    |
                                Hibernate / JDBC
                                    v
+-------------------------------------------------------------------------+
|                                DATA LAYER                               |
|        [Users]  [Customers]  [Accounts]  [Transactions]  [Audit]        |
|                       Relational DB (MySQL / SQLite)                    |
+-------------------------------------------------------------------------+
```

---

## 4. Software Engineering Diagrams & Modeling

### 4.1 Data Flow Diagrams (DFD)

#### DFD Level 0 (Context-Level DFD)
```text
                     +----------------------------------------+
                     |                                        |
 [Customer] -------->|                                        |<-------- [Admin]
  - Registration     |                                        |  - Toggle Account Status
  - Login            |        ONLINE BANKING MANAGEMENT       |  - Resolve Complaints
  - Deposit/Withdraw |                 SYSTEM                 |  - Review Block Requests
  - Fund Transfer    |                                        |  - Inspect Audit Logs
  - View Statement   |                                        |
  - Raise Grievance  |                                        |
                     +----------------------------------------+
```

#### DFD Level 1 (Detailed Subsystems)
```text
[Customer] ---> (1.0 Authentication & Session) ---> [Users Table]
                       |
                       v
         +------------>(2.0 Account Operations) ---> [Accounts Table]
         |             - Deposit / Withdraw
         |             - Check Balance
         |
         +------------>(3.0 Transfer Module) ------> [Transactions Table]
         |             - Sender Debit
         |             - Receiver Credit
         |
         +------------>(4.0 Grievance & Disputes) -> [Complaints Table]
                                                     [Transaction Blocks]
                                                              ^
[Admin] ------> (5.0 Back-Office Oversight) ------------------+
                - Review Ledgers
                - Freeze/Unfreeze Accounts
                - Audit Trails
```

### 4.2 Entity-Relationship (ER) Schema
1. **CUSTOMERS:** `(customer_id [PK], full_name, date_of_birth, gender, email [UQ], mobile [UQ], address, city, state, pin_code, status, created_at)`
2. **USERS:** `(user_id [PK], customer_id [FK], username [UQ], password, role, account_locked, failed_attempts, created_at)`
3. **ACCOUNTS:** `(account_id [PK], customer_id [FK], account_number [UQ], account_type, balance, status, created_at)`
4. **BENEFICIARIES:** `(beneficiary_id [PK], customer_id [FK], beneficiary_name, account_number, ifsc_code, bank_name, nickname, status, created_at)`
5. **TRANSACTIONS:** `(transaction_id [PK], reference_id [UQ], sender_account_id [FK], receiver_account_id [FK], transaction_type, amount, description, status, transaction_date)`
6. **COMPLAINTS:** `(complaint_id [PK], customer_id [FK], transaction_id [FK], subject, description, status, admin_response, created_at, updated_at)`
7. **TRANSACTION_BLOCK_REQUESTS:** `(request_id [PK], customer_id [FK], transaction_id [FK], reason, status, admin_remarks, created_at, reviewed_at)`
8. **AUDIT_LOGS:** `(log_id [PK], user_id [FK], action, description, ip_address, created_at)`

---

## 5. Security & Risk Mitigation Architecture

1. **Password Encryption:** Sensitive user passwords are never stored in plaintext. In Spring Boot, they are hashed using **BCrypt** with adaptive work factor salt rounds. In the portable runner, they use secure cryptographic hashing.
2. **Stateless JWT Tokens:** User authentication issues a signed JSON Web Token with expiration claims (24 hours). The token is verified on every request by `AuthTokenFilter`.
3. **Defense Against Double-Spending (Concurrency):** Fund transfers execute inside an atomic database transaction. If debiting the sender or crediting the receiver fails midway, the entire transaction triggers an immediate rollback.
4. **SQL Injection Prevention:** All SQL queries strictly employ parameterized queries and Spring Data JPA / Hibernate PreparedStatement abstractions.
5. **Role-Based Authorization:** Endpoints under `/api/admin/**` explicitly require `ROLE_ADMIN` authority. Customer endpoints are scoped to the authenticated customer's ID extracted from the token, preventing unauthorized horizontal privilege escalation.
6. **Audit Trail:** Any modifying operation (money transfer, login, account freeze) generates an immutable entry in the `audit_logs` table with timestamp and IP address.

---

## 6. Testing, Quality Assurance & Verification

A comprehensive automated test suite (`test_system.py`) was executed to validate the complete operational lifecycle:
* **Test 1 (Static Serving):** Validated loading of HTML, CSS stylesheets, and JavaScript logic (`200 OK`).
* **Test 2 (Customer Authentication):** Validated token issuance on correct credentials (`rahulverma` / `Password@123`).
* **Test 3 (Dashboard Overview):** Verified retrieval of customer profile, linked account number (`1001001001`), and live balance.
* **Test 4 (Cash Deposit):** Verified balance increment from ₹25,000.00 to ₹30,000.00 upon deposit of ₹5,000.00.
* **Test 5 (Inter-Account Transfer):** Executed ₹2,500.00 transfer to recipient account `1002002002` with reference ID tracking.
* **Test 6 (Cash Withdrawal):** Simulated ATM debit of ₹1,000.00, verifying remaining balance of ₹26,500.00.
* **Test 7 (Negative Balance Guard):** Attempted an illegal transfer of ₹99,999,999.00; system successfully rejected with HTTP `400 Bad Request` and zero state corruption.
* **Test 8 (Beneficiary Directory):** Verified listing and retrieval of saved transfer beneficiaries.
* **Test 9 (Customer Registration):** Executed end-to-end registration of a new user, verifying automated generation of unique account number and welcome balance.
* **Test 10 (Admin Authentication):** Verified `ROLE_ADMIN` login (`admin` / `Admin@123`).
* **Test 11 (Admin Analytics):** Verified aggregate customer counts, total system liquidity (deposits), and overall transaction volume.
* **Test 12 (Audit Log Trail):** Confirmed real-time logging of user logins, transfers, and registrations.

**Outcome:** 12 out of 12 automated test suites passed with 100% success.

---

## 7. Viva Voce Guide & FAQ (Top 25 College Interview Questions)

### Q1: What is the architecture of your Online Banking System?
**Answer:** The project follows a classical 3-tier enterprise architecture consisting of a Presentation Layer (Bootstrap 5, HTML5, Vanilla JavaScript SPA), a Business Logic Layer (Spring Boot REST APIs with Spring Security and JWT), and a Data Persistence Layer (MySQL 8 / SQLite with JPA/Hibernate and strict relational integrity).

### Q2: How do you prevent the "Double Spending" problem during fund transfers?
**Answer:** We enforce ACID transactions using `@Transactional` in Spring Boot and `BEGIN IMMEDIATE TRANSACTION` with explicit balance verification before debiting. The sender's balance deduction, recipient's balance addition, and transaction logging occur in an all-or-nothing atomic unit. If any step fails or an exception occurs, the transaction automatically rolls back.

### Q3: Why did you choose JWT over traditional server-side HTTP sessions?
**Answer:** JSON Web Tokens (JWT) enable stateless authentication. The server does not need to maintain an in-memory session table or sticky sessions across multiple load-balanced instances. The client stores the signed token and presents it in the `Authorization: Bearer <token>` header on each request, making the system scalable and RESTful.

### Q4: How is password security managed in the database?
**Answer:** Passwords are never stored in plaintext. They are hashed using **BCrypt** with an automatic random salt and 10 rounds of computation factor. BCrypt is specifically designed to be slow and resistant to brute-force and rainbow-table attacks.

### Q5: What is the difference between Customer and Admin roles in your system?
**Answer:** The system implements Role-Based Access Control (RBAC). A Customer (`ROLE_CUSTOMER`) can only view their own account balance, execute transfers, view their personal transaction statement, manage personal beneficiaries, and submit dispute tickets. An Admin (`ROLE_ADMIN`) has branch-wide oversight: viewing all accounts, freezing/unfreezing delinquent accounts, resolving complaints, reviewing transaction blocks, and inspecting security audit logs.

### Q6: What happens if a user enters an invalid recipient account number during transfer?
**Answer:** The backend verifies whether the recipient account exists and is in an `ACTIVE` state. If the account number is invalid or inactive, the transaction aborts with an informative `400 Bad Request` error message, and no funds are deducted from the sender.

### Q7: Can a customer transfer money to their own account?
**Answer:** No. The transfer logic explicitly validates that `sender_account_id != receiver_account_id`. If they match, the request is rejected with a validation error.

### Q8: What database constraints are used to ensure data integrity?
**Answer:** 
* `PRIMARY KEY` on IDs with auto-increment.
* `UNIQUE` constraints on `username`, `email`, `mobile`, `account_number`, and `reference_id`.
* `FOREIGN KEY` constraints linking accounts to customers, transactions to accounts, and complaints to customers.
* `CHECK` / status enumerations (`ACTIVE`, `BLOCKED`, `DEACTIVATED`).

### Q9: How is the transaction reference ID formatted?
**Answer:** It uses an alphanumeric format combining the transaction date, timestamp, and a random cryptographic suffix (e.g., `TXN20260911...`), ensuring uniqueness across millions of transactions.

### Q10: How do you prevent SQL Injection attacks?
**Answer:** All database interactions use parameterized queries through Spring Data JPA / Hibernate PreparedStatement wrappers and parameterized SQLite statements. Raw user inputs are never concatenated directly into SQL query strings.

### Q11: What is an Audit Log, and why is it necessary in banking?
**Answer:** An audit log is an immutable security trail recording critical events (user login, money transfers, account status changes, registration) along with the user ID, timestamp, and client IP address. In banking systems, it is legally required for non-repudiation, fraud investigations, and regulatory compliance.

### Q12: How does the system handle concurrent transfers from the same account?
**Answer:** Through database row-level locking. When a transfer begins, the sender's account row is locked during balance verification and update, preventing another simultaneous transaction from reading an outdated balance.

### Q13: What is the purpose of the Beneficiary management module?
**Answer:** It allows customers to save trusted counterparty account numbers, IFSC codes, and nicknames. This avoids manual re-entry errors during frequent money transfers and provides a seamless banking experience.

### Q14: How does the grievance / complaint system work?
**Answer:** Customers can lodge a complaint referencing a specific transaction ID or general service issue. The complaint status defaults to `OPEN`. Bank administrators review the complaint in their portal, enter resolution remarks, and update the status to `RESOLVED` or `REJECTED`.

### Q15: What is a Transaction Block Request?
**Answer:** If a customer notices an unrecognized or potentially fraudulent debit on their statement, they can submit an emergency Transaction Block / Dispute request. This alerts the fraud detection unit for priority review and provisional credit.

### Q16: How are transactions categorized on the account statement?
**Answer:** Each transaction has a `transaction_type` (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER`). On the customer statement, transactions where the customer is the receiver are flagged as `Credit (+)` in green, while transactions where the customer is the sender are flagged as `Debit (-)` in red.

### Q17: What happens when an account is frozen by the admin?
**Answer:** When an account status is updated to `BLOCKED` or `SUSPENDED`, all subsequent outgoing transfers, deposits, and cash withdrawals are immediately rejected with an error message directing the customer to contact the branch.

### Q18: What front-end technologies were used and why?
**Answer:** HTML5, CSS3, JavaScript (ES6+), and Bootstrap 5.3. Bootstrap was selected for its clean, responsive grid layout and modern card components. Vanilla JavaScript was used without heavy frontend frameworks (like Angular/React) to keep the project easy to understand, transparent for college viva evaluation, and lightweight without dependency overhead.

### Q19: How can the project be demonstrated if the examiner's laptop doesn't have MySQL or Maven installed?
**Answer:** The project includes a dedicated, zero-dependency Python demo server (`python3 demo_server.py`). It automatically initializes an in-memory SQLite database pre-seeded with admin and customer test accounts, exposes identical REST APIs, and serves the entire responsive web portal on `http://localhost:8000`.

### Q20: How do you validate customer input on the server side?
**Answer:** In Spring Boot, we use Jakarta Bean Validation annotations such as `@NotNull`, `@NotBlank`, `@Email`, `@Size(min=...)`, and `@Positive`. In the demo server, explicit regex and boundary checks validate emails, phone numbers, and transfer amounts before executing SQL.

### Q21: What HTTP methods are utilized in the REST API?
**Answer:** 
* `GET`: For retrieving balances, profiles, statements, and dashboard metrics.
* `POST`: For creating resources (user registration, login, transfers, deposits, withdrawals, complaints).
* `PUT`: For updating resources (profile updates, resolving complaints, toggling account statuses).
* `DELETE`: For deleting resources (removing a beneficiary).

### Q22: What are the standard HTTP status codes returned by your APIs?
**Answer:** 
* `200 OK`: Successful retrieval or update.
* `400 Bad Request`: Validation failure or insufficient balance.
* `401 Unauthorized`: Missing or invalid JWT authentication token.
* `403 Forbidden`: Insufficient role privileges (e.g., customer trying to access admin endpoints).
* `404 Not Found`: Requested customer, account, or resource does not exist.

### Q23: How would you scale this system to handle millions of users?
**Answer:** 
1. Database read/write splitting with master-replica replication.
2. Caching frequently read data (like account summary) using Redis.
3. Message queues (Kafka / RabbitMQ) for asynchronous notification and statement generation.
4. Containerization with Docker and Kubernetes for auto-scaling backend pods.

### Q24: What is the role of the IFSC code in the system?
**Answer:** The Indian Financial System Code (IFSC) is an 11-character alphanumeric code that uniquely identifies a specific bank branch participating in electronic funds transfer. We use `APEX0001089` as the primary branch code.

### Q25: What was the biggest technical challenge faced while developing this project?
**Answer:** Ensuring absolute consistency and atomicity during inter-account transfers under edge cases, such as network drops or concurrent debit attempts, and designing a clean, intuitive UI that reflects real-time ledger updates without requiring manual full-page reloads.

---

## 8. Conclusion
The **Online Banking Management System** successfully fulfills all academic and functional requirements for a comprehensive college software engineering project. It showcases enterprise-grade architectural design, resilient security safeguards, clean code structure, and a polished user interface.
