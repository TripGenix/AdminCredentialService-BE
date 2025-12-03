package com.example.travelgenix.service;


import com.example.travelgenix.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final UserRepository adminRepository;

    // Constructor Injection (Spring automatically provides AdminRepository)
    public AdminService(UserRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public boolean isAnyAdminRegistered() {

        return adminRepository.count() > 0;
    }
}