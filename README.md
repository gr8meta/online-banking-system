# Online Banking Management System (Enterprise & College Edition)

A complete, production-ready, full-stack **Online Banking Management System** designed for academic evaluation, college presentations, and production reference. Built using **Java 21, Spring Boot 3.3, Spring Security 6 with JWT, MySQL 8, and Bootstrap 5**, featuring a complementary **zero-dependency interactive demo runner** for instantaneous offline execution and evaluation.

---

## Key Features

### 1. Customer Self-Service Portal
* **Instant Onboarding & Account Generation:** Seamless user registration with automated generation of unique 10-digit account numbers and starting balance credit.
* **Stateless JWT Authentication:** Secure login with tamper-proof token storage in browser `localStorage` and automated role authorization.
* **Customer Dashboard:** Real-time balance card, quick action shortcuts, total deposits/withdrawals aggregates, and recent transaction overview.
* **Atomic Fund Transfers:** Inter-account transfers with recipient verification, insufficient fund prevention, and database transaction rollback (`@Transactional` / ACID).
* **Cash Deposit & ATM Withdrawal Simulation:** Self-service balance top-ups and cash debits.
* **Beneficiary Address Book:** Save, view, and manage frequent transfer recipients with name, account number, IFSC code, and nickname.
* **Account Statement & Transaction Ledger:** Full transaction history with search filter, type filter (All / Credit / Debit), and printable transaction receipts.
* **Dispute & Grievance Management:** Lodge complaint tickets and report suspicious debits for priority anti-fraud investigation.
* **Profile & Security Settings:** Personal information updates and secure password change facility.

### 2. Administrator & Branch Staff Portal
* **Live Analytics & Executive Dashboard:** System metrics including total active customers, deposit liquidity, transaction count, aggregate financial volume, and pending ticket alerts.
* **Customer Directory & Account Controls:** Search all registered customers, inspect linked accounts, and freeze / unfreeze delinquent accounts with a single toggle.
* **Centralized Transaction Ledger:** Real-time monitoring of all transfers, deposits, and cash debits across the bank.
* **Complaint Redressal:** Review customer complaints, submit official branch responses, and resolve tickets.
* **Anti-Fraud Transaction Block Desk:** Review customer dispute requests and take approval or rejection action.
* **Security Audit Trail:** Immutable logging of user logins, account state toggles, and financial mutations with IP addresses and timestamps.

---

## Default Demo Credentials

| Portal | Role | Username | Password | Account Number |
| :--- | :--- | :--- | :--- | :--- |
| **Customer Portal** | `ROLE_CUSTOMER` | `rahulverma` | `Password@123` | `1001001001` (Balance: ₹25,000.00) |
| **Customer Portal** | `ROLE_CUSTOMER` | `priyasharma` | `Password@123` | `1002002002` (Balance: ₹15,000.00) |
| **Admin Portal** | `ROLE_ADMIN` | `admin` | `Admin@123` | *N/A (Administrative Privileges)* |

---

## Project Structure

```
online-banking-system/
├── backend/
│   ├── pom.xml                                  # Maven dependencies (Spring Boot, Security, JWT, JPA, MySQL)
│   └── src/main/
│       ├── java/com/bankingsystem/
│       │   ├── BankingSystemApplication.java    # Spring Boot Main Application Class
│       │   ├── config/SecurityConfig.java       # Spring Security 6 Filter Chain & CORS
│       │   ├── controller/                      # REST Endpoints (Auth, Account, Transfer, Admin, etc.)
│       │   ├── dto/                             # Data Transfer Objects (Requests & Responses)
│       │   ├── entity/                          # JPA Entities (Customer, User, Account, Transaction, etc.)
│       │   ├── exception/                       # Global Exception Handling & Error DTOs
│       │   ├── repository/                      # Spring Data JPA Repositories
│       │   ├── security/                        # JWT Utils, UserDetails, AuthTokenFilter
│       │   └── service/                         # Transactional Business Logic Services
│       └── resources/
│           ├── application.properties           # Server port, MySQL datasource, JWT secret
│           └── static/                          # Embedded frontend static bundle
├── database/
│   ├── schema.sql                               # MySQL 8 Normalized Relational DDL
│   ├── data.sql                                 # Seed Admin user credentials
│   └── sample-data.sql                          # Demo customers and initial accounts
├── frontend/
│   ├── css/style.css                            # Modern, responsive Fintech UI design system
│   ├── js/api.js                                # REST API client, JWT handler, route guards
│   └── pages/                                   # 18 Web UI Pages
│       ├── index.html                           # Landing Page & Marketing Portal
│       ├── login.html                           # Customer Login Page
│       ├── admin-login.html                     # Admin Portal Login Page
│       ├── register.html                        # New Customer Registration & Account Opening
│       ├── customer-dashboard.html              # Customer Central Dashboard
│       ├── transfer.html                        # Money Transfer Page
│       ├── deposit.html                         # Cash Deposit Simulation
│       ├── withdraw.html                        # ATM Withdrawal Simulation
│       ├── transactions.html                    # Statement & Printable Receipt View
│       ├── beneficiaries.html                   # Beneficiary Directory
│       ├── profile.html                         # Customer Profile Management
│       ├── change-password.html                 # Security & Password Update
│       ├── complaints.html                      # Grievance Submission
│       ├── transaction-block.html               # Fraud Report / Block Transaction
│       ├── admin-dashboard.html                 # Branch Manager Analytics Portal
│       ├── customers.html                       # Customer Directory & Account Freeze Control
│       ├── admin-transactions.html              # Central Bank Transaction Ledger
│       ├── admin-complaints.html                # Grievance Review & Resolution
│       └── audit-logs.html                      # Security Audit Trail Inspector
├── docs/
│   ├── College_Project_Report.md                # Full academic report, DFDs, ER schema & 25 Viva Q&As
│   ├── Project_Documentation.md                 # Technical specification document
│   └── Online_Banking_API.postman_collection.json # Ready-to-import Postman API collection
├── demo_server.py                               # Instant Zero-Dependency Python Demo Server
└── test_system.py                               # Automated 12-Step E2E Verification Test Suite
```

