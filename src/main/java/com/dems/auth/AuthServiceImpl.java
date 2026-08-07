package com.dems.auth;

import com.dems.audit.AuditService;
import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import com.dems.exception.UnauthorizedException;
import com.dems.user.UserEntity;
import com.dems.user.UserMapper;
import com.dems.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Implementation of AuthService handling user login authentication and JWT token issuance.
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuditService auditService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserMapper userMapper,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        log.info("Authentication attempt for user email: [{}]", normalizedEmail);

        UserEntity user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Failed login attempt for user email [{}] - User not found", normalizedEmail);
                    auditService.recordEvent(
                            AuditAction.LOGIN,
                            AuditEntityType.AUTHENTICATION,
                            "AUTH",
                            normalizedEmail,
                            null,
                            AuditStatus.FAILED,
                            "Login failed: User not found",
                            "User not found"
                    );
                    return new UnauthorizedException("Invalid credentials.");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for user email [{}] - Invalid credentials", normalizedEmail);
            auditService.recordEvent(
                    AuditAction.LOGIN,
                    AuditEntityType.AUTHENTICATION,
                    "AUTH",
                    normalizedEmail,
                    user.getId(),
                    AuditStatus.FAILED,
                    "Login failed: Invalid password",
                    "Invalid password"
            );
            throw new UnauthorizedException("Invalid credentials.");
        }

        if (!user.isEnabled()) {
            log.warn("Failed login attempt for user email [{}] - Account disabled", normalizedEmail);
            auditService.recordEvent(
                    AuditAction.LOGIN,
                    AuditEntityType.AUTHENTICATION,
                    "AUTH",
                    normalizedEmail,
                    user.getId(),
                    AuditStatus.FAILED,
                    "Login failed: Account disabled",
                    "Account disabled"
            );
            throw new UnauthorizedException("User account is disabled.");
        }

        String token = jwtService.generateToken(user);

        log.info("Successful login for user email [{}], Employee ID [{}], Role [{}]",
                user.getEmail(), user.getEmployeeId(), user.getRole());

        auditService.recordEvent(
                AuditAction.LOGIN,
                AuditEntityType.AUTHENTICATION,
                "AUTH",
                user.getEmail(),
                user.getId(),
                AuditStatus.SUCCESS,
                "Successful login for user: " + user.getEmail()
        );

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .role(user.getRole())
                .employeeId(user.getEmployeeId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .expiresInMs(86400000L)
                .build();
    }
}
