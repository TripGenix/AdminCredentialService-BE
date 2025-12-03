package com.example.travelgenix.payload;



import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private final String type = "Bearer"; // Fixed type for JWT standard

    public AuthResponse(String token) {
        this.token = token;
    }
}