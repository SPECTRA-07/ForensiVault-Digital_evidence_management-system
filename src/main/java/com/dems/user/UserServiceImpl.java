package com.dems.user;

import com.dems.audit.AuditService;
import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import com.dems.exception.ConflictException;
import com.dems.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Implementation of UserService managing User lifecycle operations with constructor injection and audit log integration.
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String normalizedEmployeeId = request.getEmployeeId().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("User creation failed: Email [{}] already registered", normalizedEmail);
            throw new ConflictException("A user with email '" + normalizedEmail + "' already exists.");
        }

        if (userRepository.existsByEmployeeId(normalizedEmployeeId)) {
            log.warn("User creation failed: Employee ID [{}] already exists", normalizedEmployeeId);
            throw new ConflictException("A user with employee ID '" + normalizedEmployeeId + "' already exists.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        UserEntity entity = userMapper.toEntity(request, encodedPassword);
        UserEntity savedEntity = userRepository.save(entity);

        log.info("User created successfully: ID [{}], Employee ID [{}], Role [{}]",
                savedEntity.getId(), savedEntity.getEmployeeId(), savedEntity.getRole());

        auditService.recordEvent(
                AuditAction.CREATE,
                AuditEntityType.USER,
                "USER",
                savedEntity.getEmail(),
                savedEntity.getId(),
                AuditStatus.SUCCESS,
                "Created user record for: " + savedEntity.getEmail()
        );

        return userMapper.toResponse(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return userMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        String prevDetails = "Role=" + entity.getRole() + ", Dept=" + entity.getDepartment();

        entity.setFullName(request.getFullName().trim());
        entity.setRole(request.getRole());
        entity.setDepartment(request.getDepartment());
        entity.setDesignation(request.getDesignation());
        entity.setPhoneNumber(request.getPhoneNumber());

        UserEntity updatedEntity = userRepository.save(entity);
        String newDetails = "Role=" + updatedEntity.getRole() + ", Dept=" + updatedEntity.getDepartment();

        log.info("User updated successfully: ID [{}], Employee ID [{}], New Role [{}]",
                updatedEntity.getId(), updatedEntity.getEmployeeId(), updatedEntity.getRole());

        auditService.recordEventWithDiff(
                AuditAction.UPDATE,
                AuditEntityType.USER,
                "USER",
                updatedEntity.getEmail(),
                updatedEntity.getId(),
                prevDetails,
                newDetails,
                AuditStatus.SUCCESS,
                "Updated user details for: " + updatedEntity.getEmail()
        );

        return userMapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional
    public UserResponse setUserStatus(Long id, Boolean active) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        String prevStatus = "active=" + entity.getActive();
        entity.setActive(active);
        UserEntity updatedEntity = userRepository.save(entity);
        String newStatus = "active=" + updatedEntity.getActive();

        if (Boolean.FALSE.equals(active)) {
            log.info("User disabled: ID [{}], Employee ID [{}]", updatedEntity.getId(), updatedEntity.getEmployeeId());
        } else {
            log.info("User activated: ID [{}], Employee ID [{}]", updatedEntity.getId(), updatedEntity.getEmployeeId());
        }

        auditService.recordEventWithDiff(
                AuditAction.STATUS_CHANGE,
                AuditEntityType.USER,
                "USER",
                updatedEntity.getEmail(),
                updatedEntity.getId(),
                prevStatus,
                newStatus,
                AuditStatus.SUCCESS,
                "User status changed to active=" + active + " for email: " + updatedEntity.getEmail()
        );

        return userMapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> {
                    log.warn("User details lookup failed: Username/Email [{}] not found", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });
    }
}
