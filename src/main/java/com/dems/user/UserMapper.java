package com.dems.user;

import org.springframework.stereotype.Component;

/**
 * Component for mapping between User entity models and DTO payloads.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserResponse.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployeeId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .role(entity.getRole())
                .department(entity.getDepartment())
                .designation(entity.getDesignation())
                .phoneNumber(entity.getPhoneNumber())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserEntity toEntity(UserCreateRequest request, String encodedPassword) {
        if (request == null) {
            return null;
        }
        return UserEntity.builder()
                .employeeId(request.getEmployeeId().trim())
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(encodedPassword)
                .role(request.getRole())
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .phoneNumber(request.getPhoneNumber())
                .active(true)
                .build();
    }
}
