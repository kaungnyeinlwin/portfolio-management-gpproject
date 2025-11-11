package org.global.academy;

import static spark.Spark.*;

import com.google.gson.Gson;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;

import java.util.Map;
import java.util.Random;

public class Server {
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

        Gson gson = new Gson();

        get("/random", (req, res) -> {
            res.type("application/json");
            Random rand = new Random();
            int randomInt = rand.nextInt(10) + 1; // 1..10
            return gson.toJson(Map.of("number", randomInt));
        });

        post("/login", (req, res) -> {
            System.out.println("Received /login request with body: " + req.body());
            LoginRequest lr = gson.fromJson(req.body(), LoginRequest.class);
            // TODO: validate username/password
            if ("alice".equals(lr.username) && "secret".equals(lr.password)) {
                res.type("application/json");
                return gson.toJson(new LoginResponse("a-fake-token", lr.username));
            } else {
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
