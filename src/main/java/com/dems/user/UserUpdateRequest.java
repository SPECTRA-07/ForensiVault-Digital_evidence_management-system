package com.dems.user;

import com.dems.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating user profile & role information (Admin only).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "User role is required")
    private UserRole role;

    private String department;

    private String designation;

    @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$", message = "Phone number must be valid digits between 7 and 15 characters")
    private String phoneNumber;
}
