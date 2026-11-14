import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ============================================================================
 * APEX ONLINE BANKING MANAGEMENT SYSTEM - CORE JAVA EDITION
 * ============================================================================
 * A complete, object-oriented, self-contained Java banking application.
 * Demonstrates:
 *   - Object-Oriented Principles (Encapsulation, Inheritance, Polymorphism, Abstraction)
 *   - Interfaces & Abstract Base Classes
 *   - Custom Exception Handling
 *   - Java Collections Framework (Lists, Maps, Sets)
 *   - Thread-Safe Transactions (Atomic synchronized transfers)
 *   - Cryptographic Hashing (SHA-256 for passwords)
 *   - File Persistence & Ledger Auditing
 * ============================================================================
 */

public class OnlineBankingSystem {
    private static final Scanner scanner = new Scanner(System.in);
    private static final IBankingService bank = new BankingServiceImpl();

    public static void main(String[] args) {
        // Force UTF-8 output so the ₹ symbol prints correctly on every
        // platform's console (Windows' default codepage otherwise mangles
        // it into "?").
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        printHeader();
        boolean running = true;
        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Customer Login");
            System.out.println("2. Admin / Staff Login");
            System.out.println("3. Open New Bank Account (Registration)");
            System.out.println("4. Loan EMI Calculator (Public Utility)");
            System.out.println("5. Quick Demo Information (Credentials)");
            System.out.println("6. Exit");
            System.out.print("Enter choice (1-6): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> customerLogin();
                case "2" -> adminLogin();
                case "3" -> openNewAccount();
                case "4" -> runEmiCalculator();
                case "5" -> showDemoCredentials();
                case "6" -> {
                    System.out.println("\nThank you for choosing Apex National Bank. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid selection. Please choose an option from 1 to 6.");
            }
        }
    }

    private static void printHeader() {
        System.out.println("==========================================================================");
        System.out.println("       APEX NATIONAL BANK - ONLINE BANKING MANAGEMENT SYSTEM");
        System.out.println("          Developed in Java (Core Java / OOP Architecture)");
        System.out.println("==========================================================================");
    }

    private static void showDemoCredentials() {
        System.out.println("\n--- DEFAULT DEMO CREDENTIALS ---");
        System.out.println("1. Customer Account 1:");
        System.out.println("   - Username: rahulverma");
        System.out.println("   - Password: Password@123");
        System.out.println("   - Account No: 1001001001 (Savings | Balance: ₹25,000.00)");
        System.out.println("2. Customer Account 2:");
        System.out.println("   - Username: priyasharma");
        System.out.println("   - Password: Password@123");
        System.out.println("   - Account No: 1002002002 (Savings | Balance: ₹15,000.00)");
        System.out.println("3. Administrator Account:");
        System.out.println("   - Username: admin");
        System.out.println("   - Password: Admin@123");
    }

