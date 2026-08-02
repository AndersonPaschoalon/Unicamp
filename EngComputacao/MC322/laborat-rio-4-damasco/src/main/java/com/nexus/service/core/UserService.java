package com.nexus.service.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nexus.exception.EntityNotFoundException;
import com.nexus.model.entity.User;
import com.nexus.model.valueobject.Role;
import com.nexus.service.Workspace;

/**
 * Service class for managing users and exposing user-related domain operations.
 */
public class UserService {

    private final Workspace workspace;

    public UserService(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * Creates and registers a new user in the system.
     *
     * @param username the username to register
     * @param email    the email to associate with the user
     */

    public void createUser(String username, String email) {
        this.workspace.saveUser(new User(username, email));
    }

    public void createUser(String username, String email, Role role) {
        workspace.saveUser(new User(username, email, role));
    }

    /**
     * Returns an unmodifiable view of the users to preserve encapsulation.
     *
     * @return an unmodifiable list containing all registered users
     */
    public List<User> getUsers() {
        return this.workspace.findAllUsers();
    }

    /**
     * Retrieves a user by username, throwing a validation exception if not found.
     *
     * @param username the username to search for
     * @return the matching user
     * @throws EntityNotFoundException if no user with the provided username exists
     */
    public User getUserByUsername(String username) {
        return this.workspace.getUser(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + username));
    }

}