package com.example.booking.controller;

import com.example.booking.utility.BaseResponse;
import com.example.booking.dto.auth.*;
import com.example.booking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/create-user")
    public ResponseEntity<BaseResponse<UserResponse>> createUser(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.registerUser(request);
        return ResponseEntity.ok(BaseResponse.success("User created successfully", response));
    }

    @PostMapping("/create-admin")
    public ResponseEntity<BaseResponse<UserResponse>> createAdmin(
            @Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.registerAdmin(request);
        return ResponseEntity.ok(BaseResponse.success("Admin created successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
