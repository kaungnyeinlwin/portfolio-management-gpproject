package org.global.academy.repository;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.global.academy.model.User;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for User data persistence.
 * 
 * Handles loading and saving user data to/from JSON files.
 * Implements the Repository pattern to separate data access logic
 * from business logic.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class UserRepository {

    private final Gson gson;
    private final Path usersFilePath;
    private final List<User> users;

    /**
     * Constructs a new UserRepository.
     * 
     * @param gson          the Gson instance for JSON serialization/deserialization
     * @param usersFilePath the file path where user data is stored
     */
    public UserRepository(Gson gson, Path usersFilePath) {
        this.gson = gson;
        this.usersFilePath = usersFilePath;
        this.users = new ArrayList<>();
        loadUsers();
    }

    /**
     * Loads all users from the JSON file.
     * 
     * If the file doesn't exist, starts with an empty user list.
     */
    private void loadUsers() {
        if (!Files.exists(usersFilePath)) {
            return;
        }

        try {
            String content = Files.readString(usersFilePath);
            JsonObject root = gson.fromJson(content, JsonObject.class);

            if (root != null && root.has("users")) {
                Type listType = new TypeToken<List<User>>() {
                }.getType();
                List<User> loaded = gson.fromJson(root.get("users"), listType);
                if (loaded != null) {
                    users.addAll(loaded);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    /**
     * Saves all users to the JSON file.
     */
    private void saveUsers() {
        try {
            JsonObject root = new JsonObject();
            root.add("users", gson.toJsonTree(users));
            Files.writeString(usersFilePath, gson.toJson(root));
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    /**
     * Finds all users in the system.
     * 
     * @return a copy of the users list to prevent external modification
     */
    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    /**
     * Finds a user by username.
     * 
     * @param username the username to search for
     * @return the User if found, null otherwise
     */
    public User findByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if a user exists with the given username.
     * 
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        return users.stream()
                .anyMatch(u -> u.getUsername().equals(username));
    }

    /**
     * Saves a new user to the repository.
     * 
     * @param user the user to save
     * @return true if saved successfully, false if user already exists
     */
    public boolean save(User user) {
        if (existsByUsername(user.getUsername())) {
            return false;
        }
        users.add(user);
        saveUsers();
        return true;
    }

    /**
     * Authenticates a user with username and password.
     * 
     * @param username the username
     * @param password the password
     * @return true if credentials are valid, false otherwise
     */
    public boolean authenticate(String username, String password) {
        return users.stream()
                .anyMatch(u -> u.getUsername().equals(username) &&
                        u.getPassword().equals(password));
    }
}