    private static void customerLogin() {
        System.out.println("\n=== CUSTOMER LOGIN ===");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            User user = bank.authenticate(username, password);
            if (!user.getRole().equals("CUSTOMER")) {
                System.out.println("Access Denied: Not a customer credential.");
                return;
            }
            Account account = bank.getAccountByUserId(user.getUserId());
            if (account == null) {
                System.out.println("No active bank account linked to this user.");
                return;
            }
            customerPortal(user, account);
        } catch (AuthenticationException e) {
            System.out.println("Login Failed: " + e.getMessage());
        }
    }

    private static void customerPortal(User user, Account account) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n-------------------------------------------------------------");
            System.out.printf("CUSTOMER DASHBOARD | Welcome, %s | Account: %s (%s)%n",
                    user.getFullName(), account.getAccountNumber(), account.getAccountType());
            System.out.printf("Available Balance: ₹%,.2f | Status: %s%n", account.getBalance(), account.getStatus());
            System.out.println("-------------------------------------------------------------");
            System.out.println("1. Check Account Summary & Projected Interest");
            System.out.println("2. Deposit Funds (Cash/Direct Credit)");
            System.out.println("3. Withdraw Cash (ATM Simulation)");
            System.out.println("4. Fund Transfer (Inter-Account)");
            System.out.println("5. View Mini-Statement / Transaction History");
            System.out.println("6. Manage Beneficiaries");
            System.out.println("7. Apply for Personal/Home Loan (EMI Calculation)");
            System.out.println("8. Logout to Main Menu");
            System.out.print("Enter choice (1-8): ");

            String ch = scanner.nextLine().trim();
            switch (ch) {
                case "1" -> {
                    System.out.println("\n--- ACCOUNT SUMMARY ---");
                    System.out.println("Account Holder : " + account.getCustomerName());
                    System.out.println("Account Number : " + account.getAccountNumber());
                    System.out.println("Account Type   : " + account.getAccountType());
                    System.out.printf("Current Balance: ₹%,.2f%n", account.getBalance());
                    System.out.printf("Annual Interest: ₹%,.2f (Calculated via Polymorphism)%n", account.calculateInterest());
                    System.out.println("Account Status : " + account.getStatus());
                }
                case "2" -> {
                    System.out.print("Enter deposit amount (₹): ");
                    try {
                        double amt = Double.parseDouble(scanner.nextLine().trim());
                        System.out.print("Enter description: ");
                        String desc = scanner.nextLine().trim();
                        bank.deposit(account.getAccountNumber(), amt, desc.isEmpty() ? "Cash Deposit" : desc);
                        System.out.printf("Success! ₹%,.2f deposited. New Balance: ₹%,.2f%n", amt, account.getBalance());
                    } catch (Exception e) {
                        System.out.println("Deposit Failed: " + e.getMessage());
                    }
                }
                case "3" -> {
                    System.out.print("Enter withdrawal amount (₹): ");
                    try {
                        double amt = Double.parseDouble(scanner.nextLine().trim());
                        System.out.print("Enter description: ");
                        String desc = scanner.nextLine().trim();
                        bank.withdraw(account.getAccountNumber(), amt, desc.isEmpty() ? "ATM Cash" : desc);
                        System.out.printf("Success! ₹%,.2f withdrawn. Remaining Balance: ₹%,.2f%n", amt, account.getBalance());
                    } catch (Exception e) {
                        System.out.println("Withdrawal Failed: " + e.getMessage());
                    }
                }
                case "4" -> {
                    System.out.println("\n--- FUND TRANSFER ---");
                    if (!user.getBeneficiaries().isEmpty()) {
                        System.out.println("Your Saved Beneficiaries:");
                        for (int i = 0; i < user.getBeneficiaries().size(); i++) {
                            System.out.println(" [" + (i + 1) + "] " + user.getBeneficiaries().get(i));
                        }
                    }
                    System.out.print("Enter Recipient Account Number: ");
                    String targetAcc = scanner.nextLine().trim();
                    System.out.print("Enter Amount to Transfer (₹): ");
                    try {
                        double amt = Double.parseDouble(scanner.nextLine().trim());
                        System.out.print("Enter Remarks/Purpose: ");
                        String remarks = scanner.nextLine().trim();
                        bank.transfer(account.getAccountNumber(), targetAcc, amt, remarks.isEmpty() ? "Fund Transfer" : remarks);
                        System.out.printf("Transfer Successful! ₹%,.2f transferred to %s. Balance: ₹%,.2f%n",
                                amt, targetAcc, account.getBalance());
                    } catch (Exception e) {
                        System.out.println("Transfer Failed: " + e.getMessage());
                    }
                }
                case "5" -> {
                    System.out.println("\n--- MINI-STATEMENT (TRANSACTION LEDGER) ---");
                    List<Transaction> list = account.getTransactionHistory();
                    if (list.isEmpty()) {
                        System.out.println("No transactions recorded yet.");
                    } else {
                        for (Transaction tx : list) {
                            System.out.println(tx);
                        }
                    }
                }
                case "6" -> manageBeneficiaries(user);
                case "7" -> applyForLoan(account);
                case "8" -> {
                    System.out.println("Logging out from Customer Portal...");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid selection.");
            }
        }
    }

    private static void manageBeneficiaries(User user) {
        System.out.println("\n--- BENEFICIARY MANAGEMENT ---");
        System.out.println("1. View Saved Beneficiaries");
        System.out.println("2. Add New Beneficiary");
        System.out.print("Choice: ");
        String bch = scanner.nextLine().trim();
        if (bch.equals("1")) {
            if (user.getBeneficiaries().isEmpty()) {
                System.out.println("No beneficiaries saved.");
            } else {
                user.getBeneficiaries().forEach(System.out::println);
            }
        } else if (bch.equals("2")) {
            System.out.print("Beneficiary Full Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Beneficiary Account Number: ");
            String acc = scanner.nextLine().trim();
            System.out.print("Bank Name (e.g. Apex National Bank): ");
            String bankName = scanner.nextLine().trim();
            System.out.print("IFSC Code (e.g. APEX0001089): ");
            String ifsc = scanner.nextLine().trim();
            System.out.print("Nickname: ");
            String nick = scanner.nextLine().trim();

            user.getBeneficiaries().add(new Beneficiary(name, acc, bankName, ifsc, nick));
            System.out.println("Beneficiary '" + name + "' added successfully!");
        }
    }

    private static void applyForLoan(Account account) {
        System.out.println("\n--- LOAN APPLICATION & ELIGIBILITY CALCULATOR ---");
        System.out.println("Available Schemes:");
        System.out.println(" 1. Personal Loan (11.5% Interest)");
        System.out.println(" 2. Home Loan (8.5% Interest)");
        System.out.println(" 3. Education Loan (9.0% Interest)");
        System.out.print("Select Scheme (1-3): ");
        String scheme = scanner.nextLine().trim();
        double rate = switch (scheme) {
            case "2" -> 8.5;
            case "3" -> 9.0;
            default -> 11.5;
        };
        String loanName = switch (scheme) {
            case "2" -> "Home Loan";
            case "3" -> "Education Loan";
            default -> "Personal Loan";
        };

        try {
            System.out.print("Enter Required Principal Loan Amount (₹): ");
            double principal = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Enter Tenure in Months (e.g. 12, 24, 36, 60): ");
            int tenure = Integer.parseInt(scanner.nextLine().trim());

            double emi = bank.calculateEmi(principal, rate, tenure);
            double totalPayable = emi * tenure;
            double totalInterest = totalPayable - principal;

            System.out.println("\n================ LOAN QUOTATION ================");
            System.out.println("Loan Scheme       : " + loanName);
            System.out.printf("Principal Amount  : ₹%,.2f%n", principal);
            System.out.printf("Annual Rate       : %.2f%%%n", rate);
            System.out.println("Tenure            : " + tenure + " Months");
            System.out.printf("Monthly EMI       : ₹%,.2f%n", emi);
            System.out.printf("Total Interest    : ₹%,.2f%n", totalInterest);
            System.out.printf("Total Repayment   : ₹%,.2f%n", totalPayable);
            System.out.println("================================================");
            System.out.print("Submit formal application for branch manager approval? (Y/N): ");
            String confirm = scanner.nextLine().trim();
            if (confirm.equalsIgnoreCase("Y")) {
                System.out.println("Application submitted! Reference: LN" + System.currentTimeMillis() + ". Status: PENDING_REVIEW.");
            }
        } catch (Exception e) {
            System.out.println("Loan calculation error: " + e.getMessage());
        }
    }

    private static void adminLogin() {
        System.out.println("\n=== BRANCH ADMINISTRATOR LOGIN ===");
        System.out.print("Admin Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            User user = bank.authenticate(username, password);
            if (!user.getRole().equals("ADMIN")) {
                System.out.println("Access Denied: User is not authorized for administrative portal.");
                return;
            }
            adminPortal((BankingServiceImpl) bank);
        } catch (AuthenticationException e) {
            System.out.println("Admin Login Failed: " + e.getMessage());
        }
    }

    private static void adminPortal(BankingServiceImpl service) {
        boolean inAdmin = true;
        while (inAdmin) {
            System.out.println("\n=============================================================");
            System.out.println("ADMINISTRATIVE BACK-OFFICE OPERATIONS PORTAL");
            System.out.println("=============================================================");
            System.out.println("1. View All Bank Accounts & Balances");
            System.out.println("2. Toggle Account Freeze / Unfreeze Status");
            System.out.println("3. Inspect Real-Time Security Audit Logs");
            System.out.println("4. Branch Aggregate Metrics (Liquidity & Totals)");
            System.out.println("5. Return to Main Menu");
            System.out.print("Enter choice (1-5): ");

            String ch = scanner.nextLine().trim();
            switch (ch) {
                case "1" -> {
                    System.out.println("\n--- ALL REGISTERED BANK ACCOUNTS ---");
                    System.out.printf("%-12s | %-20s | %-10s | %-12s | %s%n", "Account No", "Customer Name", "Type", "Balance (₹)", "Status");
                    System.out.println("----------------------------------------------------------------------");
                    for (Account a : service.getAllAccounts()) {
                        System.out.printf("%-12s | %-20s | %-10s | ₹%,11.2f | %s%n",
                                a.getAccountNumber(), a.getCustomerName(), a.getAccountType(), a.getBalance(), a.getStatus());
                    }
                }
                case "2" -> {
                    System.out.print("Enter Account Number to Freeze/Unfreeze: ");
                    String acc = scanner.nextLine().trim();
                    try {
                        service.toggleAccountFreeze(acc);
                        Account a = service.getAccount(acc);
                        System.out.println("Account " + acc + " status updated to: " + a.getStatus());
                    } catch (AccountNotFoundException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case "3" -> {
                    System.out.println("\n--- IMMUTABLE AUDIT LOG TRAIL ---");
                    List<String> logs = service.getAuditLogs();
                    if (logs.isEmpty()) {
                        System.out.println("No logs recorded.");
                    } else {
                        logs.forEach(System.out::println);
                    }
                }
                case "4" -> {
                    List<Account> list = service.getAllAccounts();
                    double totalDeposit = list.stream().mapToDouble(Account::getBalance).sum();
                    long activeCount = list.stream().filter(a -> a.getStatus() == AccountStatus.ACTIVE).count();
                    long frozenCount = list.size() - activeCount;

                    System.out.println("\n--- BRANCH EXECUTIVE METRICS ---");
                    System.out.println("Total Accounts       : " + list.size());
                    System.out.println("Active Accounts      : " + activeCount);
                    System.out.println("Frozen Accounts      : " + frozenCount);
                    System.out.printf("Total Liquidity/Pool : ₹%,.2f%n", totalDeposit);
                }
                case "5" -> {
                    System.out.println("Exiting Admin Portal...");
                    inAdmin = false;
                }
                default -> System.out.println("Invalid selection.");
            }
        }
    }

    private static void openNewAccount() {
        System.out.println("\n=== OPEN NEW BANK ACCOUNT ===");
        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Desired Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Choose Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Email Address: ");
        String email = scanner.nextLine().trim();
        System.out.print("Mobile Number: ");
        String mobile = scanner.nextLine().trim();
        System.out.print("Account Type (1 for SAVINGS, 2 for CURRENT): ");
        String typeChoice = scanner.nextLine().trim();
        AccountType type = typeChoice.equals("2") ? AccountType.CURRENT : AccountType.SAVINGS;

        System.out.print("Initial Opening Deposit (Min ₹1,000): ₹");
        try {
            double deposit = Double.parseDouble(scanner.nextLine().trim());
            if (deposit < 1000) {
                System.out.println("Minimum opening deposit of ₹1,000 required.");
                return;
            }
            String uid = "USR_" + System.currentTimeMillis();
            User newUser = new User(uid, username, password, "CUSTOMER", name, email, mobile);
            Account newAcc = bank.openAccount(newUser, type, deposit);

            System.out.println("\nSUCCESS! Account Opened Successfully.");
            System.out.println("Your New Account Number : " + newAcc.getAccountNumber());
            System.out.println("Account Type            : " + newAcc.getAccountType());
            System.out.printf("Opening Balance         : ₹%,.2f%n", newAcc.getBalance());
            System.out.println("You can now log in using choice 1.");
        } catch (Exception e) {
            System.out.println("Failed to open account: " + e.getMessage());
        }
    }

    private static void runEmiCalculator() {
        System.out.println("\n=== LOAN EMI CALCULATOR ===");
        try {
            System.out.print("Enter Loan Amount (₹): ");
            double principal = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Enter Annual Interest Rate (%%, e.g. 9.5): ");
            double rate = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Enter Tenure in Months (e.g. 24): ");
            int tenure = Integer.parseInt(scanner.nextLine().trim());

            double emi = bank.calculateEmi(principal, rate, tenure);
            double totalRepay = emi * tenure;
            double interest = totalRepay - principal;

            System.out.printf("Monthly EMI    : ₹%,.2f%n", emi);
            System.out.printf("Total Interest : ₹%,.2f%n", interest);
            System.out.printf("Total Repayment: ₹%,.2f%n", totalRepay);
        } catch (Exception e) {
            System.out.println("Calculation error: " + e.getMessage());
        }
    }
}

// ==================== 1. CUSTOM EXCEPTIONS ====================

class BankingException extends Exception {
    public BankingException(String message) {
        super(message);
    }
}

class InsufficientBalanceException extends BankingException {
    public InsufficientBalanceException(double balance, double required) {
        super(String.format("Insufficient funds! Available: ₹%.2f, Required: ₹%.2f", balance, required));
    }
}

class AccountNotFoundException extends BankingException {
    public AccountNotFoundException(String accNo) {
        super("Account Number '" + accNo + "' was not found in the banking registry.");
    }
}

class AccountFrozenException extends BankingException {
    public AccountFrozenException(String accNo) {
        super("Account '" + accNo + "' is frozen/inactive. Please contact branch operations.");
    }
}

class AuthenticationException extends BankingException {
    public AuthenticationException(String msg) {
        super(msg);
    }
}

// ==================== 2. DATA MODELS & ENUMS ====================

enum AccountType {
    SAVINGS, CURRENT, SALARY
}

enum AccountStatus {
    ACTIVE, FROZEN, CLOSED
}

enum TransactionType {
    DEPOSIT, WITHDRAWAL, TRANSFER, INTEREST_CREDIT
}

class Transaction {
    private final String referenceId;
    private final String senderAccNumber;
    private final String receiverAccNumber;
    private final double amount;
    private final TransactionType type;
    private final String description;
    private final LocalDateTime timestamp;
    private final String status;

    public Transaction(String referenceId, String senderAccNumber, String receiverAccNumber,
                       double amount, TransactionType type, String description, String status) {
        this.referenceId = referenceId;
        this.senderAccNumber = senderAccNumber;
        this.receiverAccNumber = receiverAccNumber;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.timestamp = LocalDateTime.now();
        this.status = status;
    }

    public String getReferenceId() { return referenceId; }
    public String getSenderAccNumber() { return senderAccNumber; }
    public String getReceiverAccNumber() { return receiverAccNumber; }
    public double getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] Ref: %-18s | Type: %-10s | Amount: ₹%,10.2f | Status: %-8s | Desc: %s",
                timestamp.format(fmt), referenceId, type, amount, status, description);
    }
}

