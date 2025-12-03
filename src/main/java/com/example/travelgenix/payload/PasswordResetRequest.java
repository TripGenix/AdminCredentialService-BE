package com.example.travelgenix.payload;


import lombok.Data;

@Data
public class PasswordResetRequest {
    private String email;
}