"""
Interactive Zero-Dependency Demo Server for Online Banking Management System
Simulates the Spring Boot REST API & serves frontend static assets.
Allows instant evaluation and presentation without requiring external MySQL or Maven.
"""

import http.server
import socketserver
import json
import sqlite3
import os
import re
import time
import base64
import hmac
import hashlib
import urllib.parse
from datetime import datetime

PORT = int(os.environ.get("PORT", 8000))
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
FRONTEND_DIR = os.path.join(BASE_DIR, "frontend")
DB_FILE = os.environ.get("BANKING_DB", "/tmp/demo_banking.db")
SECRET_KEY = b"online_banking_secure_jwt_secret_key_2026"


# ---------------- Database Initialization ----------------
def get_db():
    conn = sqlite3.connect(DB_FILE)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON")
    return conn


def init_sqlite_db():
    conn = get_db()
    cursor = conn.cursor()

    cursor.executescript("""
    CREATE TABLE IF NOT EXISTS customers (
        customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
        full_name TEXT NOT NULL,
        date_of_birth TEXT NOT NULL,
        gender TEXT NOT NULL,
        email TEXT UNIQUE NOT NULL,
        mobile TEXT UNIQUE NOT NULL,
        address TEXT NOT NULL,
        city TEXT NOT NULL,
        state TEXT NOT NULL,
        pin_code TEXT NOT NULL,
        status TEXT DEFAULT 'ACTIVE',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS users (
        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
        customer_id INTEGER,
        username TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL,
        role TEXT NOT NULL,
        account_locked INTEGER DEFAULT 0,
        failed_attempts INTEGER DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
    );

    CREATE TABLE IF NOT EXISTS accounts (
        account_id INTEGER PRIMARY KEY AUTOINCREMENT,
        customer_id INTEGER NOT NULL,
        account_number TEXT UNIQUE NOT NULL,
        account_type TEXT DEFAULT 'SAVINGS',
        balance REAL DEFAULT 0.00,
        status TEXT DEFAULT 'ACTIVE',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
    );

    CREATE TABLE IF NOT EXISTS beneficiaries (
        beneficiary_id INTEGER PRIMARY KEY AUTOINCREMENT,
        customer_id INTEGER NOT NULL,
        beneficiary_name TEXT NOT NULL,
        account_number TEXT NOT NULL,
        ifsc_code TEXT NOT NULL,
        bank_name TEXT NOT NULL,
        nickname TEXT,
        status TEXT DEFAULT 'ACTIVE',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
    );

    CREATE TABLE IF NOT EXISTS transactions (
        transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
        reference_id TEXT UNIQUE NOT NULL,
        sender_account_id INTEGER,
        receiver_account_id INTEGER,
        transaction_type TEXT NOT NULL,
        amount REAL NOT NULL,
        description TEXT,
        status TEXT DEFAULT 'SUCCESS',
        transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (sender_account_id) REFERENCES accounts(account_id),
        FOREIGN KEY (receiver_account_id) REFERENCES accounts(account_id)
    );

    CREATE TABLE IF NOT EXISTS complaints (
        complaint_id INTEGER PRIMARY KEY AUTOINCREMENT,
        customer_id INTEGER NOT NULL,
        transaction_id INTEGER,
        subject TEXT NOT NULL,
        description TEXT NOT NULL,
        status TEXT DEFAULT 'OPEN',
        admin_response TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
    );

    CREATE TABLE IF NOT EXISTS transaction_block_requests (
        request_id INTEGER PRIMARY KEY AUTOINCREMENT,
        customer_id INTEGER NOT NULL,
        transaction_id INTEGER NOT NULL,
        reason TEXT NOT NULL,
        status TEXT DEFAULT 'PENDING',
        admin_remarks TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        reviewed_at TIMESTAMP,
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
        FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id)
    );

    CREATE TABLE IF NOT EXISTS audit_logs (
        log_id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER,
        action TEXT NOT NULL,
        description TEXT NOT NULL,
        ip_address TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    """)
    conn.commit()

    # Seed Admin
    cursor.execute("SELECT COUNT(*) FROM users WHERE username = 'admin'")
    if cursor.fetchone()[0] == 0:
        cursor.execute("""
        INSERT INTO users (username, password, role, account_locked, failed_attempts)
        VALUES ('admin', 'Admin@123', 'ROLE_ADMIN', 0, 0)
        """)

    # Seed Customer 1: Rahul Verma
    cursor.execute("SELECT COUNT(*) FROM users WHERE username = 'rahulverma'")
    if cursor.fetchone()[0] == 0:
        cursor.execute("""
        INSERT INTO customers (customer_id, full_name, date_of_birth, gender, email, mobile, address, city, state, pin_code, status)
        VALUES (1, 'Rahul Verma', '1998-05-15', 'Male', 'rahul.verma@example.com', '9876543210', '123 Tech Park, Sector 62', 'Noida', 'Uttar Pradesh', '201301', 'ACTIVE')
        """)
        cursor.execute("""
        INSERT INTO users (user_id, customer_id, username, password, role, account_locked, failed_attempts)
        VALUES (2, 1, 'rahulverma', 'Password@123', 'ROLE_CUSTOMER', 0, 0)
        """)
        cursor.execute("""
        INSERT INTO accounts (account_id, customer_id, account_number, account_type, balance, status)
        VALUES (1, 1, '1001001001', 'SAVINGS', 25000.00, 'ACTIVE')
        """)

    # Seed Customer 2: Priya Sharma
    cursor.execute("SELECT COUNT(*) FROM users WHERE username = 'priyasharma'")
    if cursor.fetchone()[0] == 0:
        cursor.execute("""
        INSERT INTO customers (customer_id, full_name, date_of_birth, gender, email, mobile, address, city, state, pin_code, status)
        VALUES (2, 'Priya Sharma', '2000-08-20', 'Female', 'priya.sharma@example.com', '9811223344', '45 Green Avenue, Connaught Place', 'New Delhi', 'Delhi', '110001', 'ACTIVE')
        """)
        cursor.execute("""
        INSERT INTO users (user_id, customer_id, username, password, role, account_locked, failed_attempts)
        VALUES (3, 2, 'priyasharma', 'Password@123', 'ROLE_CUSTOMER', 0, 0)
        """)
        cursor.execute("""
        INSERT INTO accounts (account_id, customer_id, account_number, account_type, balance, status)
        VALUES (2, 2, '1002002002', 'SAVINGS', 15000.00, 'ACTIVE')
        """)

    # Seed Beneficiary for Rahul
    cursor.execute("SELECT COUNT(*) FROM beneficiaries WHERE customer_id = 1")
    if cursor.fetchone()[0] == 0:
        cursor.execute("""
        INSERT INTO beneficiaries (customer_id, beneficiary_name, account_number, ifsc_code, bank_name, nickname)
        VALUES (1, 'Priya Sharma', '1002002002', 'APEX0001089', 'Apex National Bank', 'Priya (Partner)')
        """)

    # Seed Sample Transactions
    cursor.execute("SELECT COUNT(*) FROM transactions")
    if cursor.fetchone()[0] == 0:
        cursor.execute("""
        INSERT INTO transactions (reference_id, sender_account_id, receiver_account_id, transaction_type, amount, description, status, transaction_date)
        VALUES ('TXN20260901001', NULL, 1, 'DEPOSIT', 30000.00, 'Opening Balance & Salary Credit', 'SUCCESS', '2026-09-01 10:00:00')
        """)
        cursor.execute("""
        INSERT INTO transactions (reference_id, sender_account_id, receiver_account_id, transaction_type, amount, description, status, transaction_date)
        VALUES ('TXN20260904002', 1, NULL, 'WITHDRAWAL', 5000.00, 'ATM Cash Withdrawal', 'SUCCESS', '2026-09-04 15:30:00')
        """)
        cursor.execute("""
        INSERT INTO transactions (reference_id, sender_account_id, receiver_account_id, transaction_type, amount, description, status, transaction_date)
        VALUES ('TXN20260905003', NULL, 2, 'DEPOSIT', 15000.00, 'Direct Deposit', 'SUCCESS', '2026-09-05 11:00:00')
        """)

    # Seed Audit Log
    cursor.execute("SELECT COUNT(*) FROM audit_logs")
    if cursor.fetchone()[0] == 0:
        cursor.execute("""
        INSERT INTO audit_logs (user_id, action, description, ip_address)
        VALUES (1, 'SYSTEM_INIT', 'Demo database initialized with master records', '127.0.0.1')
        """)

    conn.commit()
    conn.close()


