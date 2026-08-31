package com.janhavi.apre.controller;

import com.janhavi.apre.dto.AuthResponse;
import com.janhavi.apre.dto.LoginRequest;
import com.janhavi.apre.dto.RegisterRequest;
import com.janhavi.apre.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest request) {

        String response = userService.register(request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", response);

        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                userService.login(request)
        );
    }
}