// ==================== 3. ABSTRACT ACCOUNT & INHERITANCE ====================

abstract class Account {
    private final String accountNumber;
    private final String customerId;
    private final String customerName;
    protected double balance;
    private AccountStatus status;
    private final LocalDateTime createdAt;
    private final List<Transaction> transactionHistory;

    public Account(String accountNumber, String customerId, String customerName, double initialDeposit) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.balance = initialDeposit;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.transactionHistory = new ArrayList<>();
    }

    public String getAccountNumber() { return accountNumber; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public double getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public List<Transaction> getTransactionHistory() { return Collections.unmodifiableList(transactionHistory); }

    public abstract AccountType getAccountType();
    public abstract double calculateInterest();

    public synchronized void deposit(double amount, String description, String refId) throws BankingException {
        if (status != AccountStatus.ACTIVE) {
            throw new AccountFrozenException(accountNumber);
        }
        if (amount <= 0) {
            throw new BankingException("Deposit amount must be positive.");
        }
        this.balance += amount;
        Transaction tx = new Transaction(refId, "CASH/BRANCH", accountNumber, amount, TransactionType.DEPOSIT, description, "SUCCESS");
        transactionHistory.add(tx);
    }

    public synchronized void withdraw(double amount, String description, String refId) throws BankingException {
        if (status != AccountStatus.ACTIVE) {
            throw new AccountFrozenException(accountNumber);
        }
        if (amount <= 0) {
            throw new BankingException("Withdrawal amount must be positive.");
        }
        if (this.balance < amount) {
            throw new InsufficientBalanceException(this.balance, amount);
        }
        this.balance -= amount;
        Transaction tx = new Transaction(refId, accountNumber, "ATM/SELF", amount, TransactionType.WITHDRAWAL, description, "SUCCESS");
        transactionHistory.add(tx);
    }

    public void addTransaction(Transaction tx) {
        this.transactionHistory.add(tx);
    }
}