# ---------------- JWT Authentication Helpers ----------------
def create_jwt(username, role, customer_id):
    header = base64.urlsafe_b64encode(json.dumps({"alg": "HS256", "typ": "JWT"}).encode()).decode().rstrip("=")
    payload_data = {
        "sub": username,
        "role": role,
        "customerId": customer_id,
        "exp": int(time.time()) + 86400
    }
    payload = base64.urlsafe_b64encode(json.dumps(payload_data).encode()).decode().rstrip("=")
    signature = hmac.new(SECRET_KEY, f"{header}.{payload}".encode(), hashlib.sha256).digest()
    sig_b64 = base64.urlsafe_b64encode(signature).decode().rstrip("=")
    return f"{header}.{payload}.{sig_b64}"


def verify_jwt(token):
    try:
        parts = token.split(".")
        if len(parts) != 3:
            return None
        header, payload, sig = parts
        expected_sig = base64.urlsafe_b64encode(
            hmac.new(SECRET_KEY, f"{header}.{payload}".encode(), hashlib.sha256).digest()
        ).decode().rstrip("=")
        if not hmac.compare_digest(sig, expected_sig):
            return None
        padded_payload = payload + "=" * (-len(payload) % 4)
        data = json.loads(base64.urlsafe_b64decode(padded_payload).decode())
        if data.get("exp", 0) < time.time():
            return None
        return data
    except Exception:
        return None


