package com.dems.user;

import com.dems.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data Initializer running on application startup to ensure a default ADMIN user exists.
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("No users found in database. Seeding default system ADMIN account...");

            UserEntity admin = UserEntity.builder()
                    .employeeId("EMP-ADMIN-001")
                    .fullName("System Administrator")
                    .email("admin@dems.gov")
                    .password(passwordEncoder.encode("Admin@123456"))
                    .role(UserRole.ADMIN)
                    .department("IT & Security")
                    .designation("System Admin")
                    .phoneNumber("+1234567890")
                    .active(true)
                    .build();

            userRepository.save(admin);
            log.info("Default ADMIN account created successfully with email [admin@dems.gov]");
        }
    }
}
