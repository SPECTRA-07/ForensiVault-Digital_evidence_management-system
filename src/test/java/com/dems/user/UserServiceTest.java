package com.dems.user;

import com.dems.enums.UserRole;
import com.dems.exception.ConflictException;
import com.dems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.dems.audit.AuditService auditService;

    @InjectMocks
    private UserServiceImpl userService;

    private UserCreateRequest createRequest;
    private UserEntity userEntity;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        createRequest = UserCreateRequest.builder()
                .employeeId("EMP-100")
                .fullName("John Doe")
                .email("john@dems.gov")
                .password("Password@123")
                .role(UserRole.POLICE_OFFICER)
                .department("Homicide")
                .designation("Detective")
                .phoneNumber("+1234567890")
                .build();

        userEntity = UserEntity.builder()
                .id(10L)
                .employeeId("EMP-100")
                .fullName("John Doe")
                .email("john@dems.gov")
                .password("encodedPass")
                .role(UserRole.POLICE_OFFICER)
                .active(true)
                .build();

        userResponse = UserResponse.builder()
                .id(10L)
                .employeeId("EMP-100")
                .fullName("John Doe")
                .email("john@dems.gov")
                .role(UserRole.POLICE_OFFICER)
                .active(true)
                .build();
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail("john@dems.gov")).thenReturn(false);
        when(userRepository.existsByEmployeeId("EMP-100")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPass");
        when(userMapper.toEntity(eq(createRequest), eq("encodedPass"))).thenReturn(userEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toResponse(userEntity)).thenReturn(userResponse);

        UserResponse result = userService.createUser(createRequest);

        assertNotNull(result);
        assertEquals("john@dems.gov", result.getEmail());
        assertEquals("EMP-100", result.getEmployeeId());
    }

    @Test
    void createUser_DuplicateEmail_ThrowsConflictException() {
        when(userRepository.existsByEmail("john@dems.gov")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(createRequest));
    }

    @Test
    void getUserById_NotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void setUserStatus_Deactivate_Success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        userResponse.setActive(false);
        when(userMapper.toResponse(userEntity)).thenReturn(userResponse);

        UserResponse result = userService.setUserStatus(10L, false);

        assertNotNull(result);
        assertFalse(result.getActive());
        verify(userRepository).save(userEntity);
    }
}
