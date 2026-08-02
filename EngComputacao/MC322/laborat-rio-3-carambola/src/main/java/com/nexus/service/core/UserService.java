package com.nexus.service.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nexus.exception.NexusSystemException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.entity.User;

/**
 * Service class for managing users, encapsulating the user collection
 * and providing domain-specific operations.
 */
public class UserService {
    // Encapsulamento da lista: a CLI ou Main não precisam mais gerir este estado.
    private final List<User> users = new ArrayList<>();

    public UserService() {
    }

    /**
     * Creates and registers a new user in the system.
     */
    public void createUser(String username, String email) {
        users.add(new User(username, email));
    }

    /**
     * Returns an unmodifiable view of the users to preserve encapsulation.
     */
    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    /**
     * Retrieves a user by username, throwing a validation exception if not found.
     */
    public User getUserByUsername(String username) {
        return users.stream()
                .filter(u -> u.consultUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new NexusSystemException("Usuário não encontrado: " + username));
    }
}