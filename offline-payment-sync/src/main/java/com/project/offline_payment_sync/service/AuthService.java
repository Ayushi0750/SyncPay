
package com.project.offline_payment_sync.service;

import com.project.offline_payment_sync.dto.LoginRequest;
import com.project.offline_payment_sync.dto.LoginResponse;
import com.project.offline_payment_sync.dto.RegisterRequest;
import com.project.offline_payment_sync.entity.MockBankAccount;
import com.project.offline_payment_sync.entity.Role;
import com.project.offline_payment_sync.entity.User;
import com.project.offline_payment_sync.repository.MockBankAccountRepository;
import com.project.offline_payment_sync.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MockBankAccountRepository mockBankAccountRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       MockBankAccountRepository mockBankAccountRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mockBankAccountRepository = mockBankAccountRepository;
    }

    public void register(RegisterRequest request) {

        System.out.println(" [AUTH SERVICE] Registering user: " + request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setBalance(0.0);
        user.setRole(Role.ROLE_USER);

        User savedUser = userRepository.save(user);
        System.out.println("User saved: " + savedUser.getEmail());

        // Create Mock Bank Account
        MockBankAccount bankAccount = MockBankAccount.builder()
                .user(savedUser)
                .accountNumber(generateAccountNumber())
                .balance(100000.0)
                .build();

        mockBankAccountRepository.save(bankAccount);
        System.out.println(" Mock Bank Account created: " + bankAccount.getAccountNumber());
    }

    public LoginResponse login(LoginRequest request) {

        System.out.println("\n [AUTH SERVICE] Login attempt: " + request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String userRole =
                (user.getRole() != null)
                        ? user.getRole().name()
                        : "ROLE_USER";

        System.out.println(" User authenticated: " + user.getEmail());
        System.out.println(" Role: " + userRole);

        String token = jwtService.generateToken(
                user.getEmail(),
                userRole
        );

        System.out.println(" Token generated");
        System.out.println("   Length: " + token.length() + " characters");
        System.out.println("   First 50 chars: " + token.substring(0, Math.min(50, token.length())) + "...");

        // Token expires in 24 hours = 86400000 milliseconds
        Long expiresIn = 1000L * 60 * 60 * 24;

        LoginResponse response = new LoginResponse(
                user.getEmail(),
                token,
                userRole,
                expiresIn
        );

        System.out.println(" Login successful!\n");

        return response;
    }

    private String generateAccountNumber() {

        return "MBA-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }
}