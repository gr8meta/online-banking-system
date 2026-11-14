import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * ============================================================================
 * PURE JAVA WEB SERVER & REST API FOR ONLINE BANKING SYSTEM
 * ============================================================================
 * Uses Java Standard Library's com.sun.net.httpserver.HttpServer.
 * Zero external libraries or frameworks required.
 * Runs on JDK 17+ with: java JavaBankingWebServer.java
 * Serves the modern HTML5/Bootstrap 5 frontend on http://localhost:8000
 * ============================================================================
 */
public class JavaBankingWebServer {

    private static final int PORT = 8000;
    private static File FRONTEND_DIR;

    // In-memory data store for the Java Web Server
    public static final Map<String, WebUser> users = new ConcurrentHashMap<>();
    public static final Map<String, WebAccount> accounts = new ConcurrentHashMap<>();
    public static final Map<String, String> userTokens = new ConcurrentHashMap<>(); // token -> username
    public static final List<WebTransaction> transactions = Collections.synchronizedList(new ArrayList<>());
    public static final List<String> auditLogs = Collections.synchronizedList(new ArrayList<>());
    public static final List<Map<String, Object>> complaints = Collections.synchronizedList(new ArrayList<>());
    public static final List<Map<String, Object>> transactionBlocks = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws Exception {
        // Locate frontend directory
        File currentDir = new File(System.getProperty("user.dir"));
        File candidate1 = new File(currentDir, "frontend");
        File candidate2 = new File(new File(currentDir, "online-banking-system"), "frontend");
        if (candidate1.exists()) {
            FRONTEND_DIR = candidate1;
        } else if (candidate2.exists()) {
            FRONTEND_DIR = candidate2;
        } else {
            FRONTEND_DIR = new File("/working_dir/c_8c96d75f13b2c302/online-banking-system/frontend");
        }

        seedData();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // 1. Static Web Portal & Pages Router
        server.createContext("/", new StaticFileHandler());

        // 2. REST API Endpoints
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/auth/admin/login", new LoginHandler());
        server.createContext("/api/auth/register", new RegisterHandler());
        server.createContext("/api/customer/dashboard", new CustomerDashboardHandler());
        server.createContext("/api/customer/profile", new CustomerProfileHandler());
        server.createContext("/api/customer/change-password", new ChangePasswordHandler());
        server.createContext("/api/accounts", new AccountsHandler());
        server.createContext("/api/transfers", new TransferHandler());
        server.createContext("/api/beneficiaries", new BeneficiaryHandler());
        server.createContext("/api/complaints", new ComplaintHandler());
        server.createContext("/api/transaction-blocks", new TransactionBlockHandler());
        server.createContext("/api/admin/dashboard", new AdminDashboardHandler());
        server.createContext("/api/admin/customers", new AdminCustomersHandler());
        server.createContext("/api/admin/transactions", new AdminTransactionsHandler());
        server.createContext("/api/admin/complaints", new AdminComplaintsHandler());
        server.createContext("/api/admin/audit-logs", new AdminAuditLogsHandler());

        System.out.println("============================================================================");
        System.out.println("     APEX ONLINE BANKING SYSTEM - PURE JAVA HTTP WEB SERVER");
        System.out.println("============================================================================");
        System.out.println(" Engine        : JDK 17 com.sun.net.httpserver.HttpServer");
        System.out.println(" Web Portal    : http://localhost:" + PORT + "/pages/index.html");
        System.out.println(" Frontend Root : " + FRONTEND_DIR.getAbsolutePath());
        System.out.println(" Test Accounts :");
        System.out.println("   - Customer 1: rahulverma  / Password@123 (Account: 1001001001)");
        System.out.println("   - Customer 2: priyasharma / Password@123 (Account: 1002002002)");
        System.out.println("   - Admin     : admin       / Admin@123");
        System.out.println("============================================================================");

        server.start();
    }

