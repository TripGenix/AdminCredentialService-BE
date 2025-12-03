package com.example.travelgenix.controller;


import com.example.travelgenix.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {


    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/hello")
    public String getHello(Authentication authentication) {
        return "Hello, Admin " + authentication.getName() + "! You successfully accessed a protected resource.";
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> adminExists() {

        boolean exists = adminService.isAnyAdminRegistered();

        return ResponseEntity.ok(exists);
    }
}