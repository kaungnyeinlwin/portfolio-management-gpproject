package org.global.academy;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
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
import java.util.Collections;
import java.util.stream.Collectors;
import java.lang.reflect.Type;

public class Server {
    // Key for the session attribute
    private static final String LOGGED_IN_KEY = "isLoggedIn";
    private static final Map<String, Portfolio> userPortfolios = new HashMap<>();
    
    // --- TWELVE DATA CONFIGURATION ---
    private static final String API_KEY = "949d886fb7d34956a4e435c5c84822b7"; 
    private static final String TWELVE_DATA_BASE_URL = "https://api.twelvedata.com";
    
    // In-memory users and stocks
    private static final List<User> users = new ArrayList<>();
    // This list holds the locally cached stock definitions (Symbol + Name)
    private static final List<StockReference> allAvailableStocks = new ArrayList<>();

    // --- DYNAMIC FALLBACK STORAGE ---
    // This Map stores the "Last Known Good Price". 
    // It starts with defaults, but updates every time we get a real API hit.
    private static final Map<String, Double> lastKnownPrices = new HashMap<>();
    
    static {
        // 1. Initialize with safe defaults (so the app works on very first run)
        lastKnownPrices.put("AAPL", 190.50);
        lastKnownPrices.put("MSFT", 425.00);
        lastKnownPrices.put("GOOGL", 175.00);
        lastKnownPrices.put("AMZN", 185.00);
        lastKnownPrices.put("TSLA", 240.00);
        lastKnownPrices.put("META", 500.00);
        lastKnownPrices.put("NVDA", 900.00);
        lastKnownPrices.put("INTC", 30.00);
        lastKnownPrices.put("AMD", 160.00);
        lastKnownPrices.put("NFLX", 600.00);
    }

