package com.nexus.controller.commands;

import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;
import com.nexus.service.core.UserService;

public class CreateUserHandler implements CommandHandler {

    private final UserService userService;

    public CreateUserHandler(UserService userService) {
        if (userService == null) {
            throw new IllegalArgumentException("UserService cannot be null");
        }
        this.userService = userService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        String[] p = command.getParameters();
        this.requireParamCount(p, 2, "CREATE_USER");
        this.userService.createUser(this.extractString(p, 0, "username"), this.extractString(p, 1, "email"));
    }

}
