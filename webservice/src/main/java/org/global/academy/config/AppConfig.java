package org.global.academy.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Application configuration class containing constants and default values.
 * 
 * Centralizes all configuration settings including API credentials,
 * URLs, and default stock prices for fallback scenarios.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class AppConfig {

    // === API CONFIGURATION ===
    /**
     * API key for accessing Twelve Data stock market API (hardcoded for demo
     * purposes)
     */
    public static final String TWELVE_DATA_API_KEY = "949d886fb7d34956a4e435c5c84822b7";

    /** Base URL for Twelve Data API endpoints */
    public static final String TWELVE_DATA_BASE_URL = "https://api.twelvedata.com";

    // === SESSION CONFIGURATION ===
    /** Session attribute key used to track user login status */
    public static final String LOGGED_IN_KEY = "isLoggedIn";

    /** Session attribute key for storing username */
    public static final String USERNAME_KEY = "username";

    // === SERVER CONFIGURATION ===
    /** Port number for the web server */
    public static final int SERVER_PORT = 8080;

    /** Directory for static files */
    public static final String STATIC_FILES_LOCATION = "/public";

    // === CACHE CONFIGURATION ===
    /** Maximum age of stock cache in seconds (24 hours) */
    public static final long STOCK_CACHE_MAX_AGE_SECONDS = 24 * 60 * 60;

    // === DEFAULT STOCK PRICES ===
    /**
     * Default stock prices used as fallback when API is unavailable.
     * These prices are updated dynamically when successful API calls are made.
     */
    private static final Map<String, Double> DEFAULT_STOCK_PRICES = new HashMap<>();

    static {
        // Initialize with reasonable default prices for popular stocks
        DEFAULT_STOCK_PRICES.put("AAPL", 190.50);
        DEFAULT_STOCK_PRICES.put("MSFT", 425.00);
        DEFAULT_STOCK_PRICES.put("GOOGL", 175.00);
        DEFAULT_STOCK_PRICES.put("AMZN", 185.00);
        DEFAULT_STOCK_PRICES.put("TSLA", 240.00);
        DEFAULT_STOCK_PRICES.put("META", 500.00);
        DEFAULT_STOCK_PRICES.put("NVDA", 900.00);
        DEFAULT_STOCK_PRICES.put("INTC", 30.00);
        DEFAULT_STOCK_PRICES.put("AMD", 160.00);
        DEFAULT_STOCK_PRICES.put("NFLX", 600.00);
    }

    /**
     * Gets the default stock prices map.
     * 
     * @return a copy of the default stock prices to prevent external modification
     */
    public static Map<String, Double> getDefaultStockPrices() {
        return new HashMap<>(DEFAULT_STOCK_PRICES);
    }

    /**
     * Gets the default price for a specific stock symbol.
     * 
     * @param symbol the stock symbol
     * @return the default price, or 0.0 if not found
     */
    public static double getDefaultPrice(String symbol) {
        return DEFAULT_STOCK_PRICES.getOrDefault(symbol, 0.0);
    }
}
