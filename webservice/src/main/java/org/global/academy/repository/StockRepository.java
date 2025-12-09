package org.global.academy.repository;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.global.academy.config.AppConfig;
import org.global.academy.dto.response.StockReference;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Repository class for Stock data management.
 * 
 * Handles stock reference data caching, price fetching from external API,
 * and fallback price management. Implements smart caching to minimize
 * API calls while keeping data reasonably current.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class StockRepository {

    private final Gson gson;
    private final Path stocksFilePath;
    private final List<StockReference> allAvailableStocks;
    private final Map<String, Double> lastKnownPrices;

    /**
     * Constructs a new StockRepository.
     * 
     * @param gson           the Gson instance for JSON
     *                       serialization/deserialization
     * @param stocksFilePath the file path where stock data is cached
     */
    public StockRepository(Gson gson, Path stocksFilePath) {
        this.gson = gson;
        this.stocksFilePath = stocksFilePath;
        this.allAvailableStocks = new ArrayList<>();
        this.lastKnownPrices = new HashMap<>(AppConfig.getDefaultStockPrices());
        initializeStockMasterList();
    }

    /**
     * Initializes the master list of available stocks from NASDAQ.
     * 
     * This method implements a smart caching strategy:
     * 1. Checks if a local cache file exists and is less than 24 hours old
     * 2. If cache is valid, loads from file
     * 3. If cache is stale or missing, downloads fresh data from Twelve Data API
     * 4. Saves downloaded data to cache file for future use
     * 
     * This approach minimizes API calls while keeping stock data reasonably
     * current.
     */
    private void initializeStockMasterList() {
        try {
            boolean needDownload = true;

            if (Files.exists(stocksFilePath)) {
                Instant lastModified = Files.getLastModifiedTime(stocksFilePath).toInstant();
                long secondsOld = Duration.between(lastModified, Instant.now()).getSeconds();

                if (secondsOld < AppConfig.STOCK_CACHE_MAX_AGE_SECONDS) {
                    needDownload = false;
                    System.out.println("Loading stocks from local file (Cache age: " +
                            secondsOld / 3600 + " hours).");

                    String content = Files.readString(stocksFilePath);
                    Type listType = new TypeToken<List<StockReference>>() {
                    }.getType();
                    List<StockReference> loaded = gson.fromJson(content, listType);

                    if (loaded != null && !loaded.isEmpty()) {
                        allAvailableStocks.addAll(loaded);
                        return;
                    }
                } else {
                    System.out.println("Local stock file is outdated (>24h). Refetching...");
                }
            }

            if (needDownload) {
                downloadStockList();
            }
        } catch (Exception e) {
            System.err.println("Error initializing stock list: " + e.getMessage());
        }
    }

    /**
     * Downloads the stock list from Twelve Data API.
     */
    private void downloadStockList() {
        try {
            System.out.println("Downloading fresh stock list from Twelve Data API...");

            String url = AppConfig.TWELVE_DATA_BASE_URL +
                    "/stocks?country=United%20States&exchange=NASDAQ&type=Common%20Stock";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);

                if (json.has("data")) {
                    JsonArray data = json.getAsJsonArray("data");
                    List<StockReference> fetchedStocks = new ArrayList<>();

                    for (JsonElement el : data) {
                        JsonObject obj = el.getAsJsonObject();
                        fetchedStocks.add(new StockReference(
                                obj.get("symbol").getAsString(),
                                obj.get("name").getAsString()));
                    }

                    allAvailableStocks.clear();
                    allAvailableStocks.addAll(fetchedStocks);
                    Files.writeString(stocksFilePath, gson.toJson(allAvailableStocks));
                    System.out.println("Downloaded and saved " + fetchedStocks.size() + " stocks.");
                }
            } else {
                System.err.println("Failed to download stock list. API Status: " +
                        response.statusCode());
                loadFromCacheAsFallback();
            }
        } catch (Exception e) {
            System.err.println("Error downloading stock list: " + e.getMessage());
            loadFromCacheAsFallback();
        }
    }

    /**
     * Loads stock list from cache file as fallback.
     */
    private void loadFromCacheAsFallback() {
        try {
            if (Files.exists(stocksFilePath)) {
                String content = Files.readString(stocksFilePath);
                Type listType = new TypeToken<List<StockReference>>() {
                }.getType();
                List<StockReference> loaded = gson.fromJson(content, listType);

                if (loaded != null) {
                    allAvailableStocks.addAll(loaded);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading from cache: " + e.getMessage());
        }
    }

    /**
     * Gets all available stocks.
     * 
     * @return list of all stock references
     */
    public List<StockReference> getAllStocks() {
        return new ArrayList<>(allAvailableStocks);
    }

    /**
     * Searches for stocks by query string.
     * 
     * @param query the search query (matches symbol or name)
     * @param limit maximum number of results
     * @return list of matching stock references
     */
    public List<StockReference> searchStocks(String query, int limit) {
        if (query == null || query.isBlank()) {
            return allAvailableStocks.stream()
                    .filter(s -> List.of("AAPL", "MSFT", "TSLA", "GOOG", "AMZN", "NVDA")
                            .contains(s.symbol))
                    .collect(Collectors.toList());
        }

        String qLower = query.toLowerCase();
        return allAvailableStocks.stream()
                .filter(s -> s.symbol.toLowerCase().contains(qLower) ||
                        s.name.toLowerCase().contains(qLower))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Fetches current stock prices from Twelve Data API with intelligent fallback.
     * 
     * Strategy:
     * 1. Attempts to fetch live prices from Twelve Data API
     * 2. If successful, updates the lastKnownPrices cache
     * 3. If API fails or is rate-limited, falls back to cached prices
     * 4. Returns 0.0 for completely unknown stocks
     * 
     * @param symbols list of stock symbols to fetch prices for
     * @return map of stock symbols to their current prices
     */
    public Map<String, Double> fetchCurrentPrices(List<String> symbols) {
        Map<String, Double> results = new HashMap<>();

        if (symbols == null || symbols.isEmpty()) {
            return results;
        }

        List<String> validSymbols = symbols.stream()
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toList());

        if (validSymbols.isEmpty()) {
            return results;
        }

        try {
            // Attempt to fetch live data
            String symbolsParam = String.join(",", validSymbols);
            String url = AppConfig.TWELVE_DATA_BASE_URL + "/price?symbol=" +
                    symbolsParam + "&apikey=" + AppConfig.TWELVE_DATA_API_KEY;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);

                // Check for API errors
                if (json.has("code") && json.get("code").getAsInt() != 200) {
                    System.err.println("⚠️ API Error: " + json.get("message").getAsString());
                } else {
                    // Parse and update cache
                    if (validSymbols.size() == 1) {
                        if (json.has("price")) {
                            double price = json.get("price").getAsDouble();
                            String symbol = validSymbols.get(0);
                            lastKnownPrices.put(symbol, price);
                            results.put(symbol, price);
                        }
                    } else {
                        for (String sym : validSymbols) {
                            if (json.has(sym)) {
                                JsonObject stockObj = json.getAsJsonObject(sym);
                                if (stockObj.has("price")) {
                                    double price = stockObj.get("price").getAsDouble();
                                    lastKnownPrices.put(sym, price);
                                    results.put(sym, price);
                                }
                            }
                        }
                    }
                }
            } else {
                System.err.println("⚠️ HTTP Error " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error fetching prices: " + e.getMessage());
        }

        // Fill gaps with cached prices
        for (String sym : validSymbols) {
            if (!results.containsKey(sym)) {
                results.put(sym, lastKnownPrices.getOrDefault(sym, 0.0));
            }
        }

        return results;
    }

    /**
     * Gets the last known price for a stock symbol.
     * 
     * @param symbol the stock symbol
     * @return the last known price, or 0.0 if not found
     */
    public double getLastKnownPrice(String symbol) {
        return lastKnownPrices.getOrDefault(symbol, 0.0);
    }
}
