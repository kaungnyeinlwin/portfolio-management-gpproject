package org.global.academy.dto.response;

/**
 * Data Transfer Object for successful login responses.
 * 
 * Returned to the client upon successful authentication,
 * containing session information.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class LoginResponse {
    /** Authentication token (currently a placeholder) */
    public String token;

    /** The authenticated username */
    public String username;

    /**
     * Constructs a new LoginResponse.
     * 
     * @param token    the authentication token
     * @param username the username
     */
    public LoginResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }
}
