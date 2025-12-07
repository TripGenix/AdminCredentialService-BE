package com.example.travelgenix.payload;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    private String email;
}