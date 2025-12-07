package com.example.travelgenix.payload;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private final String type = "Bearer";

    public AuthResponse(String token,String username,String email) {

        this.token = token;
        this.username = username;
        this.email = email;
    }
}