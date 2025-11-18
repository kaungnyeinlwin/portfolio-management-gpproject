package org.global.academy;

import static spark.Spark.*;

import com.google.gson.Gson;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;

import java.util.Map;
import java.util.Random;

public class Server {
    // Key for the session attribute
    private static final String LOGGED_IN_KEY = "isLoggedIn";

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
            // This is cleaner than returning null; it ensures the server doesn't start without key files.
            return; 
        }
        

        Gson gson = new Gson();

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
        get("/welcome", (req, res) -> {
            // filter already enforced session; just return content
            res.type("text/html");
            return welcomePageContent;
        });
        
        // Also serve the same secured page at /welcome.html so client redirects to that path work
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

        // 3. Update /login route
        post("/login", (req, res) -> {
            System.out.println("Received /login request with body: " + req.body());
            LoginRequest lr = gson.fromJson(req.body(), LoginRequest.class);
            
            if ("alice".equals(lr.username) && "secret".equals(lr.password)) {
                
                // *** SUCCESSFUL LOGIN: SET SESSION FLAG ***
                req.session(true).attribute(LOGGED_IN_KEY, true);
                
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
}
