package org.global.academy;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class Server {
    // Key for the session attribute
    private static final String LOGGED_IN_KEY = "isLoggedIn";
    private static final Map<String, Portfolio> userPortfolios = new HashMap<>();
    private static final String STOCKDATA_API_TOKEN = "S2LH4DIxeG2KYas4RG7O6S1LfotrRvyOIO9njPVp";
    private static final String STOCKDATA_API_URL = "https://api.stockdata.org/v1/data/quote";

    public static void main(String[] args) {
        port(8080);
        staticFiles.location("/public");
        // Simple CORS
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*"); // or specific origin
            response.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
        });
        options("/*", (req, res) -> {
            String accessControlRequestHeaders = req.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                res.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                res.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        // 1. Declare the variable as final and initialize it immediately.
        final String welcomePageContent;
        try {
            // Read the content of welcome.html using the classpath (robust approach)
            // Assuming welcome.html is in src/main/resources/welcome.html
            java.io.InputStream stream = Server.class.getResourceAsStream("/welcome.html");
            if (stream == null) {
                // If the file is not found, throw an error to stop server startup.
                throw new IOException("Resource /welcome.html not found in classpath.");
            }
            welcomePageContent = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            // If the file cannot be read, log the error and halt the application startup.
            System.err.println("FATAL: Could not read welcome.html content.");
            e.printStackTrace();
            // This is cleaner than returning null; it ensures the server doesn't start
            // without key files.
            return;
        }

        Gson gson = new Gson();

        // Resolve holdings file: prefer the classpath resource '/public/holdings.json' when
        // it's available on the filesystem (during development). If the resource is inside
        // a jar (not writable) or missing, fall back to a local file './holdings.json'.
        final Path holdingsFilePath;
        Path temp = null;
        try {
            java.net.URL resourceUrl = Server.class.getResource("/public/holdings.json");
            if (resourceUrl != null && "file".equals(resourceUrl.getProtocol())) {
                temp = Paths.get(resourceUrl.toURI());
            }
        } catch (Exception e) {
            System.err.println("Could not resolve classpath holdings.json (URI), will use fallback: " + e.getMessage());
        }
        if (temp == null) {
            temp = Paths.get("holdings.json").toAbsolutePath();
        }
        holdingsFilePath = temp;

        // Load persisted holdings into memory on startup (if any)
        loadAllHoldings(gson, holdingsFilePath);

        // 2. The route can now safely use the final variable.
        // Ensure any access to welcome paths requires an active logged-in session
        before("/welcome", (req, res) -> {
            Boolean isLoggedIn = req.session(false) == null ? null : req.session().attribute(LOGGED_IN_KEY);
            if (isLoggedIn == null || !isLoggedIn) {
                res.redirect("/login.html");
                halt();
            }
        });
        before("/welcome.html", (req, res) -> {
            Boolean isLoggedIn = req.session(false) == null ? null : req.session().attribute(LOGGED_IN_KEY);
            if (isLoggedIn == null || !isLoggedIn) {
                res.redirect("/login.html");
                halt();
            }
        });
        before("/dashboard.html", (req, res) -> {
            Boolean isLoggedIn = req.session(false) == null ? null : req.session().attribute(LOGGED_IN_KEY);
            if (isLoggedIn == null || !isLoggedIn) {
                res.redirect("/login.html");
                halt();
            }
        });
        before("/buy-stocks.html", (req, res) -> {
            Boolean isLoggedIn = req.session(false) == null ? null : req.session().attribute(LOGGED_IN_KEY);
            if (isLoggedIn == null || !isLoggedIn) {
                res.redirect("/login.html");
                halt();
            }
        });
        get("/welcome", (req, res) -> {
            // filter already enforced session; just return content
            res.type("text/html");
            return welcomePageContent;
        });

        // Also serve the same secured page at /welcome.html so client redirects to that
        // path work
        get("/welcome.html", (req, res) -> {
            // filter already enforced session; just return content
            res.type("text/html");
            return welcomePageContent;
        });

        // Logout endpoint: invalidate server session and redirect to login
        get("/logout", (req, res) -> {
            // Do not create a session if none exists
            var s = req.session(false);
            if (s != null) {
                s.invalidate();
            }
            res.redirect("/login.html");
            return "";
        });

        get("/random", (req, res) -> {
            res.type("application/json");
            Random rand = new Random();
            int randomInt = rand.nextInt(10) + 1; // 1..10
            return gson.toJson(Map.of("number", randomInt));
        });

        // Portfolio endpoint: get user's portfolio with current prices and profit/loss
        get("/api/portfolio", (req, res) -> {
            Boolean isLoggedIn = req.session(false) == null ? null : req.session().attribute(LOGGED_IN_KEY);
            if (isLoggedIn == null || !isLoggedIn) {
                res.status(401);
                res.type("application/json");
                return gson.toJson(new ErrorResponse("Not logged in"));
            }

            String username = req.session().attribute("username");
            Portfolio portfolio = userPortfolios.get(username);

            if (portfolio == null) {
                portfolio = new Portfolio();
                userPortfolios.put(username, portfolio);
            }

            // Get aggregated holdings with quantities
            java.util.Map<String, java.util.Map<String, Object>> aggregatedMap = portfolio.getAggregatedHoldings();
            java.util.List<java.util.Map<String, Object>> holdings = new java.util.ArrayList<>(aggregatedMap.values());

            // Fetch current prices and enrich holdings with profit/loss
            Map<String, Double> currentPrices = fetchCurrentPricesBySymbol(
                    holdings.stream()
                            .map(h -> h.get("symbol").toString())
                            .collect(java.util.stream.Collectors.toList())
                            .toArray(new String[0]));

            for (java.util.Map<String, Object> holding : holdings) {
                String symbol = holding.get("symbol").toString();
                double currentPrice = currentPrices.getOrDefault(symbol, ((Number) holding.get("price")).doubleValue());
                double purchasePrice = ((Number) holding.get("purchasePrice")).doubleValue();
                int quantity = ((Number) holding.get("quantity")).intValue();

                double gain = (currentPrice - purchasePrice) * quantity;
                double gainPercent = purchasePrice > 0 ? ((currentPrice - purchasePrice) / purchasePrice) * 100 : 0;
                double currentValue = currentPrice * quantity;

                holding.put("currentPrice", currentPrice);
                holding.put("gain", gain);
                holding.put("gainPercent", gainPercent);
                holding.put("currentValue", currentValue);
            }

            // Calculate total portfolio value
            double totalValue = holdings.stream()
                    .mapToDouble(h -> ((Number) h.get("currentValue")).doubleValue())
                    .sum();

            res.type("application/json");
            return gson.toJson(Map.of(
                    "username", username,
                    "holdings", holdings,
                    "totalValue", totalValue));
        });

        // Stocks endpoint: get available stocks to buy with real prices
        get("/api/stocks", (req, res) -> {
            String[] symbols = { "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "META", "NVDA", "INTC", 
                                 "AMD", "CRM", "ADBE", "NFLX", "PYPL", "IBM", "CSCO", 
                                 "ORCL", "SAP", "INTU", "SHOP", "UBER" };
            java.util.List<Map<String, Object>> stocks = fetchRealStockPrices(symbols);

            res.type("application/json");
            return gson.toJson(Map.of("stocks", stocks));
        });

        // Buy stock endpoint
        post("/api/buy-stock", (req, res) -> {
            Boolean isLoggedIn = req.session(false) == null ? null : req.session().attribute(LOGGED_IN_KEY);
            if (isLoggedIn == null || !isLoggedIn) {
                res.status(401);
                res.type("application/json");
                return gson.toJson(new ErrorResponse("Not logged in"));
            }

            BuyStockRequest buyReq = gson.fromJson(req.body(), BuyStockRequest.class);
            String username = req.session().attribute("username");

            Portfolio portfolio = userPortfolios.get(username);
            if (portfolio == null) {
                portfolio = new Portfolio();
                userPortfolios.put(username, portfolio);
            }

            // Create and add stock to portfolio
            Stock newStock = new Stock(buyReq.name, buyReq.symbol, "NASDAQ", buyReq.price);
            portfolio.addStock(newStock, buyReq.quantity);

            // Persist the updated holdings after purchase
            saveAllHoldings(gson, holdingsFilePath);

            res.type("application/json");
            return gson.toJson(Map.of("success", true, "message", "Stock purchased successfully"));
        });

        // 3. Update /login route
        post("/login", (req, res) -> {
            System.out.println("Received /login request with body: " + req.body());
            LoginRequest lr = gson.fromJson(req.body(), LoginRequest.class);

            if ("alice".equals(lr.username) && "secret".equals(lr.password)) {

                // *** SUCCESSFUL LOGIN: SET SESSION FLAG ***
                req.session(true).attribute(LOGGED_IN_KEY, true);
                req.session().attribute("username", lr.username);

                // Initialize portfolio for user if not exists
                if (!userPortfolios.containsKey(lr.username)) {
                    Portfolio portfolio = new Portfolio();
                    // Start with empty portfolio (0 holdings)
                    userPortfolios.put(lr.username, portfolio);
                }

                res.type("application/json");
                // In a real app, you would also redirect the client-side after this response
                return gson.toJson(new LoginResponse("a-fake-token", lr.username));
            } else {
                // Failed login
                res.status(401);
                res.type("application/json");
                return gson.toJson(new ErrorResponse("Invalid credentials"));
            }
        });
    }

    static class LoginRequest {
        String username;
        String password;
    }

    static class LoginResponse {
        String token;
        String username;

        LoginResponse(String t, String u) {
            token = t;
            username = u;
        }
    }

    static class ErrorResponse {
        String error;

        ErrorResponse(String e) {
            error = e;
        }
    }

    static class BuyStockRequest {
        String symbol;
        String name;
        double price;
        int quantity;
    }

    // Helper method to fetch real stock prices from StockData API
    private static java.util.List<Map<String, Object>> fetchRealStockPrices(String[] symbols) {
        java.util.List<Map<String, Object>> stocks = new java.util.ArrayList<>();

        try {
            String symbolsParam = String.join(",", symbols);
            String url = STOCKDATA_API_URL + "?symbols=" + symbolsParam + "&api_token=" + STOCKDATA_API_TOKEN;

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);

                if (jsonResponse.has("data") && jsonResponse.get("data").isJsonArray()) {
                    JsonArray dataArray = jsonResponse.getAsJsonArray("data");

                    for (int i = 0; i < dataArray.size(); i++) {
                        JsonObject stock = dataArray.get(i).getAsJsonObject();

                        Map<String, Object> stockData = new HashMap<>();
                        stockData.put("symbol", stock.get("ticker").getAsString());
                        stockData.put("name", stock.get("name").getAsString());
                        stockData.put("price", stock.get("price").getAsDouble());

                        stocks.add(stockData);
                    }
                }
            } else {
                // Fallback to sample data if API call fails
                stocks = getFallbackStockData();
            }
        } catch (Exception e) {
            System.err.println("Error fetching stock prices: " + e.getMessage());
            // Fallback to sample data if there's any error
            stocks = getFallbackStockData();
        }

        return stocks;
    }

    // Fetch current prices for a list of symbols and return as a map
    private static Map<String, Double> fetchCurrentPricesBySymbol(String[] symbols) {
        Map<String, Double> priceMap = new HashMap<>();

        if (symbols == null || symbols.length == 0) {
            return priceMap;
        }

        try {
            String symbolsParam = String.join(",", symbols);
            String url = STOCKDATA_API_URL + "?symbols=" + symbolsParam + "&api_token=" + STOCKDATA_API_TOKEN;

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);

                if (jsonResponse.has("data") && jsonResponse.get("data").isJsonArray()) {
                    JsonArray dataArray = jsonResponse.getAsJsonArray("data");

                    for (int i = 0; i < dataArray.size(); i++) {
                        JsonObject stock = dataArray.get(i).getAsJsonObject();
                        String symbol = stock.get("ticker").getAsString();
                        double price = stock.get("price").getAsDouble();
                        priceMap.put(symbol, price);
                    }
                }
            } else {
                // Fallback: use sample prices if API fails
                populateFallbackPrices(priceMap, symbols);
            }
        } catch (Exception e) {
            System.err.println("Error fetching current prices: " + e.getMessage());
            // Fallback: use sample prices if there's any error
            populateFallbackPrices(priceMap, symbols);
        }

        return priceMap;
    }

    // Populate price map with fallback prices
    private static void populateFallbackPrices(Map<String, Double> priceMap, String[] symbols) {
        Map<String, Double> fallbackPrices = new HashMap<>();
        fallbackPrices.put("AAPL", 190.0);
        fallbackPrices.put("MSFT", 420.0);
        fallbackPrices.put("GOOGL", 140.0);
        fallbackPrices.put("AMZN", 180.0);
        fallbackPrices.put("TSLA", 250.0);
        fallbackPrices.put("META", 480.0);
        fallbackPrices.put("NVDA", 875.0);
        fallbackPrices.put("INTC", 45.0);
        fallbackPrices.put("AMD", 150.0);
        fallbackPrices.put("CRM", 210.0);
        fallbackPrices.put("ADBE", 520.0);
        fallbackPrices.put("NFLX", 250.0);
        fallbackPrices.put("PYPL", 95.0);
        fallbackPrices.put("IBM", 180.0);
        fallbackPrices.put("CSCO", 48.0);
        fallbackPrices.put("ORCL", 140.0);
        fallbackPrices.put("SAP", 110.0);
        fallbackPrices.put("INTU", 580.0);
        fallbackPrices.put("SHOP", 720.0);
        fallbackPrices.put("UBER", 75.0);

        for (String symbol : symbols) {
            if (fallbackPrices.containsKey(symbol)) {
                priceMap.put(symbol, fallbackPrices.get(symbol));
            }
        }
    }

    // Fallback method if API call fails
    private static java.util.List<Map<String, Object>> getFallbackStockData() {
        java.util.List<Map<String, Object>> stocks = new java.util.ArrayList<>();
        Stock[] availableStocks = {
                new Stock("Apple", "AAPL", "NASDAQ", 190.0),
                new Stock("Microsoft", "MSFT", "NASDAQ", 420.0),
                new Stock("Google", "GOOGL", "NASDAQ", 140.0),
                new Stock("Amazon", "AMZN", "NASDAQ", 180.0),
                new Stock("Tesla", "TSLA", "NASDAQ", 250.0),
                new Stock("Meta", "META", "NASDAQ", 480.0),
                new Stock("Nvidia", "NVDA", "NASDAQ", 875.0),
                new Stock("Intel", "INTC", "NASDAQ", 45.0),
                new Stock("AMD", "AMD", "NASDAQ", 150.0),
                new Stock("Salesforce", "CRM", "NYSE", 210.0),
                new Stock("Adobe", "ADBE", "NASDAQ", 520.0),
                new Stock("Netflix", "NFLX", "NASDAQ", 250.0),
                new Stock("PayPal", "PYPL", "NASDAQ", 95.0),
                new Stock("IBM", "IBM", "NYSE", 180.0),
                new Stock("Cisco", "CSCO", "NASDAQ", 48.0),
                new Stock("Oracle", "ORCL", "NYSE", 140.0),
                new Stock("SAP", "SAP", "NYSE", 110.0),
                new Stock("Intuit", "INTU", "NASDAQ", 580.0),
                new Stock("Shopify", "SHOP", "NYSE", 720.0),
                new Stock("Uber", "UBER", "NYSE", 75.0)
        };

        for (Stock stock : availableStocks) {
            Map<String, Object> stockData = new HashMap<>();
            stockData.put("symbol", stock.getSymbol());
            stockData.put("name", stock.getCompanyName());
            stockData.put("price", stock.getPrice());
            stocks.add(stockData);
        }

        return stocks;
    }

    // Load holdings from a JSON file into in-memory portfolios
    private static void loadAllHoldings(Gson gson, Path path) {
        try {
            if (!Files.exists(path)) {
                // Create an initial empty structure
                JsonObject root = new JsonObject();
                root.add("holdings", new JsonObject());
                root.addProperty("lastUpdate", "");
                Files.writeString(path, gson.toJson(root), java.nio.charset.StandardCharsets.UTF_8,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("Created new holdings file at: " + path.toAbsolutePath());
                return;
            }

            String content = Files.readString(path, java.nio.charset.StandardCharsets.UTF_8);
            if (content == null || content.isBlank()) {
                return;
            }

            JsonObject root = gson.fromJson(content, JsonObject.class);
            if (root == null || !root.has("holdings") || !root.get("holdings").isJsonObject()) {
                return;
            }

            JsonObject holdingsObj = root.getAsJsonObject("holdings");
            for (var entry : holdingsObj.entrySet()) {
                String username = entry.getKey();
                var jsonElement = entry.getValue();
                if (!jsonElement.isJsonArray()) continue;

                java.util.Iterator<com.google.gson.JsonElement> iter = jsonElement.getAsJsonArray().iterator();
                Portfolio p = new Portfolio();

                while (iter.hasNext()) {
                    JsonObject h = iter.next().getAsJsonObject();
                    String symbol = h.has("symbol") ? h.get("symbol").getAsString() : "";
                    String name = h.has("name") ? h.get("name").getAsString() : "";
                    double price = h.has("price") ? h.get("price").getAsDouble() : 0.0;
                    double purchasePrice = h.has("purchasePrice") ? h.get("purchasePrice").getAsDouble() : price;
                    int quantity = h.has("quantity") ? h.get("quantity").getAsInt() : 1;

                    Stock s = new Stock(name, symbol, "NASDAQ", price);
                    s.setPurchasePrice(purchasePrice);
                    p.addStock(s, quantity);
                }

                userPortfolios.put(username, p);
            }

            System.out.println("Loaded holdings from: " + path.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to load holdings: " + e.getMessage());
        }
    }

    // Save current in-memory portfolios to the JSON file
    private static void saveAllHoldings(Gson gson, Path path) {
        try {
            JsonObject root = new JsonObject();
            JsonObject holdingsObj = new JsonObject();

            for (var entry : userPortfolios.entrySet()) {
                String username = entry.getKey();
                Portfolio p = entry.getValue();
                java.util.Map<String, java.util.Map<String, Object>> agg = p.getAggregatedHoldings();

                JsonArray arr = new JsonArray();
                for (var data : agg.values()) {
                    JsonObject ho = new JsonObject();
                    if (data.get("symbol") != null)
                        ho.addProperty("symbol", data.get("symbol").toString());
                    if (data.get("name") != null)
                        ho.addProperty("name", data.get("name").toString());
                    if (data.get("quantity") != null)
                        ho.addProperty("quantity", ((Number) data.get("quantity")).intValue());
                    if (data.get("price") != null)
                        ho.addProperty("price", ((Number) data.get("price")).doubleValue());
                    if (data.get("purchasePrice") != null)
                        ho.addProperty("purchasePrice", ((Number) data.get("purchasePrice")).doubleValue());
                    if (data.get("totalPrice") != null)
                        ho.addProperty("totalPrice", ((Number) data.get("totalPrice")).doubleValue());
                    if (data.get("totalPurchasePrice") != null)
                        ho.addProperty("totalPurchasePrice", ((Number) data.get("totalPurchasePrice")).doubleValue());

                    arr.add(ho);
                }

                holdingsObj.add(username, arr);
            }

            root.add("holdings", holdingsObj);
            root.addProperty("lastUpdate", java.time.Instant.now().toString());

            Files.writeString(path, gson.toJson(root), java.nio.charset.StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Saved holdings to: " + path.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to save holdings: " + e.getMessage());
        }
    }
}
