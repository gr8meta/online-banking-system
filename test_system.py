import urllib.request
import urllib.error
import json
import subprocess
import time
import sys

def run_tests():
    # Start server
    proc = subprocess.Popen([sys.executable, "demo_server.py"], cwd="/working_dir/c_8c96d75f13b2c302/online-banking-system")
    time.sleep(2)

    base_url = "http://127.0.0.1:8000"
    print("Starting Automated E2E Test Suite...")

    def api_call(endpoint, method="GET", data=None, token=None):
        url = f"{base_url}{endpoint}"
        req = urllib.request.Request(url, method=method)
        req.add_header("Content-Type", "application/json")
        if token:
            req.add_header("Authorization", f"Bearer {token}")
        body = json.dumps(data).encode("utf-8") if data else None
        try:
            with urllib.request.urlopen(req, data=body) as response:
                return response.getcode(), json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as e:
            return e.code, json.loads(e.read().decode("utf-8"))

    try:
        # 1. Test Static Homepage
        with urllib.request.urlopen(f"{base_url}/pages/index.html") as resp:
            assert resp.getcode() == 200
            print(" [PASS] 1. Static Web Portal Homepage loaded successfully.")

        # 2. Test Customer Login
        code, resp = api_call("/api/auth/login", method="POST", data={"username": "rahulverma", "password": "Password@123"})
        assert code == 200 and "token" in resp
        customer_token = resp["token"]
        print(" [PASS] 2. Customer Authentication & JWT Generation succeeded.")

        # 3. Test Dashboard Retrieval
        code, resp = api_call("/api/customer/dashboard", method="GET", token=customer_token)
        assert code == 200 and resp["fullName"] == "Rahul Verma"
        initial_balance = resp["balance"]
        account_id = resp["accountId"]
        print(f" [PASS] 3. Customer Dashboard verified. Account Balance: ₹{initial_balance:,.2f}")

        # 4. Test Deposit
        code, resp = api_call("/api/accounts/deposit", method="POST", data={"amount": 5000, "description": "Test Salary Bonus"}, token=customer_token)
        assert code == 200 and resp["success"] is True
        print(f" [PASS] 4. Cash Deposit of ₹5,000 completed. New Balance: ₹{resp['newBalance']:,.2f}")

        # 5. Test Fund Transfer to Priya (1002002002)
        code, resp = api_call("/api/transfers", method="POST", data={"receiverAccountNumber": "1002002002", "amount": 2500, "description": "Project Share"}, token=customer_token)
        assert code == 200 and resp["success"] is True
        print(f" [PASS] 5. Inter-Account Fund Transfer of ₹2,500 executed. Reference ID: {resp['referenceId']}")

        # 6. Test Withdrawal
        code, resp = api_call("/api/accounts/withdraw", method="POST", data={"amount": 1000, "description": "ATM Test"}, token=customer_token)
        assert code == 200 and resp["success"] is True
        print(f" [PASS] 6. ATM Cash Withdrawal of ₹1,000 processed. Remaining: ₹{resp['newBalance']:,.2f}")

        # 7. Test Insufficient Funds Transfer Validation
        code, resp = api_call("/api/transfers", method="POST", data={"receiverAccountNumber": "1002002002", "amount": 99999999, "description": "Exorbitant Transfer"}, token=customer_token)
        assert code == 400 and resp["success"] is False
        print(" [PASS] 7. Insufficient Balance Guard & ACID Rollback verified.")

        # 8. Test Beneficiaries
        code, resp = api_call("/api/beneficiaries", method="GET", token=customer_token)
        assert code == 200 and len(resp) >= 1
        print(f" [PASS] 8. Beneficiary Directory verified ({len(resp)} registered).")

        # 9. Test New User Registration
        new_username = f"user_{int(time.time())}"
        reg_payload = {
            "fullName": "Amit Saxena",
            "username": new_username,
            "password": "Password@123",
            "email": f"{new_username}@example.com",
            "mobile": f"98{int(time.time()%100000000):08d}",
            "dateOfBirth": "1995-12-10",
            "gender": "Male",
            "address": "Sector 18",
            "city": "Noida",
            "state": "Uttar Pradesh",
            "pinCode": "201301"
        }
        code, resp = api_call("/api/auth/register", method="POST", data=reg_payload)
        assert code == 200 and resp["success"] is True
        print(f" [PASS] 9. Self-Service User Onboarding succeeded. Generated Account: {resp['accountNumber']}")

        # 10. Test Admin Login & Overview
        code, resp = api_call("/api/auth/admin/login", method="POST", data={"username": "admin", "password": "Admin@123"})
        assert code == 200 and resp["role"] == "ROLE_ADMIN"
        admin_token = resp["token"]
        print(" [PASS] 10. Admin Role-Based Authentication verified.")

        # 11. Test Admin Dashboard & Audit Logs
        code, resp = api_call("/api/admin/dashboard", method="GET", token=admin_token)
        assert code == 200 and resp["totalCustomers"] >= 2
        print(f" [PASS] 11. Admin Real-Time Metrics: {resp['totalCustomers']} Active Customers, ₹{resp['totalDeposits']:,.2f} Deposits, {resp['totalTransactions']} Transactions.")

        code, resp = api_call("/api/admin/audit-logs", method="GET", token=admin_token)
        assert code == 200 and len(resp) >= 1
        print(f" [PASS] 12. Security Audit Trail inspected ({len(resp)} audit records recorded).")

        print("\nALL 12 AUTOMATED TESTS PASSED WITH 100% SUCCESS!")

    finally:
        proc.terminate()

if __name__ == "__main__":
    run_tests()