# ---------------- Request Handler ----------------
class BankingRequestHandler(http.server.SimpleHTTPRequestHandler):

    def log_message(self, format, *args):
        # Clean logging
        print(f"[{datetime.now().strftime('%H:%M:%S')}] {self.command} {self.path}")

    def end_headers(self):
        # Enable CORS for cross-origin or local development
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
        super().end_headers()

    def do_OPTIONS(self):
        self.send_response(204)
        self.end_headers()

    def get_auth_user(self):
        auth_header = self.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            return None
        token = auth_header[7:].strip()
        return verify_jwt(token)

    def read_json_body(self):
        content_length = int(self.headers.get("Content-Length", 0))
        if content_length == 0:
            return {}
        body = self.rfile.read(content_length).decode("utf-8")
        return json.loads(body) if body else {}

    def send_json(self, status_code, data):
        response_bytes = json.dumps(data, default=str).encode("utf-8")
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(response_bytes)))
        self.end_headers()
        self.wfile.write(response_bytes)

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path

        # Route API Calls
        if path.startswith("/api/"):
            self.handle_api_get(path, parsed.query)
            return

        # Serve Frontend Static Files
        if path == "/" or path == "/index.html":
            path = "/pages/index.html"

        # Look in frontend folder
        filepath = os.path.normpath(os.path.join(FRONTEND_DIR, path.lstrip("/")))
        if os.path.exists(filepath) and os.path.isfile(filepath):
            return self.serve_static_file(filepath)

        # Fallback to standard handler
        self.send_error(404, f"File Not Found: {path}")

    def serve_static_file(self, filepath):
        ext = os.path.splitext(filepath)[1].lower()
        content_types = {
            ".html": "text/html; charset=utf-8",
            ".css": "text/css; charset=utf-8",
            ".js": "application/javascript; charset=utf-8",
            ".json": "application/json; charset=utf-8",
            ".png": "image/png",
            ".jpg": "image/jpeg",
            ".svg": "image/svg+xml"
        }
        mime = content_types.get(ext, "application/octet-stream")
        try:
            with open(filepath, "rb") as f:
                content = f.read()
            self.send_response(200)
            self.send_header("Content-Type", mime)
            self.send_header("Content-Length", str(len(content)))
            self.end_headers()
            self.wfile.write(content)
        except Exception as e:
            self.send_error(500, str(e))

    def handle_api_get(self, path, query_string):
        user = self.get_auth_user()

        conn = get_db()
        cursor = conn.cursor()

        try:
            # 1. Customer Profile
            if path == "/api/customer/profile":
                if not user or user["role"] != "ROLE_CUSTOMER":
                    return self.send_json(401, {"error": "Unauthorized"})
                cursor.execute("SELECT * FROM customers WHERE customer_id = ?", (user["customerId"],))
                row = cursor.fetchone()
                return self.send_json(200, dict(row) if row else {})

            # 2. Customer Dashboard
            elif path == "/api/customer/dashboard":
                if not user or user["role"] != "ROLE_CUSTOMER":
                    return self.send_json(401, {"error": "Unauthorized"})

                c_id = user["customerId"]
                cursor.execute("SELECT full_name FROM customers WHERE customer_id = ?", (c_id,))
                cust = cursor.fetchone()
                full_name = cust["full_name"] if cust else "Valued Customer"

                cursor.execute("SELECT * FROM accounts WHERE customer_id = ?", (c_id,))
                acc = cursor.fetchone()
                acc_id = acc["account_id"] if acc else None
                acc_no = acc["account_number"] if acc else "N/A"
                balance = acc["balance"] if acc else 0.0
                acc_type = acc["account_type"] if acc else "SAVINGS"

                deposits = 0.0
                withdrawals = 0.0
                recent_tx = []

                if acc_id:
                    cursor.execute("""
                    SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
                    WHERE receiver_account_id = ? AND status = 'SUCCESS'
                    """, (acc_id,))
                    deposits = cursor.fetchone()[0]

                    cursor.execute("""
                    SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
                    WHERE sender_account_id = ? AND status = 'SUCCESS'
                    """, (acc_id,))
                    withdrawals = cursor.fetchone()[0]

                    cursor.execute("""
                    SELECT t.*, 
                           s_acc.account_number as sender_acc_no,
                           r_acc.account_number as receiver_acc_no
                    FROM transactions t
                    LEFT JOIN accounts s_acc ON t.sender_account_id = s_acc.account_id
                    LEFT JOIN accounts r_acc ON t.receiver_account_id = r_acc.account_id
                    WHERE (t.sender_account_id = ? OR t.receiver_account_id = ?)
                    ORDER BY t.transaction_date DESC LIMIT 5
                    """, (acc_id, acc_id))
                    for r in cursor.fetchall():
                        recent_tx.append({
                            "transactionId": r["transaction_id"],
                            "referenceId": r["reference_id"],
                            "transactionType": r["transaction_type"],
                            "amount": r["amount"],
                            "description": r["description"],
                            "status": r["status"],
                            "transactionDate": r["transaction_date"],
                            "senderAccount": r["sender_acc_no"] or "N/A",
                            "receiverAccount": r["receiver_acc_no"] or "N/A"
                        })

                return self.send_json(200, {
                    "fullName": full_name,
                    "accountId": acc_id,
                    "accountNumber": acc_no,
                    "balance": balance,
                    "accountType": acc_type,
                    "totalDeposits": deposits,
                    "totalWithdrawals": withdrawals,
                    "recentTransactions": recent_tx
                })

            # 3. Account Summary
            elif path == "/api/accounts":
                if not user:
                    return self.send_json(401, {"error": "Unauthorized"})
                cursor.execute("SELECT * FROM accounts WHERE customer_id = ?", (user["customerId"],))
                accounts = [dict(r) for r in cursor.fetchall()]
                return self.send_json(200, accounts)

            # 4. Transactions List for an Account
            elif re.match(r"^/api/accounts/\d+/transactions$", path):
                if not user:
                    return self.send_json(401, {"error": "Unauthorized"})
                acc_id = int(path.split("/")[3])
                cursor.execute("""
                SELECT t.*, 
                       s_acc.account_number as sender_acc_no,
                       r_acc.account_number as receiver_acc_no
                FROM transactions t
                LEFT JOIN accounts s_acc ON t.sender_account_id = s_acc.account_id
                LEFT JOIN accounts r_acc ON t.receiver_account_id = r_acc.account_id
                WHERE (t.sender_account_id = ? OR t.receiver_account_id = ?)
                ORDER BY t.transaction_date DESC
                """, (acc_id, acc_id))
                results = []
                for r in cursor.fetchall():
                    results.append({
                        "transactionId": r["transaction_id"],
                        "referenceId": r["reference_id"],
                        "transactionType": r["transaction_type"],
                        "amount": r["amount"],
                        "description": r["description"],
                        "status": r["status"],
                        "transactionDate": r["transaction_date"],
                        "senderAccount": r["sender_acc_no"] or "N/A",
                        "receiverAccount": r["receiver_acc_no"] or "N/A"
                    })
                return self.send_json(200, results)

            # 5. Beneficiaries List
            elif path == "/api/beneficiaries":
                if not user:
                    return self.send_json(401, {"error": "Unauthorized"})
                cursor.execute("SELECT * FROM beneficiaries WHERE customer_id = ? ORDER BY created_at DESC", (user["customerId"],))
                return self.send_json(200, [dict(r) for r in cursor.fetchall()])

            # 6. Customer Complaints List
            elif path == "/api/complaints":
                if not user:
                    return self.send_json(401, {"error": "Unauthorized"})
                cursor.execute("SELECT * FROM complaints WHERE customer_id = ? ORDER BY created_at DESC", (user["customerId"],))
                return self.send_json(200, [dict(r) for r in cursor.fetchall()])

            # 7. Customer Transaction Block Requests
            elif path == "/api/transaction-blocks":
                if not user:
                    return self.send_json(401, {"error": "Unauthorized"})
                cursor.execute("""
                SELECT tb.*, t.reference_id, t.amount, t.description as tx_desc
                FROM transaction_block_requests tb
                JOIN transactions t ON tb.transaction_id = t.transaction_id
                WHERE tb.customer_id = ?
                ORDER BY tb.created_at DESC
                """, (user["customerId"],))
                return self.send_json(200, [dict(r) for r in cursor.fetchall()])

            # 8. Admin Dashboard
            elif path == "/api/admin/dashboard":
                if not user or user["role"] != "ROLE_ADMIN":
                    return self.send_json(403, {"error": "Access Denied"})

                cursor.execute("SELECT COUNT(*) FROM customers WHERE status = 'ACTIVE'")
                active_cust = cursor.fetchone()[0]

                cursor.execute("SELECT COUNT(*) FROM accounts")
                total_acc = cursor.fetchone()[0]

                cursor.execute("SELECT COALESCE(SUM(balance), 0.0) FROM accounts")
                total_dep = cursor.fetchone()[0]

                cursor.execute("SELECT COUNT(*) FROM transactions WHERE status = 'SUCCESS'")
                total_tx = cursor.fetchone()[0]

                cursor.execute("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE status = 'SUCCESS'")
                total_vol = cursor.fetchone()[0]

                cursor.execute("SELECT COUNT(*) FROM complaints WHERE status = 'OPEN'")
                open_comp = cursor.fetchone()[0]

                cursor.execute("SELECT COUNT(*) FROM transaction_block_requests WHERE status = 'PENDING'")
                pending_blocks = cursor.fetchone()[0]

                return self.send_json(200, {
                    "totalCustomers": active_cust,
                    "totalAccounts": total_acc,
                    "totalDeposits": total_dep,
                    "totalTransactions": total_tx,
                    "totalVolume": total_vol,
                    "pendingComplaints": open_comp,
                    "pendingBlocks": pending_blocks
                })

            # 9. Admin Customers Directory
            elif path == "/api/admin/customers":
                if not user or user["role"] != "ROLE_ADMIN":
                    return self.send_json(403, {"error": "Access Denied"})
                cursor.execute("""
                SELECT c.*, a.account_number, a.balance, a.status as acc_status, u.username, u.account_locked
                FROM customers c
                LEFT JOIN accounts a ON c.customer_id = a.customer_id
                LEFT JOIN users u ON c.customer_id = u.customer_id
                ORDER BY c.customer_id ASC
                """)
                return self.send_json(200, [dict(r) for r in cursor.fetchall()])

            # 10. Admin Transactions Directory
            elif path == "/api/admin/transactions":
                if not user or user["role"] != "ROLE_ADMIN":
                    return self.send_json(403, {"error": "Access Denied"})
                cursor.execute("""
                SELECT t.*, 
                       s_acc.account_number as sender_acc_no,
                       r_acc.account_number as receiver_acc_no
                FROM transactions t
                LEFT JOIN accounts s_acc ON t.sender_account_id = s_acc.account_id
                LEFT JOIN accounts r_acc ON t.receiver_account_id = r_acc.account_id
                ORDER BY t.transaction_date DESC LIMIT 100
                """)
                results = []
                for r in cursor.fetchall():
                    results.append({
                        "transactionId": r["transaction_id"],
                        "referenceId": r["reference_id"],
                        "transactionType": r["transaction_type"],
                        "amount": r["amount"],
                        "description": r["description"],
                        "status": r["status"],
                        "transactionDate": r["transaction_date"],
                        "senderAccount": r["sender_acc_no"] or "N/A",
                        "receiverAccount": r["receiver_acc_no"] or "N/A"
                    })
                return self.send_json(200, results)

            # 11. Admin Complaints Directory
            elif path == "/api/admin/complaints":
                if not user or user["role"] != "ROLE_ADMIN":
                    return self.send_json(403, {"error": "Access Denied"})
                cursor.execute("""
                SELECT c.*, cust.full_name as customer_name, cust.email as customer_email, a.account_number
                FROM complaints c
                JOIN customers cust ON c.customer_id = cust.customer_id
                LEFT JOIN accounts a ON cust.customer_id = a.customer_id
                ORDER BY c.created_at DESC
                """)
                return self.send_json(200, [dict(r) for r in cursor.fetchall()])

            # 12. Admin Transaction Blocks
            elif path == "/api/admin/transaction-blocks":
                if not user or user["role"] != "ROLE_ADMIN":
                    return self.send_json(403, {"error": "Access Denied"})
                cursor.execute("""
                SELECT tb.*, c.full_name as customer_name, t.reference_id, t.amount
                FROM transaction_block_requests tb
                JOIN customers c ON tb.customer_id = c.customer_id
                JOIN transactions t ON tb.transaction_id = t.transaction_id
                ORDER BY tb.created_at DESC
                """)
                return self.send_json(200, [dict(r) for r in cursor.fetchall()])

            # 13. Admin Audit Logs
            elif path == "/api/admin/audit-logs":
                if not user or user["role"] != "ROLE_ADMIN":
                    return self.send_json(403, {"error": "Access Denied"})
                cursor.execute("""
                SELECT al.*, u.username, u.role
                FROM audit_logs al
                LEFT JOIN users u ON al.user_id = u.user_id
                ORDER BY al.created_at DESC LIMIT 50
                """)
                return self.send_json(200, [dict(r) for r in cursor.fetchall()])

            else:
                return self.send_json(404, {"error": f"Endpoint not found: {path}"})

        finally:
            conn.close()

    def do_POST(self):
        path = urllib.parse.urlparse(self.path).path
        data = self.read_json_body()
        user = self.get_auth_user()

        conn = get_db()
        cursor = conn.cursor()

        try:
            # 1. Customer / Admin Login
            if path in ["/api/auth/login", "/api/auth/admin/login"]:
                username = data.get("username", "").strip()
                password = data.get("password", "").strip()

                cursor.execute("SELECT * FROM users WHERE username = ?", (username,))
                u = cursor.fetchone()

                if not u or u["password"] != password:
                    return self.send_json(401, {"success": False, "message": "Invalid username or password."})

                if u["account_locked"]:
                    return self.send_json(403, {"success": False, "message": "Account is locked. Please contact branch administrator."})

                # Check role if admin endpoint
                if path == "/api/auth/admin/login" and u["role"] != "ROLE_ADMIN":
                    return self.send_json(403, {"success": False, "message": "Access Denied: Not an administrative account."})

                token = create_jwt(u["username"], u["role"], u["customer_id"])

                cursor.execute("INSERT INTO audit_logs (user_id, action, description, ip_address) VALUES (?, 'LOGIN', ?, '127.0.0.1')",
                               (u["user_id"], f"User {username} logged in successfully"))
                conn.commit()

                return self.send_json(200, {
                    "token": token,
                    "username": u["username"],
                    "role": u["role"],
                    "customerId": u["customer_id"],
                    "message": "Login successful"
                })

            # 2. Customer Registration
            elif path == "/api/auth/register":
                username = data.get("username", "").strip()
                password = data.get("password", "").strip()
                full_name = data.get("fullName", "").strip()
                email = data.get("email", "").strip()
                mobile = data.get("mobile", "").strip()
                dob = data.get("dateOfBirth", "2000-01-01")
                gender = data.get("gender", "Other")
                address = data.get("address", "New Delhi")
                city = data.get("city", "Delhi")
                state = data.get("state", "Delhi")
                pin = data.get("pinCode", "110001")

                # Validate
                cursor.execute("SELECT user_id FROM users WHERE username = ?", (username,))
                if cursor.fetchone():
                    return self.send_json(400, {"success": False, "message": "Username is already taken."})

                cursor.execute("SELECT customer_id FROM customers WHERE email = ? OR mobile = ?", (email, mobile))
                if cursor.fetchone():
                    return self.send_json(400, {"success": False, "message": "Email or Mobile number is already registered."})

                # Insert customer
                cursor.execute("""
                INSERT INTO customers (full_name, date_of_birth, gender, email, mobile, address, city, state, pin_code, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')
                """, (full_name, dob, gender, email, mobile, address, city, state, pin))
                c_id = cursor.lastrowid

                # Insert user
                cursor.execute("""
                INSERT INTO users (customer_id, username, password, role, account_locked, failed_attempts)
                VALUES (?, ?, ?, 'ROLE_CUSTOMER', 0, 0)
                """, (c_id, username, password))
                u_id = cursor.lastrowid

                # Generate account number
                acc_num = f"100{int(time.time() % 9000000 + 1000000)}"
                cursor.execute("""
                INSERT INTO accounts (customer_id, account_number, account_type, balance, status)
                VALUES (?, ?, 'SAVINGS', 5000.00, 'ACTIVE')
                """, (c_id, acc_num))
                acc_id = cursor.lastrowid

                # Opening deposit transaction
                import random; ref_id = f"TXN{datetime.now().strftime('%Y%m%d%H%M%S')}{random.randint(1000, 9999)}"
                cursor.execute("""
                INSERT INTO transactions (reference_id, sender_account_id, receiver_account_id, transaction_type, amount, description, status)
                VALUES (?, NULL, ?, 'DEPOSIT', 5000.00, 'Welcome Account Opening Credit', 'SUCCESS')
                """, (ref_id, acc_id))

                cursor.execute("INSERT INTO audit_logs (user_id, action, description, ip_address) VALUES (?, 'REGISTRATION', ?, '127.0.0.1')",
                               (u_id, f"New customer registered: {full_name}, Account: {acc_num}"))
                conn.commit()

                return self.send_json(200, {
                    "success": True,
                    "message": f"Registration successful! Welcome bonus ₹5,000 credited to Account: {acc_num}",
                    "accountNumber": acc_num
                })

            # 3. Deposit
            elif path == "/api/accounts/deposit":
                if not user or user["role"] != "ROLE_CUSTOMER":
                    return self.send_json(401, {"error": "Unauthorized"})

                amount = float(data.get("amount", 0))
                desc = data.get("description", "Direct Online Deposit")
                if amount <= 0:
                    return self.send_json(400, {"success": False, "message": "Amount must be positive."})

                cursor.execute("SELECT account_id, balance, status FROM accounts WHERE customer_id = ?", (user["customerId"],))
                acc = cursor.fetchone()
                if not acc or acc["status"] != "ACTIVE":
                    return self.send_json(400, {"success": False, "message": "Account is not active."})

                cursor.execute("UPDATE accounts SET balance = balance + ? WHERE account_id = ?", (amount, acc["account_id"]))
                import random; ref_id = f"TXN{datetime.now().strftime('%Y%m%d%H%M%S')}{random.randint(1000, 9999)}"
                cursor.execute("""
                INSERT INTO transactions (reference_id, sender_account_id, receiver_account_id, transaction_type, amount, description, status)
                VALUES (?, NULL, ?, 'DEPOSIT', ?, ?, 'SUCCESS')
                """, (ref_id, acc["account_id"], amount, desc))
                conn.commit()

                return self.send_json(200, {
                    "success": True,
                    "message": f"Successfully deposited ₹{amount:,.2f}",
                    "referenceId": ref_id,
                    "newBalance": acc["balance"] + amount
                })

            # 4. Withdraw
            elif path == "/api/accounts/withdraw":
                if not user or user["role"] != "ROLE_CUSTOMER":
                    return self.send_json(401, {"error": "Unauthorized"})

                amount = float(data.get("amount", 0))
                desc = data.get("description", "ATM Cash Withdrawal")
                if amount <= 0:
                    return self.send_json(400, {"success": False, "message": "Amount must be positive."})

                cursor.execute("SELECT account_id, balance, status FROM accounts WHERE customer_id = ?", (user["customerId"],))
                acc = cursor.fetchone()
                if not acc or acc["status"] != "ACTIVE":
                    return self.send_json(400, {"success": False, "message": "Account is not active."})
                if acc["balance"] < amount:
                    return self.send_json(400, {"success": False, "message": f"Insufficient balance. Current: ₹{acc['balance']:,.2f}"})

                cursor.execute("UPDATE accounts SET balance = balance - ? WHERE account_id = ?", (amount, acc["account_id"]))
                import random; ref_id = f"TXN{datetime.now().strftime('%Y%m%d%H%M%S')}{random.randint(1000, 9999)}"
                cursor.execute("""
                INSERT INTO transactions (reference_id, sender_account_id, receiver_account_id, transaction_type, amount, description, status)
                VALUES (?, ?, NULL, 'WITHDRAWAL', ?, ?, 'SUCCESS')
                """, (ref_id, acc["account_id"], amount, desc))
                conn.commit()

                return self.send_json(200, {
                    "success": True,
                    "message": f"Successfully withdrew ₹{amount:,.2f}",
                    "referenceId": ref_id,
                    "newBalance": acc["balance"] - amount
                })

            # 5. Fund Transfer
            elif path == "/api/transfers":
                if not user or user["role"] != "ROLE_CUSTOMER":
                    return self.send_json(401, {"error": "Unauthorized"})

                target_acc_no = str(data.get("receiverAccountNumber", "")).strip()
                amount = float(data.get("amount", 0))
                desc = data.get("description", "Online Funds Transfer")

                if amount <= 0:
                    return self.send_json(400, {"success": False, "message": "Transfer amount must be positive."})

                # Sender
                cursor.execute("SELECT account_id, account_number, balance, status FROM accounts WHERE customer_id = ?", (user["customerId"],))
                sender = cursor.fetchone()
                if not sender or sender["status"] != "ACTIVE":
                    return self.send_json(400, {"success": False, "message": "Your account is frozen or inactive."})
                if sender["account_number"] == target_acc_no:
                    return self.send_json(400, {"success": False, "message": "Cannot transfer money to yourself."})
                if sender["balance"] < amount:
                    return self.send_json(400, {"success": False, "message": f"Insufficient funds. Balance: ₹{sender['balance']:,.2f}"})

                # Receiver
                cursor.execute("SELECT a.account_id, a.status, c.full_name FROM accounts a JOIN customers c ON a.customer_id = c.customer_id WHERE a.account_number = ?", (target_acc_no,))
                receiver = cursor.fetchone()
                if not receiver:
                    return self.send_json(400, {"success": False, "message": "Recipient account number does not exist."})
                if receiver["status"] != "ACTIVE":
                    return self.send_json(400, {"success": False, "message": "Recipient account is currently inactive."})

                # Execute Atomic Transfer
                cursor.execute("UPDATE accounts SET balance = balance - ? WHERE account_id = ?", (amount, sender["account_id"]))
                cursor.execute("UPDATE accounts SET balance = balance + ? WHERE account_id = ?", (amount, receiver["account_id"]))

                import random; ref_id = f"TXN{datetime.now().strftime('%Y%m%d%H%M%S')}{random.randint(1000, 9999)}"
                cursor.execute("""
                INSERT INTO transactions (reference_id, sender_account_id, receiver_account_id, transaction_type, amount, description, status)
                VALUES (?, ?, ?, 'TRANSFER', ?, ?, 'SUCCESS')
                """, (ref_id, sender["account_id"], receiver["account_id"], amount, desc))

                cursor.execute("INSERT INTO audit_logs (user_id, action, description, ip_address) VALUES (?, 'TRANSFER', ?, '127.0.0.1')",
                               (user["customerId"], f"Transfer of ₹{amount:,.2f} to {receiver['full_name']} ({target_acc_no})"))
                conn.commit()

                return self.send_json(200, {
                    "success": True,
                    "message": f"Transfer of ₹{amount:,.2f} to {receiver['full_name']} completed successfully.",
                    "referenceId": ref_id,
                    "recipientName": receiver["full_name"],
                    "remainingBalance": sender["balance"] - amount
                })

            # 6. Add Beneficiary
            elif path == "/api/beneficiaries":
                if not user or user["role"] != "ROLE_CUSTOMER":
                    return self.send_json(401, {"error": "Unauthorized"})

                name = data.get("beneficiaryName", "").strip()
                acc_num = data.get("accountNumber", "").strip()
                ifsc = data.get("ifscCode", "APEX0001089").strip()
                bank = data.get("bankName", "Apex National Bank").strip()
                nickname = data.get("nickname", "").strip()

                cursor.execute("SELECT beneficiary_id FROM beneficiaries WHERE customer_id = ? AND account_number = ?", (user["customerId"], acc_num))
                if cursor.fetchone():
                    return self.send_json(400, {"success": False, "message": "Beneficiary already added."})

                cursor.execute("""
                INSERT INTO beneficiaries (customer_id, beneficiary_name, account_number, ifsc_code, bank_name, nickname, status)
                VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')
                """, (user["customerId"], name, acc_num, ifsc, bank, nickname))
                conn.commit()

                return self.send_json(200, {"success": True, "message": f"Beneficiary '{name}' added successfully."})

            # 7. Add Complaint / Grievance
            elif path == "/api/complaints":
                if not user or user["role"] != "ROLE_CUSTOMER":
                    return self.send_json(401, {"error": "Unauthorized"})

                subject = data.get("subject", "").strip()
                desc = data.get("description", "").strip()
                tx_id = data.get("transactionId")

                cursor.execute("""
                INSERT INTO complaints (customer_id, transaction_id, subject, description, status)
                VALUES (?, ?, ?, ?, 'OPEN')
                """, (user["customerId"], tx_id, subject, desc))
                conn.commit()

                return self.send_json(200, {"success": True, "message": "Complaint submitted successfully. Ticket ID generated."})

            # 8. Report Fraud / Block Transaction
            elif path == "/api/transaction-blocks":
                if not user or user["role"] != "ROLE_CUSTOMER":
                    return self.send_json(401, {"error": "Unauthorized"})

                tx_id = data.get("transactionId")
                reason = data.get("reason", "").strip()

                cursor.execute("""
                INSERT INTO transaction_block_requests (customer_id, transaction_id, reason, status)
                VALUES (?, ?, ?, 'PENDING')
                """, (user["customerId"], tx_id, reason))
                conn.commit()

                return self.send_json(200, {"success": True, "message": "Transaction dispute/block request lodged. Anti-fraud team will review."})

            # 9. Change Password
            elif path == "/api/customer/change-password":
                if not user:
                    return self.send_json(401, {"error": "Unauthorized"})
                old_pwd = data.get("oldPassword")
                new_pwd = data.get("newPassword")

                cursor.execute("SELECT password FROM users WHERE username = ?", (user["sub"],))
                row = cursor.fetchone()
                if not row or row["password"] != old_pwd:
                    return self.send_json(400, {"success": False, "message": "Current password does not match."})

                cursor.execute("UPDATE users SET password = ? WHERE username = ?", (new_pwd, user["sub"]))
                conn.commit()
                return self.send_json(200, {"success": True, "message": "Password updated successfully."})

            else:
                return self.send_json(404, {"error": f"Endpoint not found: {path}"})

        finally:
            conn.close()

    def do_PUT(self):
        path = urllib.parse.urlparse(self.path).path
        data = self.read_json_body()
        user = self.get_auth_user()

        conn = get_db()
        cursor = conn.cursor()

        try:
            # 1. Update Profile
            if path == "/api/customer/profile":
                if not user or user["role"] != "ROLE_CUSTOMER":
                    return self.send_json(401, {"error": "Unauthorized"})
                cursor.execute("""
                UPDATE customers SET full_name = ?, address = ?, city = ?, state = ?, pin_code = ?
                WHERE customer_id = ?
                """, (data.get("fullName"), data.get("address"), data.get("city"), data.get("state"), data.get("pinCode"), user["customerId"]))
                conn.commit()
                return self.send_json(200, {"success": True, "message": "Profile updated successfully."})

            # 2. Admin Toggle Customer Status (Freeze/Unfreeze)
            elif re.match(r"^/api/admin/customers/\d+/toggle-status$", path):
                if not user or user["role"] != "ROLE_ADMIN":
                    return self.send_json(403, {"error": "Access Denied"})
                c_id = int(path.split("/")[4])
                cursor.execute("SELECT status FROM customers WHERE customer_id = ?", (c_id,))
                cust = cursor.fetchone()
                if not cust:
                    return self.send_json(404, {"error": "Customer not found"})
                new_status = "BLOCKED" if cust["status"] == "ACTIVE" else "ACTIVE"
                cursor.execute("UPDATE customers SET status = ? WHERE customer_id = ?", (new_status, c_id))
                cursor.execute("UPDATE accounts SET status = ? WHERE customer_id = ?", (new_status, c_id))
                cursor.execute("UPDATE users SET account_locked = ? WHERE customer_id = ?", (1 if new_status == "BLOCKED" else 0, c_id))
                cursor.execute("INSERT INTO audit_logs (user_id, action, description, ip_address) VALUES (?, 'ADMIN_TOGGLE_STATUS', ?, '127.0.0.1')",
                               (1, f"Customer #{c_id} status updated to {new_status}"))
                conn.commit()
                return self.send_json(200, {"success": True, "newStatus": new_status})

            # 3. Admin Respond to Complaint
            elif re.match(r"^/api/admin/complaints/\d+/respond$", path):
                if not user or user["role"] != "ROLE_ADMIN":
                    return self.send_json(403, {"error": "Access Denied"})
                comp_id = int(path.split("/")[4])
                status = data.get("status", "RESOLVED")
                response_text = data.get("adminResponse", "Reviewed and resolved by Branch Operations Manager.")
                cursor.execute("""
                UPDATE complaints SET status = ?, admin_response = ? WHERE complaint_id = ?
                """, (status, response_text, comp_id))
                conn.commit()
                return self.send_json(200, {"success": True, "message": "Complaint updated successfully."})

            # 4. Admin Action on Transaction Block
            elif re.match(r"^/api/admin/transaction-blocks/\d+/action$", path):
                if not user or user["role"] != "ROLE_ADMIN":
                    return self.send_json(403, {"error": "Access Denied"})
                req_id = int(path.split("/")[4])
                action = data.get("action", "APPROVED")  # APPROVED or REJECTED
                remarks = data.get("remarks", "Reviewed by Fraud Detection Unit.")
                cursor.execute("""
                UPDATE transaction_block_requests SET status = ?, admin_remarks = ?, reviewed_at = CURRENT_TIMESTAMP
                WHERE request_id = ?
                """, (action, remarks, req_id))
                conn.commit()
                return self.send_json(200, {"success": True, "message": f"Block request {action.lower()}."})

            else:
                return self.send_json(404, {"error": f"Endpoint not found: {path}"})

        finally:
            conn.close()

    def do_DELETE(self):
        path = urllib.parse.urlparse(self.path).path
        user = self.get_auth_user()

        conn = get_db()
        cursor = conn.cursor()

        try:
            # Delete Beneficiary
            if re.match(r"^/api/beneficiaries/\d+$", path):
                if not user or user["role"] != "ROLE_CUSTOMER":
                    return self.send_json(401, {"error": "Unauthorized"})
                b_id = int(path.split("/")[3])
                cursor.execute("DELETE FROM beneficiaries WHERE beneficiary_id = ? AND customer_id = ?", (b_id, user["customerId"]))
                conn.commit()
                return self.send_json(200, {"success": True, "message": "Beneficiary removed."})
            else:
                return self.send_json(404, {"error": f"Endpoint not found: {path}"})
        finally:
            conn.close()


def run():
    init_sqlite_db()
    print("================================================================")
    print("   APEX ONLINE BANKING MANAGEMENT SYSTEM - DEMO RUNNER")
    print("================================================================")
    print(f" Database: {DB_FILE} initialized.")
    print(f" Web Server running on: http://localhost:{PORT}")
    print(f" Open in browser: http://localhost:{PORT}/pages/index.html")
    print(" Default Test Accounts:")
    print("   - Admin:      admin       / Admin@123")
    print("   - Customer 1: rahulverma  / Password@123 (Account: 1001001001)")
    print("   - Customer 2: priyasharma / Password@123 (Account: 1002002002)")
    print("================================================================")

    class ReusableServer(socketserver.TCPServer):
        allow_reuse_address = True
    with ReusableServer(("", PORT), BankingRequestHandler) as httpd:
        httpd.serve_forever()


if __name__ == "__main__":
    run()
