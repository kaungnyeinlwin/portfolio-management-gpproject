package org.global.academy.model;

/**
 * Represents a user account in the portfolio management system.
 * 
 * This class stores user credentials and is used for authentication
 * and session management throughout the application.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class User {
    /** The unique username for this user account */
    private String username;

    /**
     * The password for this user account (stored in plain text - consider hashing
     * in production)
     */
    private String password;

    /**
     * Default no-argument constructor.
     * Required for JSON deserialization with Gson.
     */
    public User() {
    }

    /**
     * Constructs a new User with the specified username and password.
     * 
     * @param username the unique username for this user
     * @param password the password for this user account
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the username of this user.
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of this user.
     * 
     * @param u the new username to set
     */
    public void setUsername(String u) {
        this.username = u;
    }

    /**
     * Gets the password of this user.
     * 
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of this user.
     * 
     * @param p the new password to set
     */
    public void setPassword(String p) {
        this.password = p;
    }
}
