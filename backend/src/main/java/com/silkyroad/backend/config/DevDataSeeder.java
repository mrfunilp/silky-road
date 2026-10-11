package com.silkyroad.backend.config;

import com.silkyroad.backend.user.dto.CreateUserRequest;
import com.silkyroad.backend.user.UserRepository;
import com.silkyroad.backend.user.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DevDataSeeder {

    @Bean
    ApplicationRunner seedAdminUser(UserService userService, UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userService.register(new CreateUserRequest("admin", "admin@example.com", "admin123"));
            }
        };
    }
}
