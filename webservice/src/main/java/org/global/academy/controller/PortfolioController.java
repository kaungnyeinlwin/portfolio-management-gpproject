package org.global.academy.controller;

import com.google.gson.Gson;
import org.global.academy.config.AppConfig;
import org.global.academy.dto.request.BuyStockRequest;
import org.global.academy.dto.response.ErrorResponse;
import org.global.academy.service.PortfolioService;
import spark.Request;
import spark.Response;

import java.util.Map;

import static spark.Spark.*;

/**
 * Controller for portfolio-related endpoints.
 * 
 * Handles portfolio viewing and stock purchase operations.
 * Requires user authentication for all operations.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final Gson gson;

    /**
     * Constructs a new PortfolioController.
     * 
     * @param portfolioService the portfolio service for business logic
     * @param gson             the Gson instance for JSON serialization
     */
    public PortfolioController(PortfolioService portfolioService, Gson gson) {
        this.portfolioService = portfolioService;
        this.gson = gson;
    }

    /**
     * Registers all portfolio routes.
     */
    public void registerRoutes() {
        get("/api/portfolio", this::handleGetPortfolio);
        post("/api/buy-stock", this::handleBuyStock);
    }

    /**
     * Handles portfolio retrieval requests.
     * 
     * @param req the request object
     * @param res the response object
     * @return JSON response with portfolio data
     */
    private Object handleGetPortfolio(Request req, Response res) {
        if (!isLoggedIn(req)) {
            res.status(401);
            res.type("application/json");
            return gson.toJson(new ErrorResponse("Not logged in"));
        }

        try {
            String username = req.session().attribute(AppConfig.USERNAME_KEY);
            Map<String, Object> portfolioData = portfolioService.getPortfolioData(username);

            res.type("application/json");
            return gson.toJson(portfolioData);
        } catch (Exception e) {
            res.status(500);
            res.type("application/json");
            return gson.toJson(new ErrorResponse("Error fetching portfolio: " + e.getMessage()));
        }
    }

    /**
     * Handles stock purchase requests.
     * 
     * @param req the request object
     * @param res the response object
     * @return JSON response with purchase result
     */
    private Object handleBuyStock(Request req, Response res) {
        if (!isLoggedIn(req)) {
            res.status(401);
            res.type("application/json");
            return gson.toJson(new ErrorResponse("Not logged in"));
        }

        try {
            BuyStockRequest buyRequest = gson.fromJson(req.body(), BuyStockRequest.class);
            String username = req.session().attribute(AppConfig.USERNAME_KEY);

            boolean success = portfolioService.buyStock(username, buyRequest);

            res.type("application/json");
            return gson.toJson(Map.of(
                    "success", success,
                    "message", "Stock purchased successfully"));
        } catch (Exception e) {
            res.status(500);
            res.type("application/json");
            return gson.toJson(new ErrorResponse("Error buying stock: " + e.getMessage()));
        }
    }

    /**
     * Checks if the current request has an authenticated session.
     * 
     * @param req the request object
     * @return true if user is logged in, false otherwise
     */
    private boolean isLoggedIn(Request req) {
        Boolean loggedIn = req.session(false) == null ? null : req.session().attribute(AppConfig.LOGGED_IN_KEY);
        return loggedIn != null && loggedIn;
    }
}
