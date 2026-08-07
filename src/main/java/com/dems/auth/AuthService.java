package com.dems.auth;

/**
 * Service interface for user authentication and JWT login processing.
 */
public interface AuthService {

    LoginResponse login(LoginRequest request);
}