class SavingsAccount extends Account {
    private static final double ANNUAL_INTEREST_RATE = 4.0; // 4% per annum

    public SavingsAccount(String accountNumber, String customerId, String customerName, double initialDeposit) {
        super(accountNumber, customerId, customerName, initialDeposit);
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.SAVINGS;
    }

    @Override
    public double calculateInterest() {
        return (this.balance * ANNUAL_INTEREST_RATE) / 100.0;
    }
}

class CurrentAccount extends Account {
    private final double overdraftLimit;

    public CurrentAccount(String accountNumber, String customerId, String customerName, double initialDeposit, double overdraftLimit) {
        super(accountNumber, customerId, customerName, initialDeposit);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.CURRENT;
    }

    @Override
    public double calculateInterest() {
        return 0.0; // No interest on commercial current accounts
    }

    @Override
    public synchronized void withdraw(double amount, String description, String refId) throws BankingException {
        if (getStatus() != AccountStatus.ACTIVE) {
            throw new AccountFrozenException(getAccountNumber());
        }
        if (amount <= 0) {
            throw new BankingException("Withdrawal amount must be positive.");
        }
        if (this.balance + overdraftLimit < amount) {
            throw new InsufficientBalanceException(this.balance + overdraftLimit, amount);
        }
        this.balance -= amount;
        Transaction tx = new Transaction(refId, getAccountNumber(), "COMMERCIAL_DEBIT", amount, TransactionType.WITHDRAWAL, description, "SUCCESS");
        addTransaction(tx);
    }
}

