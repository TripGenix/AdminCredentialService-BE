package com.example.travelgenix.payload;

import lombok.Data;

@Data
public class NewPasswordRequest {
    private String token;
    private String newPassword;
}