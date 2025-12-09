package org.global.academy.dto.request;

/**
 * Data Transfer Object for login and signup requests.
 * 
 * Used for both user authentication and new user registration.
 * Deserialized from JSON request body.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class LoginRequest {
    /** The username for authentication */
    public String username;

    /** The password for authentication */
    public String password;
}
