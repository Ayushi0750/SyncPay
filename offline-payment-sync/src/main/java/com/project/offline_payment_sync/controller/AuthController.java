package com.project.offline_payment_sync.controller;

import com.project.offline_payment_sync.dto.LoginRequest;
import com.project.offline_payment_sync.dto.LoginResponse;
import com.project.offline_payment_sync.dto.RegisterRequest;  
import com.project.offline_payment_sync.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {  
        authService.register(request);
        return "user registered successfully";
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/test")
    public String test() {
        return "working";
    }
}