package org.global.academy.controller;

import com.google.gson.Gson;
import org.global.academy.config.AppConfig;
import org.global.academy.dto.request.LoginRequest;
import org.global.academy.dto.response.ErrorResponse;
import org.global.academy.dto.response.LoginResponse;
import org.global.academy.service.PortfolioService;
import org.global.academy.service.UserService;
import spark.Request;
import spark.Response;

import java.util.Map;

import static spark.Spark.*;

/**
 * Controller for authentication-related endpoints.
 * 
 * Handles user login, signup, and logout operations.
 * Manages user sessions and authentication state.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class AuthController {

    private final UserService userService;
    private final PortfolioService portfolioService;
    private final Gson gson;

    /**
     * Constructs a new AuthController.
     * 
     * @param userService      the user service for authentication logic
     * @param portfolioService the portfolio service for initializing user
     *                         portfolios
     * @param gson             the Gson instance for JSON serialization
     */
    public AuthController(UserService userService,
            PortfolioService portfolioService,
            Gson gson) {
        this.userService = userService;
        this.portfolioService = portfolioService;
        this.gson = gson;
    }

    /**
     * Registers all authentication routes.
     */
    public void registerRoutes() {
        post("/login", this::handleLogin);
        post("/signup", this::handleSignup);
        get("/logout", this::handleLogout);
    }

    /**
     * Handles user login requests.
     * 
     * @param req the request object
     * @param res the response object
     * @return JSON response with login result
     */
    private Object handleLogin(Request req, Response res) {
        try {
            LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
            LoginResponse loginResponse = userService.authenticate(loginRequest);

            if (loginResponse != null) {
                // Set session attributes
                req.session(true).attribute(AppConfig.LOGGED_IN_KEY, true);
                req.session().attribute(AppConfig.USERNAME_KEY, loginRequest.username);

                // Initialize portfolio for user
                portfolioService.getPortfolio(loginRequest.username);

                res.type("application/json");
                return gson.toJson(loginResponse);
            } else {
                res.status(401);
                res.type("application/json");
                return gson.toJson(new ErrorResponse("Invalid credentials"));
            }
        } catch (Exception e) {
            res.status(500);
            res.type("application/json");
            return gson.toJson(new ErrorResponse("Login error: " + e.getMessage()));
        }
    }

    /**
     * Handles user signup requests.
     * 
     * @param req the request object
     * @param res the response object
     * @return JSON response with signup result
     */
    private Object handleSignup(Request req, Response res) {
        try {
            LoginRequest signupRequest = gson.fromJson(req.body(), LoginRequest.class);

            if (userService.userExists(signupRequest.username)) {
                res.status(409);
                res.type("application/json");
                return gson.toJson(new ErrorResponse("User already exists"));
            }

            boolean success = userService.registerUser(signupRequest);

            if (success) {
                res.status(201);
                res.type("application/json");
                return gson.toJson(Map.of(
                        "success", true,
                        "message", "User created"));
            } else {
                res.status(500);
                res.type("application/json");
                return gson.toJson(new ErrorResponse("Error creating user"));
            }
        } catch (Exception e) {
            res.status(500);
            res.type("application/json");
            return gson.toJson(new ErrorResponse("Signup error: " + e.getMessage()));
        }
    }

    /**
     * Handles user logout requests.
     * 
     * @param req the request object
     * @param res the response object
     * @return empty string (redirects to login page)
     */
    private Object handleLogout(Request req, Response res) {
        if (req.session(false) != null) {
            req.session().invalidate();
        }
        res.redirect("/login.html");
        return "";
    }
}