    public static void main(String[] args) {
        port(8080);
        staticFiles.location("/public");
        
        // CORS Setup
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
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

        // Load Welcome Page
        final String welcomePageContent;
        try {
            java.io.InputStream stream = Server.class.getResourceAsStream("/welcome.html");
            if (stream == null) throw new IOException("Resource /welcome.html not found");
            welcomePageContent = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("FATAL: Could not read welcome.html content.");
            return;
        }

        Gson gson = new Gson();

        // --- FILE PATH RESOLUTION ---
        Path dataDir = resolveDataDirectory();
        
        final Path holdingsFilePath = dataDir.resolve("holdings.json");
        final Path usersFilePath = dataDir.resolve("users.json");
        final Path stocksFilePath = dataDir.resolve("stocks.json");

        // --- DATA LOADING ---
        loadAllHoldings(gson, holdingsFilePath);
        loadAllUsers(gson, usersFilePath);
        
        // Initialize Master Stock List (Reference Data)
        initializeStockMasterList(gson, stocksFilePath);

        // --- SECURITY FILTERS ---
        before("/welcome", (req, res) -> checkLogin(req, res));
        before("/welcome.html", (req, res) -> checkLogin(req, res));
        before("/dashboard.html", (req, res) -> checkLogin(req, res));
        before("/buy-stocks.html", (req, res) -> checkLogin(req, res));

        // --- ROUTES ---

        get("/welcome", (req, res) -> { res.type("text/html"); return welcomePageContent; });
        get("/welcome.html", (req, res) -> { res.type("text/html"); return welcomePageContent; });

        get("/logout", (req, res) -> {
            if (req.session(false) != null) req.session().invalidate();
            res.redirect("/login.html");
            return "";
        });

        // Portfolio endpoint
        get("/api/portfolio", (req, res) -> {
            if (!isLoggedIn(req)) { res.status(401); return gson.toJson(new ErrorResponse("Not logged in")); }

            String username = req.session().attribute("username");
            Portfolio portfolio = userPortfolios.computeIfAbsent(username, k -> new Portfolio());

            Map<String, Map<String, Object>> aggregatedMap = portfolio.getAggregatedHoldings();
            List<Map<String, Object>> holdings = new ArrayList<>(aggregatedMap.values());

            // Get live prices (Attempts Live -> Falls back to Last Known)
            List<String> symbols = holdings.stream()
                    .map(h -> h.get("symbol").toString())
                    .collect(Collectors.toList());
            
            Map<String, Double> currentPrices = fetchCurrentPricesTwelveData(symbols);

            for (Map<String, Object> holding : holdings) {
                String symbol = holding.get("symbol").toString();
                double currentPrice = currentPrices.getOrDefault(symbol, ((Number) holding.get("price")).doubleValue());
                double purchasePrice = ((Number) holding.get("purchasePrice")).doubleValue();
                int quantity = ((Number) holding.get("quantity")).intValue();

                double gain = (currentPrice - purchasePrice) * quantity;
                double currentValue = currentPrice * quantity;

                holding.put("currentPrice", currentPrice);
                holding.put("gain", gain);
                holding.put("currentValue", currentValue);
            }

            double totalValue = holdings.stream().mapToDouble(h -> ((Number) h.get("currentValue")).doubleValue()).sum();

            res.type("application/json");
            return gson.toJson(Map.of("username", username, "holdings", holdings, "totalValue", totalValue));
        });

        // Search Endpoint
        get("/api/stocks", (req, res) -> {
            String query = req.queryParams("q"); 
            List<StockReference> results;
            
            if (query == null || query.isBlank()) {
                results = allAvailableStocks.stream()
                    .filter(s -> List.of("AAPL", "MSFT", "TSLA", "GOOG", "AMZN", "NVDA").contains(s.symbol))
                    .collect(Collectors.toList());
            } else {
                String qLower = query.toLowerCase();
                results = allAvailableStocks.stream()
                    .filter(s -> s.symbol.toLowerCase().contains(qLower) || s.name.toLowerCase().contains(qLower))
                    .limit(20) 
                    .collect(Collectors.toList());
            }

            List<String> symbols = results.stream().map(s -> s.symbol).collect(Collectors.toList());
            Map<String, Double> prices = fetchCurrentPricesTwelveData(symbols);
            
            List<Map<String, Object>> responseList = new ArrayList<>();
            for(StockReference stock : results) {
                Map<String, Object> map = new HashMap<>();
                map.put("symbol", stock.symbol);
                map.put("name", stock.name);
                
                Double p = prices.get(stock.symbol);
                if (p != null) map.put("price", p);
                else map.put("price", "Unavailable");
                
                responseList.add(map);
            }

            res.type("application/json");
            return gson.toJson(Map.of("stocks", responseList));
        });

        // Buy stock endpoint
        post("/api/buy-stock", (req, res) -> {
            if (!isLoggedIn(req)) { res.status(401); return gson.toJson(new ErrorResponse("Not logged in")); }

            BuyStockRequest buyReq = gson.fromJson(req.body(), BuyStockRequest.class);
            String username = req.session().attribute("username");
            
            Portfolio portfolio = userPortfolios.computeIfAbsent(username, k -> new Portfolio());
            Stock newStock = new Stock(buyReq.name, buyReq.symbol, "US", buyReq.price);
            portfolio.addStock(newStock, buyReq.quantity);
            saveAllHoldings(gson, holdingsFilePath);

            res.type("application/json");
            return gson.toJson(Map.of("success", true, "message", "Stock purchased successfully"));
        });

        // Sign up
        post("/signup", (req, res) -> {
            try {
                LoginRequest signup = gson.fromJson(req.body(), LoginRequest.class);
                synchronized (users) {
                    if (users.stream().anyMatch(u -> u.getUsername().equals(signup.username))) {
                        res.status(409);
                        return gson.toJson(new ErrorResponse("User already exists"));
                    }
                    users.add(new User(signup.username, signup.password));
                    saveAllUsers(gson, usersFilePath);
                }
                res.status(201);
                return gson.toJson(Map.of("success", true, "message", "User created"));
            } catch (Exception ex) {
                res.status(500);
                return gson.toJson(new ErrorResponse("Error creating user"));
            }
        });

        // Login
        post("/login", (req, res) -> {
            LoginRequest lr = gson.fromJson(req.body(), LoginRequest.class);
            boolean authenticated = false;
            synchronized (users) {
                authenticated = users.stream()
                    .anyMatch(u -> u.getUsername().equals(lr.username) && u.getPassword().equals(lr.password));
            }

            if (authenticated) {
                req.session(true).attribute(LOGGED_IN_KEY, true);
                req.session().attribute("username", lr.username);
                userPortfolios.computeIfAbsent(lr.username, k -> new Portfolio());
                return gson.toJson(new LoginResponse("token", lr.username));
            } else {
                res.status(401);
                return gson.toJson(new ErrorResponse("Invalid credentials"));
            }
        });
    }