---

## How to Run the Project

You can run this project in **two ways**:

### Option 1: Instant Plug-and-Play Demo (Recommended for College Presentations)
*Requires ONLY Python 3.10+ (Standard Library). No external MySQL, Maven, or internet downloads needed.*

1. Open your terminal inside `online-banking-system/`.
2. Run the demo server:
   ```bash
   python3 demo_server.py
   ```
3. Open your browser and navigate to:
   ```
   http://localhost:8000/pages/index.html
   ```
4. Log in using `rahulverma` / `Password@123` for customer access or `admin` / `Admin@123` for admin access.

### Option 2: Full Enterprise Spring Boot + MySQL Stack
1. **Set Up MySQL:**
   Just make sure a MySQL 8 server is running and reachable — you do **not** need to run the SQL scripts by hand anymore. On first startup the app creates the schema itself (`spring.jpa.hibernate.ddl-auto=update`) and `DataInitializer` seeds the admin account and two demo customers automatically. `database/schema.sql`, `data.sql`, and `sample-data.sql` are kept only as an optional manual alternative if you'd rather seed the DB yourself with a MySQL client.
2. **Configure Database Connection:**
   Verify credentials in `backend/src/main/resources/application.properties` (defaults to root/root; override with the `DB_USERNAME` / `DB_PASSWORD` environment variables).
3. **Build & Run Backend:**
   ```bash
   cd backend
   mvn install
   mvn spring-boot:run
   ```
   (Note the hyphen — the goal is `spring-boot:run`, not `springboot:run`.)
4. **Access Web Application:**
   Open `http://localhost:8080/` in your web browser — it now redirects straight to `index.html`. (`http://localhost:8080/pages/index.html` still works too.)

---

## Bug Fixes Applied

* **Admin login was failing while Customer login worked.** The `admin` user was only ever created by manually running `database/data.sql`, a file Spring Boot never auto-loads (it lives outside `src/main/resources`) — and its bcrypt password hash didn't actually match `Admin@123` anyway. Fixed by adding `config/DataInitializer.java`, which seeds the admin account (and the two demo customers) on every startup using the app's real `PasswordEncoder`, so the stored hash always matches. `data.sql`/`sample-data.sql` were also corrected with genuine bcrypt hashes for anyone who still wants to seed manually.
* **`http://localhost:8080/` showed a 404 instead of the site.** `index.html` lives at `static/pages/index.html`, one level below where Spring Boot looks for it by default. Fixed by adding `config/WebConfig.java`, which redirects `/` and `/index.html` to `/pages/index.html`.
* **Failed logins returned HTTP 500 instead of 401.** `GlobalExceptionHandler` now catches Spring Security's `AuthenticationException` and returns a clean `401 Unauthorized` with a JSON error body.
* **Console app (`OnlineBankingSystem.java`) rupee symbol (`₹`) printed as `?`** on consoles that don't default to UTF-8 (common on Windows). Fixed by forcing `System.out` to UTF-8 at startup.

---

## Running Automated Verification Tests
Run the test suite to verify all endpoints, authentication, and transaction integrity:
```bash
python3 test_system.py
```
All 12 automated checks validate registration, login, balance queries, deposits, withdrawals, transfers, fraud alerts, admin controls, and audit trails.