    private static void seedData() {
        // Admin
        WebUser admin = new WebUser("admin", "Admin@123", "ROLE_ADMIN", "System Administrator", "admin@apexbank.com", "9800000000", null);
        users.put("admin", admin);

        // Customer 1: Rahul Verma
        WebAccount acc1 = new WebAccount(1L, "1001001001", "SAVINGS", 25000.0, "ACTIVE");
        WebUser rahul = new WebUser("rahulverma", "Password@123", "ROLE_CUSTOMER", "Rahul Verma", "rahul.verma@example.com", "9876543210", acc1);
        users.put("rahulverma", rahul);
        accounts.put("1001001001", acc1);
        rahul.beneficiaries.add(new WebBeneficiary(1L, "Priya Sharma", "1002002002", "APEX0001089", "Apex National Bank", "Priya"));

        // Customer 2: Priya Sharma
        WebAccount acc2 = new WebAccount(2L, "1002002002", "SAVINGS", 15000.0, "ACTIVE");
        WebUser priya = new WebUser("priyasharma", "Password@123", "ROLE_CUSTOMER", "Priya Sharma", "priya.sharma@example.com", "9811223344", acc2);
        users.put("priyasharma", priya);
        accounts.put("1002002002", acc2);

        // Transactions
        transactions.add(new WebTransaction("TXN20260901001", null, "1001001001", 30000.0, "DEPOSIT", "Opening Balance & Salary Credit", "SUCCESS", "2026-09-01 10:00:00"));
        transactions.add(new WebTransaction("TXN20260904002", "1001001001", null, 5000.0, "WITHDRAWAL", "ATM Cash Withdrawal", "SUCCESS", "2026-09-04 15:30:00"));

        logAudit("SYSTEM_INIT", "Pure Java banking web server ledger initialized.");
    }

