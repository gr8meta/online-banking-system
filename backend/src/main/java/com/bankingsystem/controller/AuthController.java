package com.bankingsystem.controller;

import com.bankingsystem.dto.request.LoginRequest;
import com.bankingsystem.dto.request.RegisterRequest;
import com.bankingsystem.dto.response.ApiResponse;
import com.bankingsystem.dto.response.AuthResponse;
import com.bankingsystem.entity.Account;
import com.bankingsystem.entity.Customer;
import com.bankingsystem.entity.User;
import com.bankingsystem.entity.enums.AccountStatus;
import com.bankingsystem.entity.enums.AccountType;
import com.bankingsystem.entity.enums.Role;
import com.bankingsystem.exception.BadRequestException;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.CustomerRepository;
import com.bankingsystem.repository.UserRepository;
import com.bankingsystem.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<ApiResponse> registerCustomer(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username is already taken");
        }
        if (customerRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already registered");
        }
        if (customerRepository.existsByMobile(request.mobile())) {
            throw new BadRequestException("Mobile number is already registered");
        }

        // 1. Customer Details
        Customer customer = Customer.builder()
                .fullName(request.fullName())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .email(request.email())
                .mobile(request.mobile())
                .address(request.address())
                .city(request.city())
                .state(request.state())
                .pinCode(request.pinCode())
                .status(Customer.Status.ACTIVE)
                .build();
        Customer savedCustomer = customerRepository.save(customer);

        // 2. User Credentials
        User user = User.builder()
                .customer(savedCustomer)
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_CUSTOMER)
                .build();
        userRepository.save(user);

        // 3. Bank Account
        String generatedAccNumber = "100" + (1000000 + new Random().nextInt(9000000));
        Account account = Account.builder()
                .customer(savedCustomer)
                .accountNumber(generatedAccNumber)
                .accountType(AccountType.SAVINGS)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();
        accountRepository.save(account);

        return ResponseEntity.ok(new ApiResponse(true, "Registration successful! Account Number: " + generatedAccNumber, generatedAccNumber));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Long customerId = user.getCustomer() != null ? user.getCustomer().getCustomerId() : null;

        String jwt = jwtUtils.generateJwtToken(userDetails, user.getRole().name(), customerId);

        return ResponseEntity.ok(new AuthResponse(jwt, user.getUsername(), user.getRole().name(), customerId, "Login Successful"));
    }
}
