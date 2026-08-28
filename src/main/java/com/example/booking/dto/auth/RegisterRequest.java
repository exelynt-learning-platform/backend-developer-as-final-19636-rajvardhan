package com.example.booking.dto.auth;

public record RegisterRequest(
        String username,
        String password) {
}