    public static void logAudit(String action, String details) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        auditLogs.add(0, String.format("[%s] %-15s | %s", ts, action, details));
    }

    public static WebUser getAuthUser(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        String token = auth.substring(7).trim();
        String username = userTokens.get(token);
        return username != null ? users.get(username) : null;
    }

    public static void sendJson(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;
        String clean = json.trim();
        if (clean.startsWith("{")) clean = clean.substring(1);
        if (clean.endsWith("}")) clean = clean.substring(0, clean.length() - 1);
        String[] pairs = clean.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String val = kv[1].trim().replace("\"", "");
                map.put(key, val);
            }
        }
        return map;
    }

    // ==================== HTTP HANDLERS ====================

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                sendJson(exchange, 204, "");
                return;
            }

            if (path.equals("/") || path.equals("/index.html")) {
                path = "/pages/index.html";
            }

            File file = new File(FRONTEND_DIR, path.replaceFirst("^/", ""));
            if (file.exists() && file.isFile()) {
                String mime = "application/octet-stream";
                if (path.endsWith(".html")) mime = "text/html; charset=utf-8";
                else if (path.endsWith(".css")) mime = "text/css; charset=utf-8";
                else if (path.endsWith(".js")) mime = "application/javascript; charset=utf-8";
                else if (path.endsWith(".json")) mime = "application/json; charset=utf-8";
                else if (path.endsWith(".png")) mime = "image/png";
                else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) mime = "image/jpeg";
                else if (path.endsWith(".svg")) mime = "image/svg+xml";

                byte[] bytes = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().set("Content-Type", mime);
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String notFound = "<h1>404 Not Found</h1><p>Resource " + path + " not found.</p>";
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                sendJson(exchange, 204, "");
                return;
            }
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            Map<String, String> body = parseSimpleJson(readRequestBody(exchange));
            String username = body.get("username");
            String password = body.get("password");

            WebUser user = users.get(username);
            if (user == null || !user.password.equals(password)) {
                sendJson(exchange, 401, "{\"success\":false,\"message\":\"Invalid username or password\"}");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if (path.contains("/admin") && !user.role.equals("ROLE_ADMIN")) {
                sendJson(exchange, 403, "{\"success\":false,\"message\":\"Access Denied: Administrative privilege required.\"}");
                return;
            }

            String token = "TOKEN_" + UUID.randomUUID().toString();
            userTokens.put(token, user.username);
            logAudit("LOGIN", "User '" + user.username + "' logged in via Java Web Server.");

            Long custId = user.account != null ? user.account.id : null;
            String json = String.format("{\"token\":\"%s\",\"username\":\"%s\",\"role\":\"%s\",\"customerId\":%s,\"message\":\"Login successful\"}",
                    token, user.username, user.role, custId != null ? custId : "null");
            sendJson(exchange, 200, json);
        }
    }

    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                sendJson(exchange, 204, "");
                return;
            }
            Map<String, String> body = parseSimpleJson(readRequestBody(exchange));
            String username = body.get("username");
            String password = body.get("password");
            String fullName = body.get("fullName");
            String email = body.get("email");
            String mobile = body.get("mobile");

            if (users.containsKey(username)) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"Username already exists\"}");
                return;
            }

            String accNum = "100" + (1000000 + new Random().nextInt(9000000));
            long newId = accounts.size() + 1;
            WebAccount acc = new WebAccount(newId, accNum, "SAVINGS", 5000.0, "ACTIVE");
            WebUser u = new WebUser(username, password, "ROLE_CUSTOMER", fullName, email, mobile, acc);

            users.put(username, u);
            accounts.put(accNum, acc);

            transactions.add(new WebTransaction("TXN" + System.currentTimeMillis(), null, accNum, 5000.0, "DEPOSIT", "Welcome Opening Bonus", "SUCCESS", LocalDateTime.now().toString()));
            logAudit("REGISTER", "New user '" + fullName + "' registered with Account: " + accNum);

            sendJson(exchange, 200, String.format("{\"success\":true,\"message\":\"Registration successful!\",\"accountNumber\":\"%s\"}", accNum));
        }
    }

    static class CustomerDashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            WebUser user = getAuthUser(exchange);
            if (user == null || user.account == null) {
                sendJson(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }

            double deposits = transactions.stream()
                    .filter(t -> user.account.accountNumber.equals(t.receiverAccount) && t.status.equals("SUCCESS"))
                    .mapToDouble(t -> t.amount).sum();
            double withdrawals = transactions.stream()
                    .filter(t -> user.account.accountNumber.equals(t.senderAccount) && t.status.equals("SUCCESS"))
                    .mapToDouble(t -> t.amount).sum();

            StringBuilder txJson = new StringBuilder("[");
            int count = 0;
            for (WebTransaction t : transactions) {
                if (user.account.accountNumber.equals(t.senderAccount) || user.account.accountNumber.equals(t.receiverAccount)) {
                    if (count > 0) txJson.append(",");
                    txJson.append(t.toJson());
                    count++;
                    if (count >= 5) break;
                }
            }
            txJson.append("]");

            String resp = String.format("{\"fullName\":\"%s\",\"accountId\":%d,\"accountNumber\":\"%s\",\"balance\":%.2f,\"accountType\":\"%s\",\"totalDeposits\":%.2f,\"totalWithdrawals\":%.2f,\"recentTransactions\":%s}",
                    user.fullName, user.account.id, user.account.accountNumber, user.account.balance, user.account.accountType, deposits, withdrawals, txJson);
            sendJson(exchange, 200, resp);
        }
    }

    static class AccountsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            WebUser user = getAuthUser(exchange);
            if (user == null || user.account == null) {
                sendJson(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }

            if (path.endsWith("/deposit") && exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Map<String, String> body = parseSimpleJson(readRequestBody(exchange));
                double amt = Double.parseDouble(body.getOrDefault("amount", "0"));
                String desc = body.getOrDefault("description", "Online Deposit");
                if (amt <= 0) {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"Deposit must be positive\"}");
                    return;
                }
                user.account.balance += amt;
                String ref = "TXN" + System.currentTimeMillis();
                transactions.add(0, new WebTransaction(ref, null, user.account.accountNumber, amt, "DEPOSIT", desc, "SUCCESS", LocalDateTime.now().toString()));
                logAudit("DEPOSIT", "Deposited ₹" + amt + " into " + user.account.accountNumber);
                sendJson(exchange, 200, String.format("{\"success\":true,\"message\":\"Deposit successful\",\"referenceId\":\"%s\",\"newBalance\":%.2f}", ref, user.account.balance));
            } else if (path.endsWith("/withdraw") && exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Map<String, String> body = parseSimpleJson(readRequestBody(exchange));
                double amt = Double.parseDouble(body.getOrDefault("amount", "0"));
                String desc = body.getOrDefault("description", "ATM Withdrawal");
                if (amt <= 0 || user.account.balance < amt) {
                    sendJson(exchange, 400, "{\"success\":false,\"message\":\"Insufficient balance\"}");
                    return;
                }
                user.account.balance -= amt;
                String ref = "TXN" + System.currentTimeMillis();
                transactions.add(0, new WebTransaction(ref, user.account.accountNumber, null, amt, "WITHDRAWAL", desc, "SUCCESS", LocalDateTime.now().toString()));
                logAudit("WITHDRAWAL", "Withdrew ₹" + amt + " from " + user.account.accountNumber);
                sendJson(exchange, 200, String.format("{\"success\":true,\"message\":\"Withdrawal successful\",\"referenceId\":\"%s\",\"newBalance\":%.2f}", ref, user.account.balance));
            } else if (path.matches("^/api/accounts/\\d+/transactions$")) {
                StringBuilder sb = new StringBuilder("[");
                int c = 0;
                for (WebTransaction t : transactions) {
                    if (user.account.accountNumber.equals(t.senderAccount) || user.account.accountNumber.equals(t.receiverAccount)) {
                        if (c > 0) sb.append(",");
                        sb.append(t.toJson());
                        c++;
                    }
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } else {
                String accJson = String.format("[{\"accountId\":%d,\"accountNumber\":\"%s\",\"accountType\":\"%s\",\"balance\":%.2f,\"status\":\"%s\"}]",
                        user.account.id, user.account.accountNumber, user.account.accountType, user.account.balance, user.account.status);
                sendJson(exchange, 200, accJson);
            }
        }
    }

    static class TransferHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                sendJson(exchange, 204, "");
                return;
            }
            WebUser sender = getAuthUser(exchange);
            if (sender == null || sender.account == null) {
                sendJson(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }

            Map<String, String> body = parseSimpleJson(readRequestBody(exchange));
            String targetAccNo = body.get("receiverAccountNumber");
            double amount = Double.parseDouble(body.getOrDefault("amount", "0"));
            String desc = body.getOrDefault("description", "Funds Transfer");

            if (amount <= 0 || sender.account.balance < amount) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"Insufficient balance or invalid amount\"}");
                return;
            }

            WebAccount targetAcc = accounts.get(targetAccNo);
            if (targetAcc == null) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"Target account number does not exist\"}");
                return;
            }

            // Execute synchronized transfer
            synchronized (JavaBankingWebServer.class) {
                sender.account.balance -= amount;
                targetAcc.balance += amount;
                String refId = "TXN" + System.currentTimeMillis();
                transactions.add(0, new WebTransaction(refId, sender.account.accountNumber, targetAccNo, amount, "TRANSFER", desc, "SUCCESS", LocalDateTime.now().toString()));
                logAudit("TRANSFER", String.format("Transferred ₹%.2f from %s to %s", amount, sender.account.accountNumber, targetAccNo));

                String resp = String.format("{\"success\":true,\"message\":\"Transfer completed successfully\",\"referenceId\":\"%s\",\"remainingBalance\":%.2f}", refId, sender.account.balance);
                sendJson(exchange, 200, resp);
            }
        }
    }

    static class BeneficiaryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            WebUser user = getAuthUser(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }

            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < user.beneficiaries.size(); i++) {
                    if (i > 0) sb.append(",");
                    WebBeneficiary b = user.beneficiaries.get(i);
                    sb.append(String.format("{\"beneficiaryId\":%d,\"beneficiaryName\":\"%s\",\"accountNumber\":\"%s\",\"ifscCode\":\"%s\",\"bankName\":\"%s\",\"nickname\":\"%s\"}",
                            b.id, b.name, b.accountNumber, b.ifsc, b.bankName, b.nickname));
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Map<String, String> b = parseSimpleJson(readRequestBody(exchange));
                user.beneficiaries.add(new WebBeneficiary((long) (user.beneficiaries.size() + 1), b.get("beneficiaryName"), b.get("accountNumber"), b.get("ifscCode"), b.get("bankName"), b.get("nickname")));
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Beneficiary saved\"}");
            }
        }
    }

    static class ComplaintHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            WebUser user = getAuthUser(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                Map<String, String> b = parseSimpleJson(readRequestBody(exchange));
                Map<String, Object> map = new HashMap<>();
                map.put("complaintId", complaints.size() + 1);
                map.put("subject", b.get("subject"));
                map.put("description", b.get("description"));
                map.put("status", "OPEN");
                map.put("username", user.username);
                complaints.add(0, map);
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Grievance submitted\"}");
            } else {
                sendJson(exchange, 200, "[]");
            }
        }
    }

    static class TransactionBlockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Transaction reported\"}");
        }
    }

    static class CustomerProfileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            WebUser user = getAuthUser(exchange);
            if (user == null) {
                sendJson(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }
            String resp = String.format("{\"fullName\":\"%s\",\"email\":\"%s\",\"mobile\":\"%s\",\"address\":\"Connaught Place\",\"city\":\"New Delhi\",\"state\":\"Delhi\",\"pinCode\":\"110001\"}",
                    user.fullName, user.email, user.mobile);
            sendJson(exchange, 200, resp);
        }
    }

    static class ChangePasswordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Password updated\"}");
        }
    }

    static class AdminDashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            WebUser user = getAuthUser(exchange);
            if (user == null || !user.role.equals("ROLE_ADMIN")) {
                sendJson(exchange, 403, "{\"error\":\"Forbidden\"}");
                return;
            }
            double totalDep = accounts.values().stream().mapToDouble(a -> a.balance).sum();
            double totalVol = transactions.stream().mapToDouble(t -> t.amount).sum();

            String resp = String.format("{\"totalCustomers\":%d,\"totalAccounts\":%d,\"totalDeposits\":%.2f,\"totalTransactions\":%d,\"totalVolume\":%.2f,\"pendingComplaints\":%d,\"pendingBlocks\":0}",
                    users.size() - 1, accounts.size(), totalDep, transactions.size(), totalVol, complaints.size());
            sendJson(exchange, 200, resp);
        }
    }

    static class AdminCustomersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder("[");
            int c = 0;
            for (WebUser u : users.values()) {
                if (u.role.equals("ROLE_ADMIN")) continue;
                if (c > 0) sb.append(",");
                sb.append(String.format("{\"customerId\":%d,\"fullName\":\"%s\",\"email\":\"%s\",\"mobile\":\"%s\",\"accountNumber\":\"%s\",\"balance\":%.2f,\"status\":\"ACTIVE\"}",
                        u.account != null ? u.account.id : 0, u.fullName, u.email, u.mobile, u.account != null ? u.account.accountNumber : "N/A", u.account != null ? u.account.balance : 0.0));
                c++;
            }
            sb.append("]");
            sendJson(exchange, 200, sb.toString());
        }
    }

    static class AdminTransactionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < transactions.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(transactions.get(i).toJson());
            }
            sb.append("]");
            sendJson(exchange, 200, sb.toString());
        }
    }

    static class AdminComplaintsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendJson(exchange, 200, "[]");
        }
    }

    static class AdminAuditLogsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < auditLogs.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(String.format("{\"logId\":%d,\"action\":\"INFO\",\"description\":\"%s\",\"ipAddress\":\"127.0.0.1\",\"createdAt\":\"2026-09-11\"}",
                        i + 1, auditLogs.get(i).replace("\"", "'")));
            }
            sb.append("]");
            sendJson(exchange, 200, sb.toString());
        }
    }

    // ==================== SUPPORTING DATA CLASSES ====================

    static class WebUser {
        String username;
        String password;
        String role;
        String fullName;
        String email;
        String mobile;
        WebAccount account;
        List<WebBeneficiary> beneficiaries = new ArrayList<>();

        WebUser(String username, String password, String role, String fullName, String email, String mobile, WebAccount account) {
            this.username = username;
            this.password = password;
            this.role = role;
            this.fullName = fullName;
            this.email = email;
            this.mobile = mobile;
            this.account = account;
        }
    }

    static class WebAccount {
        long id;
        String accountNumber;
        String accountType;
        double balance;
        String status;

        WebAccount(long id, String accountNumber, String accountType, double balance, String status) {
            this.id = id;
            this.accountNumber = accountNumber;
            this.accountType = accountType;
            this.balance = balance;
            this.status = status;
        }
    }

    static class WebBeneficiary {
        long id;
        String name;
        String accountNumber;
        String ifsc;
        String bankName;
        String nickname;

        WebBeneficiary(long id, String name, String accountNumber, String ifsc, String bankName, String nickname) {
            this.id = id;
            this.name = name;
            this.accountNumber = accountNumber;
            this.ifsc = ifsc;
            this.bankName = bankName;
            this.nickname = nickname;
        }
    }

    static class WebTransaction {
        String referenceId;
        String senderAccount;
        String receiverAccount;
        double amount;
        String type;
        String description;
        String status;
        String date;

        WebTransaction(String referenceId, String senderAccount, String receiverAccount, double amount, String type, String description, String status, String date) {
            this.referenceId = referenceId;
            this.senderAccount = senderAccount;
            this.receiverAccount = receiverAccount;
            this.amount = amount;
            this.type = type;
            this.description = description;
            this.status = status;
            this.date = date;
        }

        String toJson() {
            return String.format("{\"transactionId\":1,\"referenceId\":\"%s\",\"transactionType\":\"%s\",\"amount\":%.2f,\"description\":\"%s\",\"status\":\"%s\",\"transactionDate\":\"%s\",\"senderAccount\":\"%s\",\"receiverAccount\":\"%s\"}",
                    referenceId, type, amount, description, status, date,
                    senderAccount != null ? senderAccount : "N/A",
                    receiverAccount != null ? receiverAccount : "N/A");
        }
    }
}
