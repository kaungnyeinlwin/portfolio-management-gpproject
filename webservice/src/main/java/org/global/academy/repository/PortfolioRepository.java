package org.global.academy.repository;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.global.academy.model.Portfolio;
import org.global.academy.model.Stock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository class for Portfolio data persistence.
 * 
 * Handles loading and saving portfolio data to/from JSON files.
 * Manages user portfolios with their stock holdings.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class PortfolioRepository {

    private final Gson gson;
    private final Path holdingsFilePath;
    private final Map<String, Portfolio> userPortfolios;

    /**
     * Constructs a new PortfolioRepository.
     * 
     * @param gson             the Gson instance for JSON
     *                         serialization/deserialization
     * @param holdingsFilePath the file path where portfolio data is stored
     */
    public PortfolioRepository(Gson gson, Path holdingsFilePath) {
        this.gson = gson;
        this.holdingsFilePath = holdingsFilePath;
        this.userPortfolios = new HashMap<>();
        loadAllHoldings();
    }

    /**
     * Loads all user portfolio holdings from the JSON file.
     * 
     * Reads the holdings.json file and reconstructs each user's portfolio
     * with their stock holdings. If the file doesn't exist, this method
     * does nothing (starting with empty portfolios).
     */
    private void loadAllHoldings() {
        if (!Files.exists(holdingsFilePath)) {
            return;
        }

        try {
            String content = Files.readString(holdingsFilePath);
            JsonObject root = gson.fromJson(content, JsonObject.class);

            if (root != null && root.has("holdings")) {
                JsonObject holdingsObj = root.getAsJsonObject("holdings");

                for (String username : holdingsObj.keySet()) {
                    Portfolio portfolio = new Portfolio();
                    JsonArray holdings = holdingsObj.getAsJsonArray(username);

                    for (JsonElement element : holdings) {
                        JsonObject holding = element.getAsJsonObject();

                        Stock stock = new Stock(
                                holding.get("name").getAsString(),
                                holding.get("symbol").getAsString(),
                                "US",
                                holding.get("price").getAsDouble());

                        if (holding.has("purchasePrice")) {
                            stock.setPurchasePrice(holding.get("purchasePrice").getAsDouble());
                        }

                        int quantity = holding.get("quantity").getAsInt();
                        portfolio.addStock(stock, quantity);
                    }

                    userPortfolios.put(username, portfolio);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading holdings: " + e.getMessage());
        }
    }

    /**
     * Saves all user portfolio holdings to the JSON file.
     * 
     * Persists the current state of all user portfolios to holdings.json.
     * This method should be called whenever a user buys or sells stocks.
     */
    public void saveAllHoldings() {
        try {
            JsonObject root = new JsonObject();
            JsonObject holdingsObj = new JsonObject();

            for (Map.Entry<String, Portfolio> entry : userPortfolios.entrySet()) {
                JsonArray arr = new JsonArray();

                for (Map<String, Object> data : entry.getValue().getAggregatedHoldings().values()) {
                    JsonObject holdingObj = gson.toJsonTree(data).getAsJsonObject();
                    arr.add(holdingObj);
                }

                holdingsObj.add(entry.getKey(), arr);
            }

            root.add("holdings", holdingsObj);
            Files.writeString(holdingsFilePath, gson.toJson(root));
        } catch (IOException e) {
            System.err.println("Error saving holdings: " + e.getMessage());
        }
    }

    /**
     * Gets the portfolio for a specific user.
     * Creates a new empty portfolio if one doesn't exist.
     * 
     * @param username the username
     * @return the user's portfolio
     */
    public Portfolio getPortfolio(String username) {
        return userPortfolios.computeIfAbsent(username, k -> new Portfolio());
    }

    /**
     * Checks if a user has a portfolio.
     * 
     * @param username the username
     * @return true if portfolio exists, false otherwise
     */
    public boolean hasPortfolio(String username) {
        return userPortfolios.containsKey(username);
    }
}