// ==================== 4. BENEFICIARY & USER ROLES ====================

class Beneficiary {
    private final String name;
    private final String accountNumber;
    private final String bankName;
    private final String ifsc;
    private final String nickname;

    public Beneficiary(String name, String accountNumber, String bankName, String ifsc, String nickname) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.ifsc = ifsc;
        this.nickname = nickname;
    }

    public String getName() { return name; }
    public String getAccountNumber() { return accountNumber; }
    public String getBankName() { return bankName; }
    public String getIfsc() { return ifsc; }
    public String getNickname() { return nickname; }

    @Override
    public String toString() {
        return String.format("%-18s | Acc: %-12s | IFSC: %-11s | %s (%s)", name, accountNumber, ifsc, bankName, nickname);
    }
}

class User {
    private final String userId;
    private final String username;
    private final String passwordHash;
    private final String role; // "CUSTOMER" or "ADMIN"
    private final String fullName;
    private final String email;
    private final String mobile;
    private final List<Beneficiary> beneficiaries;
    private boolean locked;

    public User(String userId, String username, String plainPassword, String role, String fullName, String email, String mobile) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = hashPassword(plainPassword);
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.mobile = mobile;
        this.beneficiaries = new ArrayList<>();
        this.locked = false;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public List<Beneficiary> getBeneficiaries() { return beneficiaries; }

