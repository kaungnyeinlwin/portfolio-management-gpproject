package org.global.academy;

import static spark.Spark.*;

import com.google.gson.Gson;
import org.global.academy.config.AppConfig;
import org.global.academy.controller.AuthController;
import org.global.academy.controller.PortfolioController;
import org.global.academy.controller.StockController;
import org.global.academy.repository.PortfolioRepository;
import org.global.academy.repository.StockRepository;
import org.global.academy.repository.UserRepository;
import org.global.academy.service.PortfolioService;
import org.global.academy.service.StockService;
import org.global.academy.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main server class for the Portfolio Management Web Application.
 * 
 * This class bootstraps the application by:
 * - Configuring the Spark web server
 * - Initializing repositories, services, and controllers
 * - Registering all routes
 * - Setting up CORS and security filters
 * 
 * The application follows the MVC (Model-View-Controller) pattern with
 * additional Service and Repository layers for better separation of concerns.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class Server {

    /**
     * Main entry point for the Portfolio Management Server.
     * 
     * Initializes the Spark web server, configures routes, loads persistent data,
     * and starts listening for HTTP requests.
     * 
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // === SERVER CONFIGURATION ===
        port(AppConfig.SERVER_PORT);
        staticFiles.location(AppConfig.STATIC_FILES_LOCATION);

        // === CORS SETUP ===
        configureCORS();

        // === LOAD WELCOME PAGE ===
        final String welcomePageContent = loadWelcomePage();
        if (welcomePageContent == null) {
            System.err.println("FATAL: Could not load welcome.html. Exiting.");
            return;
        }

        // === INITIALIZE DEPENDENCIES ===
        Gson gson = new Gson();
        Path dataDir = resolveDataDirectory();

        // Initialize Repositories
        UserRepository userRepository = new UserRepository(
                gson,
                dataDir.resolve("users.json"));

        PortfolioRepository portfolioRepository = new PortfolioRepository(
                gson,
                dataDir.resolve("holdings.json"));

        StockRepository stockRepository = new StockRepository(
                gson,
                dataDir.resolve("stocks.json"));

        // Initialize Services
        UserService userService = new UserService(userRepository);
        PortfolioService portfolioService = new PortfolioService(
                portfolioRepository,
                stockRepository);
        StockService stockService = new StockService(stockRepository);

        // Initialize Controllers
        AuthController authController = new AuthController(
                userService,
                portfolioService,
                gson);

        PortfolioController portfolioController = new PortfolioController(
                portfolioService,
                gson);

        StockController stockController = new StockController(
                stockService,
                gson);

        // === SECURITY FILTERS ===
        before("/welcome", (req, res) -> checkLogin(req, res));
        before("/welcome.html", (req, res) -> checkLogin(req, res));
        before("/dashboard.html", (req, res) -> checkLogin(req, res));
        before("/buy-stocks.html", (req, res) -> checkLogin(req, res));

        // === REGISTER ROUTES ===

        // Welcome page routes
        get("/welcome", (req, res) -> {
            res.type("text/html");
            return welcomePageContent;
        });

        get("/welcome.html", (req, res) -> {
            res.type("text/html");
            return welcomePageContent;
        });

        // Register controller routes
        authController.registerRoutes();
        portfolioController.registerRoutes();
        stockController.registerRoutes();

        System.out.println("🚀 Portfolio Management Server started on port " +
                AppConfig.SERVER_PORT);
        System.out.println("📊 Access the application at: http://localhost:" +
                AppConfig.SERVER_PORT);
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) headers.
     * Allows the frontend to make requests from different origins.
     */
    private static void configureCORS() {
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
    }

    /**
     * Loads the welcome page HTML content from resources.
     * 
     * @return the HTML content as a string, or null if loading fails
     */
    private static String loadWelcomePage() {
        try {
            InputStream stream = Server.class.getResourceAsStream("/welcome.html");
            if (stream == null) {
                throw new IOException("Resource /welcome.html not found");
            }
            return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error loading welcome.html: " + e.getMessage());
            return null;
        }
    }

    /**
     * Security filter that checks if a user is logged in.
     * 
     * If the user is not authenticated, redirects to the login page
     * and halts request processing.
     * 
     * @param req the Spark request object
     * @param res the Spark response object
     */
    private static void checkLogin(spark.Request req, spark.Response res) {
        Boolean loggedIn = req.session(false) == null ? null : req.session().attribute(AppConfig.LOGGED_IN_KEY);

        if (loggedIn == null || !loggedIn) {
            res.redirect("/login.html");
            halt();
        }
    }

    /**
     * Resolves the data directory path for storing persistent data files.
     * 
     * This method attempts to find the 'webservice' directory in the project
     * structure and creates a 'data' subdirectory within it. If the webservice
     * directory cannot be found, falls back to using the current working directory.
     * 
     * The data directory is used to store:
     * - users.json (user accounts)
     * - holdings.json (portfolio data)
     * - stocks.json (cached stock list)
     * 
     * @return Path object pointing to the data directory
     */
    private static Path resolveDataDirectory() {
        try {
            Path location = Paths.get(
                    Server.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI());

            if (Files.isRegularFile(location)) {
                location = location.getParent();
            }

            Path current = location;
            while (current != null &&
                    !current.getFileName().toString().equalsIgnoreCase("webservice")) {
                current = current.getParent();
            }

            if (current == null) {
                current = Paths.get(System.getProperty("user.dir"));
            }

            Path dataDir = current.resolve("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            return dataDir;
        } catch (Exception e) {
            System.err.println("Error resolving data directory: " + e.getMessage());
            return Paths.get("data");
        }
    }
}