package com.dems.auth;

import com.dems.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication Login Response DTO payload returning JWT access token and user claims.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    @Builder.Default
    private String type = "Bearer";
    private UserRole role;
    private String employeeId;
    private String fullName;
    private String email;
    private long expiresInMs;
}