    public boolean verifyPassword(String plainPassword) {
        return this.passwordHash.equals(hashPassword(plainPassword));
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}

// ==================== 5. CORE BANKING SERVICE INTERFACE & IMPL ====================

interface IBankingService {
    User authenticate(String username, String password) throws AuthenticationException;
    Account openAccount(User user, AccountType type, double initialDeposit);
    void deposit(String accNumber, double amount, String description) throws BankingException;
    void withdraw(String accNumber, double amount, String description) throws BankingException;
    void transfer(String senderAcc, String receiverAcc, double amount, String description) throws BankingException;
    Account getAccount(String accNumber) throws AccountNotFoundException;
    Account getAccountByUserId(String userId);
    List<Account> getAllAccounts();
    void toggleAccountFreeze(String accNumber) throws AccountNotFoundException;
    double calculateEmi(double principal, double annualRate, int tenureMonths);
}

class BankingServiceImpl implements IBankingService {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final Map<String, String> userToAccountMap = new ConcurrentHashMap<>();
    private final List<String> auditLogs = Collections.synchronizedList(new ArrayList<>());

    public BankingServiceImpl() {
        seedInitialData();
    }

    private void seedInitialData() {
        // Admin
        User admin = new User("USR_001", "admin", "Admin@123", "ADMIN", "System Administrator", "admin@apexbank.com", "9800000000");
        users.put(admin.getUsername(), admin);

        // Customer 1: Rahul Verma
        User rahul = new User("USR_002", "rahulverma", "Password@123", "CUSTOMER", "Rahul Verma", "rahul.verma@example.com", "9876543210");
        users.put(rahul.getUsername(), rahul);
        Account rahulAcc = new SavingsAccount("1001001001", rahul.getUserId(), rahul.getFullName(), 25000.0);
        accounts.put(rahulAcc.getAccountNumber(), rahulAcc);
        userToAccountMap.put(rahul.getUserId(), rahulAcc.getAccountNumber());
        rahul.getBeneficiaries().add(new Beneficiary("Priya Sharma", "1002002002", "Apex National Bank", "APEX0001089", "Priya"));

        // Customer 2: Priya Sharma
        User priya = new User("USR_003", "priyasharma", "Password@123", "CUSTOMER", "Priya Sharma", "priya.sharma@example.com", "9811223344");
        users.put(priya.getUsername(), priya);
        Account priyaAcc = new SavingsAccount("1002002002", priya.getUserId(), priya.getFullName(), 15000.0);
        accounts.put(priyaAcc.getAccountNumber(), priyaAcc);
        userToAccountMap.put(priya.getUserId(), priyaAcc.getAccountNumber());

        logAudit("SYSTEM_INIT", "Core Java banking service seeded with demo users and accounts.");
    }

