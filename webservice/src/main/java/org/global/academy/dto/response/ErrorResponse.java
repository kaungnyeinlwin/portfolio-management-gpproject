package org.global.academy.dto.response;

/**
 * Data Transfer Object for error responses.
 * 
 * Used to return consistent error messages to the client
 * in JSON format.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class ErrorResponse {
    /** The error message to display to the user */
    public String message;

    /**
     * Constructs a new ErrorResponse.
     * 
     * @param message the error message
     */
    public ErrorResponse(String message) {
        this.message = message;
    }
}
