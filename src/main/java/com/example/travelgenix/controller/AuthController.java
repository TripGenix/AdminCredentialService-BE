package com.example.travelgenix.controller;

import com.example.travelgenix.model.User;
import com.example.travelgenix.payload.*;
import com.example.travelgenix.repository.UserRepository;
import com.example.travelgenix.security.JwtService;
import com.example.travelgenix.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import com.example.travelgenix.payload.UpdateProfileRequest;
import com.example.travelgenix.payload.ChangePasswordRequest;
import org.springframework.transaction.annotation.Transactional;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtService jwtService;
    @Autowired private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return new ResponseEntity<>("Email is already in use!", HttpStatus.BAD_REQUEST);
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(newUser);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtService.generateToken(authentication);

        AuthResponse response = new AuthResponse(
                jwt,
                newUser.getUsername(),
                newUser.getEmail()
        );

        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtService.generateToken(authentication);

        //User user = (User) authentication.getPrincipal();
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found after successful authentication."));


        AuthResponse response = new AuthResponse(
                jwt,
                user.getUsername(),
                user.getEmail()
        );

        return ResponseEntity.ok(response);
    }


    @PostMapping("/password-reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setTokenExpiryDate(Instant.now().plusSeconds(3600));
            userRepository.save(user);

            try {
                emailService.sendResetEmail(user.getEmail(), token);
            } catch (Exception e) {
                // Log and swallow email error for security
                System.err.println("Failed to send email: " + e.getMessage());
            }
        }
        return ResponseEntity.ok("If a matching account exists, a password reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody NewPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByResetToken(request.getToken());

        if (userOptional.isEmpty() || userOptional.get().getTokenExpiryDate().isBefore(Instant.now())) {
            return new ResponseEntity<>("Invalid or expired reset token.", HttpStatus.BAD_REQUEST);
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);

        return ResponseEntity.ok("Password has been successfully reset. You may now log in.");
    }

    /**
     * Update profile details (username and/or email).
     */

    @PutMapping("/update")
    @Transactional
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        String currentEmail = authentication.getName();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String newEmail = request.getEmail() != null ? request.getEmail().trim() : null;
        String newUsername = request.getUsername() != null ? request.getUsername().trim() : null;

        if (newEmail != null && !newEmail.isEmpty() && !newEmail.equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use by another account.");
            }
            user.setEmail(newEmail);
        }

        if (newUsername != null && !newUsername.isEmpty()) {
            user.setUsername(newUsername);
        }

        userRepository.save(user);

        // Return updated user info (no password). You can return AuthResponse if you prefer to include a fresh token.
        AuthResponse resp = new AuthResponse(null, user.getUsername(), user.getEmail());
        return ResponseEntity.ok(resp);
    }

    /**
     * Change password for current user. Validates current password.
     */
    @PutMapping("/password")
    @Transactional
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest request) {
        String currentEmail = authentication.getName();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getCurrentPassword() == null || request.getNewPassword() == null) {
            return ResponseEntity.badRequest().body("Both currentPassword and newPassword are required.");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Current password does not match.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("New password must be different from the current password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully.");
    }

    @DeleteMapping("/delete")
    @Transactional
    public ResponseEntity<?> deleteAccount(Authentication authentication) {
        String currentEmail = authentication.getName();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.delete(user);

        return ResponseEntity.ok("Account deleted successfully.");
    }

}