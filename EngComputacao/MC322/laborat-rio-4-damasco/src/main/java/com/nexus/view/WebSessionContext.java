package com.nexus.view;

import com.nexus.model.valueobject.Role;
import com.vaadin.flow.server.VaadinSession;


public final class WebSessionContext {
    private static final String ATTR = WebSessionContext.class.getName();

    private final String username;
    private final Role role;

    public WebSessionContext(String username, Role role) {
        this.username = username;
        this.role = role;
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

    public static WebSessionContext current() {
        return (WebSessionContext) VaadinSession.getCurrent().getAttribute(ATTR);
    }

    public static void set(WebSessionContext ctx) {
        VaadinSession.getCurrent().setAttribute(ATTR, ctx);
    }

    public static void clear() {
        VaadinSession.getCurrent().setAttribute(ATTR, null);
    }
}
