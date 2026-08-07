package com.dems.auth;

import com.dems.audit.AuditService;
import com.dems.enums.UserRole;
import com.dems.exception.UnauthorizedException;
import com.dems.user.UserEntity;
import com.dems.user.UserMapper;
import com.dems.user.UserRepository;
import com.dems.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity testUser;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .id(1L)
                .employeeId("EMP-001")
                .fullName("Test Officer")
                .email("officer@dems.gov")
                .password("encodedPassword")
                .role(UserRole.POLICE_OFFICER)
                .active(true)
                .build();

        testUserResponse = UserResponse.builder()
                .id(1L)
                .employeeId("EMP-001")
                .email("officer@dems.gov")
                .role(UserRole.POLICE_OFFICER)
                .build();
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("officer@dems.gov", "Password@123");

        when(userRepository.findByEmail("officer@dems.gov")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("mock-jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals("EMP-001", response.getEmployeeId());
        assertEquals(UserRole.POLICE_OFFICER, response.getRole());
    }

    @Test
    void login_InvalidPassword_ThrowsUnauthorizedException() {
        LoginRequest request = new LoginRequest("officer@dems.gov", "WrongPassword");

        when(userRepository.findByEmail("officer@dems.gov")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }
}
