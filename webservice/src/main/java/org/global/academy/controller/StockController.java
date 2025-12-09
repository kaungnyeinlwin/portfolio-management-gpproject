package org.global.academy.controller;

import com.google.gson.Gson;
import org.global.academy.service.StockService;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

import static spark.Spark.*;

/**
 * Controller for stock-related endpoints.
 * 
 * Handles stock search and price lookup operations.
 * Provides public access to stock information.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class StockController {

    private final StockService stockService;
    private final Gson gson;

    /**
     * Constructs a new StockController.
     * 
     * @param stockService the stock service for business logic
     * @param gson         the Gson instance for JSON serialization
     */
    public StockController(StockService stockService, Gson gson) {
        this.stockService = stockService;
        this.gson = gson;
    }

    /**
     * Registers all stock routes.
     */
    public void registerRoutes() {
        get("/api/stocks", this::handleSearchStocks);
    }

    /**
     * Handles stock search requests.
     * 
     * @param req the request object
     * @param res the response object
     * @return JSON response with stock search results
     */
    private Object handleSearchStocks(Request req, Response res) {
        try {
            String query = req.queryParams("q");
            List<Map<String, Object>> stocks = stockService.searchStocks(query);

            res.type("application/json");
            return gson.toJson(Map.of("stocks", stocks));
        } catch (Exception e) {
            res.status(500);
            res.type("application/json");
            return gson.toJson(Map.of(
                    "error", "Error searching stocks: " + e.getMessage()));
        }
    }
}
