package com.dems.user;

import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

/**
 * Service interface for User account management and authentication detail loading.
 */
public interface UserService extends UserDetailsService {

    UserResponse createUser(UserCreateRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    UserResponse setUserStatus(Long id, Boolean active);
}
