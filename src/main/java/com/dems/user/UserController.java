package com.dems.user;

import com.dems.common.ApiResponse;
import com.dems.enums.UserRole;
import com.dems.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for DEMS User Account Management.
 */
@Tag(name = "User Management", description = "Endpoints for creating, updating, retrieving, and toggling user accounts")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create a new user account (Admin only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User account created successfully"));
    }

    @Operation(summary = "Get all registered users (Admin only)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @Operation(summary = "Get user details by ID (Admin or Self)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        UserResponse user = userService.getUserById(id);

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRole.ADMIN.name()));
        boolean isSelf = currentUser.getUsername().equalsIgnoreCase(user.getEmail());

        if (!isAdmin && !isSelf) {
            throw new UnauthorizedException("Access denied: You are only permitted to view your own profile.");
        }

        return ResponseEntity.ok(ApiResponse.success(user, "User details retrieved successfully"));
    }

    @Operation(summary = "Update user details by ID (Admin only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User account updated successfully"));
    }

    @Operation(summary = "Activate or Deactivate user account (Admin only)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> setUserStatus(
            @PathVariable Long id,
            @RequestParam Boolean active) {
        UserResponse response = userService.setUserStatus(id, active);
        String statusMessage = Boolean.TRUE.equals(active) ? "User account activated" : "User account deactivated";
        return ResponseEntity.ok(ApiResponse.success(response, statusMessage));
    }
}
