package com.dems.user;

import com.dems.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Safe User Response DTO exposing user attributes without sensitive password fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String employeeId;
    private String fullName;
    private String email;
    private UserRole role;
    private String department;
    private String designation;
    private String phoneNumber;
    private Boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
