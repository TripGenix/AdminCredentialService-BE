package com.example.travelgenix.security;

import com.example.travelgenix.model.User;
import com.example.travelgenix.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Find the user in the database using the email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. Return a Spring Security UserDetails object
        // The UserDetails object needs the username (email), password (hashed), and authorities/roles.
        // Since this is a simple admin login, we use an empty list for authorities.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),          // The principal (username/email)
                user.getPassword(),       // The stored, hashed password
                Collections.emptyList()   // User roles/authorities (none needed for basic JWT setup)
        );
    }
}