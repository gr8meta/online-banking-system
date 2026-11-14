package com.bankingsystem.config;

import com.bankingsystem.entity.Account;
import com.bankingsystem.entity.Customer;
import com.bankingsystem.entity.User;
import com.bankingsystem.entity.enums.AccountStatus;
import com.bankingsystem.entity.enums.AccountType;
import com.bankingsystem.entity.enums.Role;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.CustomerRepository;
import com.bankingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds a default Administrator account (plus two demo customers) the first
 * time the app starts against a fresh database.
 * <p>
 * WHY THIS EXISTS (bug fix):
 * The project used to rely on {@code /database/data.sql} to insert the admin
 * row. That file lived outside {@code src/main/resources}, so Spring Boot's
 * automatic SQL initializer never ran it (data.sql is only auto-executed
 * from the classpath). Even if it had been run manually, the bcrypt hash
 * baked into that file was not actually a valid hash of "Admin@123" -
 * so login for the "admin" user always failed with "Invalid credentials",
 * while Customer login worked fine because customers register themselves
 * through /api/auth/register, which correctly calls
 * {@code passwordEncoder.encode(...)} on whatever password they choose.
 * <p>
 * Seeding here instead guarantees the stored hash is produced by the very
 * same {@link PasswordEncoder} bean used to verify it at login time. The
 * existsByUsername/existsByAccountNumber checks make this safe to run on
 * every application startup - it only inserts data once.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedDemoCustomer("rahulverma", "Password@123", "Rahul Verma", LocalDate.of(1998, 5, 15), "Male",
                "rahul.verma@example.com", "9876543210", "123 Tech Park, Sector 62", "Noida",
                "Uttar Pradesh", "201301", "1001001001", new BigDecimal("25000.00"));
        seedDemoCustomer("priyasharma", "Password@123", "Priya Sharma", LocalDate.of(2000, 8, 20), "Female",
                "priya.sharma@example.com", "9811223344", "45 Green Avenue, Connaught Place", "New Delhi",
                "Delhi", "110001", "1002002002", new BigDecimal("15000.00"));
    }

    private void seedAdmin() {
        if (userRepository.existsByUsername("admin")) {
            return;
        }
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ROLE_ADMIN)
                .build();
        userRepository.save(admin);
        System.out.println(">> [DataInitializer] Seeded default admin account -> username: admin / password: Admin@123");
    }

    private void seedDemoCustomer(String username, String rawPassword, String fullName, LocalDate dob,
                                   String gender, String email, String mobile, String address, String city,
                                   String state, String pinCode, String accountNumber, BigDecimal openingBalance) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        Customer customer = Customer.builder()
                .fullName(fullName)
                .dateOfBirth(dob)
                .gender(gender)
                .email(email)
                .mobile(mobile)
                .address(address)
                .city(city)
                .state(state)
                .pinCode(pinCode)
                .status(Customer.Status.ACTIVE)
                .build();
        Customer savedCustomer = customerRepository.save(customer);

        User user = User.builder()
                .customer(savedCustomer)
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.ROLE_CUSTOMER)
                .build();
        userRepository.save(user);

        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            Account account = Account.builder()
                    .customer(savedCustomer)
                    .accountNumber(accountNumber)
                    .accountType(AccountType.SAVINGS)
                    .balance(openingBalance)
                    .status(AccountStatus.ACTIVE)
                    .build();
            accountRepository.save(account);
        }
        System.out.println(">> [DataInitializer] Seeded demo customer -> username: " + username + " / password: " + rawPassword);
    }
}
