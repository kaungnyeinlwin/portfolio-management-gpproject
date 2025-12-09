package org.global.academy.service;

import org.global.academy.dto.request.LoginRequest;
import org.global.academy.dto.response.ErrorResponse;
import org.global.academy.dto.response.LoginResponse;
import org.global.academy.model.User;
import org.global.academy.repository.UserRepository;

/**
 * Service class for user-related business logic.
 * 
 * Handles user authentication, registration, and account management.
 * Acts as an intermediary between controllers and repositories.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class UserService {

    private final UserRepository userRepository;

    /**
     * Constructs a new UserService.
     * 
     * @param userRepository the user repository for data access
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Authenticates a user with the provided credentials.
     * 
     * @param request the login request containing username and password
     * @return LoginResponse if successful, null if authentication fails
     */
    public LoginResponse authenticate(LoginRequest request) {
        boolean authenticated = userRepository.authenticate(
                request.username,
                request.password);

        if (authenticated) {
            return new LoginResponse("token", request.username);
        }

        return null;
    }

    /**
     * Registers a new user account.
     * 
     * @param request the signup request containing username and password
     * @return true if registration successful, false if user already exists
     */
    public boolean registerUser(LoginRequest request) {
        User newUser = new User(request.username, request.password);
        return userRepository.save(newUser);
    }

    /**
     * Checks if a user exists with the given username.
     * 
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Finds a user by username.
     * 
     * @param username the username to search for
     * @return the User if found, null otherwise
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
