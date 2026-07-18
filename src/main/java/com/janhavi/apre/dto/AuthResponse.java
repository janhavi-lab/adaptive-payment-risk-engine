package com.janhavi.apre.dto;

public class AuthResponse {

    private String token;
    private String message;
    private String name;
    private String email;
    private String role;

    public AuthResponse() {
    }

    public AuthResponse(String token, String message, String name, String email, String role) {
        this.token = token;
        this.message = message;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}