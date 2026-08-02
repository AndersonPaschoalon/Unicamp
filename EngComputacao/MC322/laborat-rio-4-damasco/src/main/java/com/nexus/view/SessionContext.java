package com.nexus.view;

import com.nexus.model.valueobject.Role;

public final class SessionContext {
    private static SessionContext instance;

    private final String username;
    private final Role role;

    private SessionContext(String username, Role role) {
        this.username = username;
        this.role = role;
    }

    // Permite login/logout/re-login na mesma execução: sobrescreve a instância
    // anterior.
    public static SessionContext build(String username, Role role) {
        instance = new SessionContext(username, role);
        return instance;
    }

    public static SessionContext getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SessionContext not initialized. Call build() first.");
        }
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public boolean hasAtLeastRole(Role minimum) {
        return role.ordinal() >= minimum.ordinal();
    }
}