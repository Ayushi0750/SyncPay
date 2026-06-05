package com.project.offline_payment_sync.controller;


import com.project.offline_payment_sync.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final UserService userService;

    public TestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/health")
    public String health() {
        return userService.testService();
    }
}