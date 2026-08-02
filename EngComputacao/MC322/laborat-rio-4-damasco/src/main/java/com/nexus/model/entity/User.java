package com.nexus.model.entity;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.nexus.exception.NexusDomainException;
import com.nexus.model.Entity;
import com.nexus.model.valueobject.Role;

public final class User implements Entity {

    private final String username;
    private final String email;
    private final String id;
    private final Role role;

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern
            .compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public User(String username, String email) {
        this(username, email, Role.CONTRIBUTOR);
    }

    public User(String username, String email, Role role) {
        validateUsername(username);
        validateEmail(email);

        this.id = username;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public User withRole(Role newRole) {
        return new User(this.id, this.username, this.email, newRole, true);
    }

    @Override
    public String getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public String consultEmail() {
        return email;
    }

    public String consultUsername() {
        return username;
    }

    public long calculateWorkload(List<Task> tasks) {
        return tasks.stream()
                .filter(t -> t.getOwnerId() != null
                        && t.getOwnerId().equals(this.id)
                        && t.getStatusId() != null
                        && t.getStatusId().equals("IN_PROGRESS"))
                .count();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return Objects.equals(username, user.username) ||
                Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email);
    }

    private User(String id, String username, String email, Role role, boolean trusted) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NexusDomainException("Username não pode ser vazio.");
        }
        if (username.trim().isEmpty()) {
            throw new NexusDomainException("Username não pode ser apenas espaços em branco.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new NexusDomainException("Email não pode ser vazio.");
        }
        if (!VALID_EMAIL_ADDRESS_REGEX.matcher(email).matches()) {
            throw new NexusDomainException("Email inválido.");
        }
    }

}