package com.example.booking.config;

import com.example.booking.entity.SystemUser;
import com.example.booking.entity.Role;
import com.example.booking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("Admin")) {
            SystemUser admin = new SystemUser();
            admin.setUsername("Admin");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("User")) {
            SystemUser user = new SystemUser();
            user.setUsername("User");
            user.setPassword(passwordEncoder.encode("User@123"));
            user.setRole(Role.USER);
            userRepository.save(user);
        }
    }
}