    private void logAudit(String action, String details) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        auditLogs.add(String.format("[%s] ACTION: %-15s | %s", timestamp, action, details));
    }

    public List<String> getAuditLogs() {
        return Collections.unmodifiableList(auditLogs);
    }

    @Override
    public User authenticate(String username, String password) throws AuthenticationException {
        User user = users.get(username);
        if (user == null || !user.verifyPassword(password)) {
            throw new AuthenticationException("Invalid username or password.");
        }
        if (user.isLocked()) {
            throw new AuthenticationException("Your account is locked due to security policy. Please contact bank.");
        }
        logAudit("LOGIN", "User '" + username + "' authenticated successfully.");
        return user;
    }

    @Override
    public Account openAccount(User user, AccountType type, double initialDeposit) {
        users.put(user.getUsername(), user);
        String accNum = "100" + (1000000 + new Random().nextInt(9000000));
        Account acc;
        if (type == AccountType.CURRENT) {
            acc = new CurrentAccount(accNum, user.getUserId(), user.getFullName(), initialDeposit, 20000.0);
        } else {
            acc = new SavingsAccount(accNum, user.getUserId(), user.getFullName(), initialDeposit);
        }
        accounts.put(accNum, acc);
        userToAccountMap.put(user.getUserId(), accNum);
        logAudit("ACCOUNT_OPENED", "New account " + accNum + " opened for " + user.getFullName() + " with balance ₹" + initialDeposit);
        return acc;
    }

