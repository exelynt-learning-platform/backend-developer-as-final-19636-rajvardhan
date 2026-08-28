package com.example.booking.dto.auth;

public record UserResponse(
        Long id,
        String username,
        String role) {
}