    // --- HELPER METHODS ---

    private static void checkLogin(spark.Request req, spark.Response res) {
        if (!isLoggedIn(req)) {
            res.redirect("/login.html");
            halt();
        }
    }

    private static boolean isLoggedIn(spark.Request req) {
        Boolean loggedIn = req.session(false) == null ? null : req.session().attribute(LOGGED_IN_KEY);
        return loggedIn != null && loggedIn;
    }
    
    private static void initializeStockMasterList(Gson gson, Path path) {
        try {
            boolean needDownload = true;
            if (Files.exists(path)) {
                java.time.Instant lastModified = Files.getLastModifiedTime(path).toInstant();
                long secondsOld = java.time.Duration.between(lastModified, java.time.Instant.now()).getSeconds();
                
                if (secondsOld < (24 * 60 * 60)) {
                    needDownload = false;
                    System.out.println("Loading stocks from local file (Cache age: " + secondsOld/3600 + " hours).");
                    String content = Files.readString(path);
                    Type listType = new TypeToken<List<StockReference>>() {}.getType();
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
                System.out.println("Downloading fresh stock list from Twelve Data API...");
                String url = TWELVE_DATA_BASE_URL + "/stocks?country=United%20States&exchange=NASDAQ&type=Common%20Stock";
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                    if (json.has("data")) {
                        JsonArray data = json.getAsJsonArray("data");
                        List<StockReference> fetchedStocks = new ArrayList<>();
                        for (JsonElement el : data) {
                            JsonObject obj = el.getAsJsonObject();
                            fetchedStocks.add(new StockReference(obj.get("symbol").getAsString(), obj.get("name").getAsString()));
                        }
                        allAvailableStocks.clear();
                        allAvailableStocks.addAll(fetchedStocks);
                        Files.writeString(path, gson.toJson(allAvailableStocks));
                        System.out.println("Downloaded and saved " + fetchedStocks.size() + " stocks.");
                    }
                } else {
                    System.err.println("Failed to download stock list. API Status: " + response.statusCode());
                    if (Files.exists(path)) {
                         String content = Files.readString(path);
                         Type listType = new TypeToken<List<StockReference>>() {}.getType();
                         List<StockReference> loaded = gson.fromJson(content, listType);
                         if (loaded != null) allAvailableStocks.addAll(loaded);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing stock list: " + e.getMessage());
        }
    }

    // ** STRATEGY: Try Live -> Update Memory -> If Fail, Use Memory **
    private static Map<String, Double> fetchCurrentPricesTwelveData(List<String> symbols) {
        Map<String, Double> results = new HashMap<>();
        if (symbols == null || symbols.isEmpty()) return results;
        
        List<String> validSymbols = symbols.stream().filter(s -> s != null && !s.isEmpty()).collect(Collectors.toList());
        if (validSymbols.isEmpty()) return results;

        try {
            // 1. Attempt to fetch LIVE data
            String symbolsParam = String.join(",", validSymbols);
            String url = TWELVE_DATA_BASE_URL + "/price?symbol=" + symbolsParam + "&apikey=" + API_KEY;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            boolean apiCallSuccess = false;

            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);

                // Check for Limit Reached
                if (json.has("code") && json.get("code").getAsInt() != 200) {
                    System.err.println("⚠️ API Error (Limit or Invalid): " + json.get("message").getAsString());
                    // Flag false so we drop to fallback below
                    apiCallSuccess = false; 
                } else {
                    apiCallSuccess = true;
                    // PARSE & UPDATE MEMORY
                    if (validSymbols.size() == 1) {
                        if (json.has("price")) {
                            double p = json.get("price").getAsDouble();
                            lastKnownPrices.put(validSymbols.get(0), p); // <--- UPDATE MEMORY
                            results.put(validSymbols.get(0), p);
                        }
                    } else {
                        for (String sym : validSymbols) {
                            if (json.has(sym)) {
                                JsonObject stockObj = json.getAsJsonObject(sym);
                                if (stockObj.has("price")) {
                                    double p = stockObj.get("price").getAsDouble();
                                    lastKnownPrices.put(sym, p); // <--- UPDATE MEMORY
                                    results.put(sym, p);
                                }
                            }
                        }
                    }
                }
            } else {
                System.err.println("⚠️ HTTP Error " + response.statusCode());
            }

            // 2. FALLBACK LOGIC
            // If the API call failed (or missed some symbols), fill gaps using "lastKnownPrices"
            for (String sym : validSymbols) {
                if (!results.containsKey(sym)) {
                    if (lastKnownPrices.containsKey(sym)) {
                        // System.out.println("Using cached/fallback price for: " + sym);
                        results.put(sym, lastKnownPrices.get(sym));
                    } else {
                        // Truly unknown stock (never seen before, API failed)
                        results.put(sym, 0.0);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error fetching prices: " + e.getMessage());
            // Total failure? Return everything from memory
            for (String sym : validSymbols) {
                 results.put(sym, lastKnownPrices.getOrDefault(sym, 0.0));
            }
        }
        
        return results;
    }

    private static Path resolveDataDirectory() {
        try {
            Path location = Paths.get(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(location)) location = location.getParent();
            Path current = location;
            while (current != null && !current.getFileName().toString().equalsIgnoreCase("webservice")) {
                current = current.getParent();
            }
            if (current == null) current = Paths.get(System.getProperty("user.dir"));
            Path dataDir = current.resolve("data");
            if (!Files.exists(dataDir)) Files.createDirectories(dataDir);
            return dataDir;
        } catch (Exception e) {
            return Paths.get("data"); 
        }
    }

    private static void loadAllHoldings(Gson gson, Path path) {
        if (!Files.exists(path)) return;
        try {
            JsonObject root = gson.fromJson(Files.readString(path), JsonObject.class);
            if (root != null && root.has("holdings")) {
                JsonObject holdingsObj = root.getAsJsonObject("holdings");
                for (String username : holdingsObj.keySet()) {
                    Portfolio p = new Portfolio();
                    for (JsonElement e : holdingsObj.getAsJsonArray(username)) {
                        JsonObject h = e.getAsJsonObject();
                        Stock s = new Stock(h.get("name").getAsString(), h.get("symbol").getAsString(), "US", h.get("price").getAsDouble());
                        if (h.has("purchasePrice")) s.setPurchasePrice(h.get("purchasePrice").getAsDouble());
                        p.addStock(s, h.get("quantity").getAsInt());
                    }
                    userPortfolios.put(username, p);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void saveAllHoldings(Gson gson, Path path) {
        try {
            JsonObject root = new JsonObject();
            JsonObject holdingsObj = new JsonObject();
            for (var entry : userPortfolios.entrySet()) {
                JsonArray arr = new JsonArray();
                for (var data : entry.getValue().getAggregatedHoldings().values()) {
                    JsonObject ho = gson.toJsonTree(data).getAsJsonObject();
                    arr.add(ho);
                }
                holdingsObj.add(entry.getKey(), arr);
            }
            root.add("holdings", holdingsObj);
            Files.writeString(path, gson.toJson(root));
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private static void loadAllUsers(Gson gson, Path path) {
        if (!Files.exists(path)) return;
        try {
            JsonObject root = gson.fromJson(Files.readString(path), JsonObject.class);
            if (root != null && root.has("users")) {
                List<User> loaded = gson.fromJson(root.get("users"), new TypeToken<List<User>>(){}.getType());
                users.addAll(loaded);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void saveAllUsers(Gson gson, Path path) {
        try {
            JsonObject root = new JsonObject();
            root.add("users", gson.toJsonTree(users));
            Files.writeString(path, gson.toJson(root));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- DTO CLASSES ---

    private static class StockReference {
        String symbol;
        String name;
        public StockReference(String symbol, String name) { this.symbol = symbol; this.name = name; }
    }
    private static class ErrorResponse {
        public String message;
        public ErrorResponse(String m) { this.message = m; }
    }
    private static class BuyStockRequest { public String name; public String symbol; public double price; public int quantity; }
    private static class LoginRequest { public String username; public String password; }
    private static class LoginResponse { public String token; public String username; public LoginResponse(String t, String u) { token=t; username=u; }}
}