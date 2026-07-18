package com.janhavi.apre.service;

import com.janhavi.apre.dto.AuthResponse;
import com.janhavi.apre.dto.LoginRequest;
import com.janhavi.apre.dto.RegisterRequest;
import com.janhavi.apre.entity.User;
import com.janhavi.apre.enums.Role;
import com.janhavi.apre.exception.DuplicateUserException;
import com.janhavi.apre.repository.UserRepository;
import com.janhavi.apre.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ===========================
    // Register User
    // ===========================
    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email already registered");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getRole() == null) {
            user.setRole(Role.ANALYST);
        } else {
            user.setRole(request.getRole());
        }

        userRepository.save(user);

        return "User registered successfully";
    }

    // ===========================
    // Login User
    // ===========================
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                "Login successful",
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}