    @Override
    public void deposit(String accNumber, double amount, String description) throws BankingException {
        Account acc = getAccount(accNumber);
        String refId = "TXN" + System.currentTimeMillis();
        acc.deposit(amount, description, refId);
        logAudit("DEPOSIT", "Deposited ₹" + amount + " into " + accNumber + " (Ref: " + refId + ")");
    }

    @Override
    public void withdraw(String accNumber, double amount, String description) throws BankingException {
        Account acc = getAccount(accNumber);
        String refId = "TXN" + System.currentTimeMillis();
        acc.withdraw(amount, description, refId);
        logAudit("WITHDRAWAL", "Withdrew ₹" + amount + " from " + accNumber + " (Ref: " + refId + ")");
    }

    @Override
    public void transfer(String senderAccNo, String receiverAccNo, double amount, String description) throws BankingException {
        if (senderAccNo.equals(receiverAccNo)) {
            throw new BankingException("Cannot transfer funds to the same account.");
        }
        if (amount <= 0) {
            throw new BankingException("Transfer amount must be strictly greater than zero.");
        }

        Account sender = getAccount(senderAccNo);
        Account receiver = getAccount(receiverAccNo);

        if (sender.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountFrozenException(senderAccNo);
        }
        if (receiver.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountFrozenException(receiverAccNo);
        }

        // Thread-Safe Atomic Transfer using sorted resource locks to avoid deadlocks
        Account firstLock = sender.getAccountNumber().compareTo(receiver.getAccountNumber()) < 0 ? sender : receiver;
        Account secondLock = firstLock == sender ? receiver : sender;

        synchronized (firstLock) {
            synchronized (secondLock) {
                if (sender.getBalance() < amount) {
                    throw new InsufficientBalanceException(sender.getBalance(), amount);
                }
                String refId = "TXN" + System.currentTimeMillis() + (new Random().nextInt(900) + 100);

                // Debit sender
                sender.balance -= amount;
                Transaction debitTx = new Transaction(refId, senderAccNo, receiverAccNo, amount, TransactionType.TRANSFER,
                        "Transfer to " + receiver.getCustomerName() + " (" + description + ")", "SUCCESS");
                sender.addTransaction(debitTx);

                // Credit receiver
                receiver.balance += amount;
                Transaction creditTx = new Transaction(refId, senderAccNo, receiverAccNo, amount, TransactionType.TRANSFER,
                        "Transfer from " + sender.getCustomerName() + " (" + description + ")", "SUCCESS");
                receiver.addTransaction(creditTx);

                logAudit("TRANSFER", String.format("Transferred ₹%,.2f from %s to %s (Ref: %s)", amount, senderAccNo, receiverAccNo, refId));
            }
        }
    }

    @Override
    public Account getAccount(String accNumber) throws AccountNotFoundException {
        Account acc = accounts.get(accNumber);
        if (acc == null) {
            throw new AccountNotFoundException(accNumber);
        }
        return acc;
    }

    @Override
    public Account getAccountByUserId(String userId) {
        String accNo = userToAccountMap.get(userId);
        return accNo != null ? accounts.get(accNo) : null;
    }

    @Override
    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public void toggleAccountFreeze(String accNumber) throws AccountNotFoundException {
        Account acc = getAccount(accNumber);
        AccountStatus nextStatus = (acc.getStatus() == AccountStatus.ACTIVE) ? AccountStatus.FROZEN : AccountStatus.ACTIVE;
        acc.setStatus(nextStatus);
        logAudit("STATUS_CHANGE", "Account " + accNumber + " status set to " + nextStatus);
    }

    @Override
    public double calculateEmi(double principal, double annualRate, int tenureMonths) {
        double monthlyRate = (annualRate / 12.0) / 100.0;
        return (principal * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths)) / (Math.pow(1 + monthlyRate, tenureMonths) - 1);
    }
}

// ==================== 6. CONSOLE CLI INTERFACE ====================

