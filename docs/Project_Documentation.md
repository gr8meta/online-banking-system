# Complete Project Documentation: Online Banking Management System

## 1. Project Title
**Online Banking Management System** (Full-Stack Enterprise Edition)

---

## 2. Executive Summary & Problem Statement
Traditional brick-and-mortar banking operations involve significant manual overhead, delays in transaction processing, physical queues, and operational inefficiencies. This project presents a modern, secure, full-stack Online Banking Management System developed using Java 21, Spring Boot 3.x, Spring Security 6, JWT, and MySQL 8. The application delivers real-time account management, atomic fund transfers, fraud detection and transaction locking, beneficiary address books, and administrative oversight with data analytics.

---

## 3. Technology Stack & Architectural Specifications
* **Backend Framework:** Spring Boot 3.3.3 (Java 21 LTS)
* **Security & Auth:** Spring Security 6, BCrypt Password Encoder, JSON Web Tokens (JJWT 0.12.6)
* **Data Persistence:** Spring Data JPA / Hibernate ORM, MySQL Connector/J
* **Database Management:** MySQL 8.0 with InnoDB ACID Transaction Engine
* **Frontend Technologies:** HTML5, CSS3, JavaScript (ES6+), Bootstrap 5.3, Bootstrap Icons, Chart.js
* **Build System:** Apache Maven 3.9+
* **Testing & Tools:** JUnit 5, Mockito, Spring Security Test, Postman

---

## 4. System Architecture & Layered Design
The system adheres to standard enterprise multi-tier architecture:
1. **Presentation Layer (Client):** Responsive web UI interacting through JSON REST APIs using Fetch API.
2. **Controller Layer (Spring REST):** Exposes secure endpoints, enforces `@Valid` annotations, and manages HTTP responses.
3. **Service Layer (Business Logic):** Implements business workflows, validation rules, and `@Transactional` isolation.
4. **Data Access Layer (Spring Data JPA):** Repositories managing entities, native queries, and indexing.
5. **Database Layer (MySQL):** Normalized relational schema with strict foreign keys and ACID guarantees.

---

## 5. Security Architecture
* **Stateless Authentication:** Stateless JWTs signed using HMAC-SHA256.
* **Role-Based Access Control (RBAC):** Distinction between `ROLE_CUSTOMER` and `ROLE_ADMIN`.
* **Credential Security:** Passwords hashed with BCrypt (10 rounds work factor).
* **Brute-Force Lockout:** Automated account lock after 3 failed login attempts.
* **Audit Trail:** Dedicated audit logging for all mutating financial operations and authentication events.

---

## 6. Software Engineering Models

### DFD Level 0 (Context Diagram)
```text
                  +--------------------------------+
                  |                                |
  [Customer] ---->|     ONLINE BANKING SYSTEM      |<---- [Administrator]
                  |                                |
                  +--------------------------------+
```

### DFD Level 1 (Major Processes)
1. Process 1.0: User Registration & JWT Authentication
2. Process 2.0: Account Balance & Ledger Processing
3. Process 3.0: Transaction & Transfer Execution (Atomic `@Transactional`)
4. Process 4.0: Beneficiary Management
5. Process 5.0: Grievance Support & Dispute Resolution
6. Process 6.0: Administrative Oversight & Audit Logging

### Entity-Relationship (ER) Relationships
* **Customer** (1) ─── (1) **User**
* **Customer** (1) ─── (N) **Account**
* **Customer** (1) ─── (N) **Beneficiary**
* **Customer** (1) ─── (N) **Complaint**
* **Account** (1) ─── (N) **Transaction** (Sender / Receiver)
* **Transaction** (1) ─── (1) **TransactionBlockRequest**

---

## 7. Conclusion & Future Enhancements
The Online Banking Management System satisfies all functional and non-functional requirements for an academic MCA/M.Tech capstone project. Future enhancements may include microservices decomposition, multi-factor authentication (TOTP/SMS OTP), and automated AI-driven fraud score analysis.
