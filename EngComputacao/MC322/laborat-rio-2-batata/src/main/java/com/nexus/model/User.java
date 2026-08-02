/**
 * Represents a user in the Nexus system.
 * 
 * This class encapsulates user information including username and email address. It provides
 * validation for both fields during instantiation and methods to retrieve user data and calculate
 * workload based on assigned tasks.
 * 
 * The class enforces the following constraints: - Username cannot be null, empty, or contain only
 * whitespace - Email must be a valid email format according to RFC standards
 * 
 * @author Sanchez
 * @version 1.0
 */

package com.nexus.model;

import java.util.List;
import java.util.regex.Pattern;
import com.nexus.exception.NexusValidationException;

public class User {
    private final String username;
    private final String email;

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    /**
     * Construtor.
     * 
     * @param username
     * @param email
     */
    public User(String username, String email) {
        validateUsername(username);
        validateEmail(email);

        this.username = username;
        this.email = email;
    }

    /**
     * Validates the provided username string.
     * 
     * @param username the username to validate
     * @throws IllegalArgumentException if the username is null or blank
     * @throws NexusValidationException if the username contains only whitespace characters
     */
    private void validateUsername(String username)
            throws IllegalArgumentException, NexusValidationException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }
        if (username.trim().length() == 0) {
            throw new NexusValidationException("Username não pode ser apenas espaços em branco.");
        }
    }

    /**
     * Validates the provided email address.
     *
     * @param email the email address to validate
     * @throws IllegalArgumentException if the email is null or blank
     * @throws NexusValidationException if the email format is invalid according to the email regex
     *         pattern
     */
    private void validateEmail(String email) throws NexusValidationException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email não pode ser vazio.");
        }
        if (!VALID_EMAIL_ADDRESS_REGEX.matcher(email).matches()) {
            throw new NexusValidationException("Email inválido.");
        }
    }

    /**
     * Retrieves the email address associated with this user.
     *
     * @return the email address of the user
     */
    public String consultEmail() {
        return email;
    }

    /**
     * Retrieves the username of the user.
     *
     * @return the username associated with this user
     */
    public String consultUsername() {
        return username;
    }

    /**
     * Calculates the workload for this user by counting the number of in-progress tasks assigned to
     * them.
     *
     * @param tasks the list of tasks to filter and count
     * @return the number of tasks that are assigned to this user and have a status of IN_PROGRESS
     */
    public long calculateWorkload(List<Task> tasks) {
        return tasks.stream().filter(t -> t.getOwner() != null && t.getOwner().equals(this)
                && !t.getStatus().isTerminal()).count();
    }

    @Override
    public String toString() {
        return "User(username='" + this.username + "', email='" + this.email + "')";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        User u = (User) o;
        if (!this.username.equals(u.username)) {
            return false;
        }
        if (!this.email.equals(u.email)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        int prime = 31;

        int usernameHash = this.username.hashCode();
        result = prime * result + usernameHash;

        int emailHash = this.email.hashCode();
        result = prime * result + emailHash;

        return result;
    }

    /**
     * Os cargos possiveis que um usuario pode ter
     * 
     */
    public static enum Role {
        VIEWER, CONTRIBUTOR, MANAGER;

        /**
         * Converte uma string roleStr em uma enum User.Role.
         * 
         * @param roleStr
         * @return enum User.Role
         */
        public static User.Role parseRole(String roleStr) {
            if (roleStr == null || roleStr.isBlank()) {
                throw new IllegalArgumentException("Cargo não pode ser nulo ou vazio.");
            }
            try {
                return Role.valueOf(roleStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Cargo inválido: " + roleStr
                        + ". Valores aceitos: VIEWER, CONTRIBUTOR, MANAGER");
            }
        }
    }
}
