package com.example.booking.dto.auth;

public record LoginResponse(
        String token,
        String tokenType) {
}
