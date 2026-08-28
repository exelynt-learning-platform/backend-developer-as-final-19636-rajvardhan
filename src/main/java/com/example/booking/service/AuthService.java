package com.example.booking.service;

import com.example.booking.dto.auth.*;
import com.example.booking.entity.SystemUser;
import com.example.booking.entity.Role;
import com.example.booking.exception.BadRequestException;
import com.example.booking.repository.UserRepository;
import com.example.booking.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken
                (request.username(), request.password()));

        UserDetails user = userDetailsService.loadUserByUsername(request.username());

        return new LoginResponse(jwtService.generateToken(user), "Bearer");
    }

    public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already exists");
        }
        if(request.password() == null || request.password().length() < 8 || !request.password().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")) {
            throw new BadRequestException("Invalid password format");
        }
        SystemUser user = new SystemUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        userRepository.save(user);
        return new UserResponse(user.getId(), user.getUsername(), user.getRole().name());
    }

    public UserResponse registerAdmin(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already exists");
        }
        if(request.password() == null || request.password().length() < 8 || !request.password().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")) {
            throw new BadRequestException("Invalid password format");
        }
        SystemUser user = new SystemUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        return new UserResponse(user.getId(), user.getUsername(), user.getRole().name());
    }
}
