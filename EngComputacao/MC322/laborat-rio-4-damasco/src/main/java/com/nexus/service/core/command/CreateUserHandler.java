package com.nexus.service.core.command;

import com.nexus.exception.CommandFormatException;
import com.nexus.model.Command;
import com.nexus.service.core.UserService;

public class CreateUserHandler implements CommandHandler{
    private final UserService userService;

    public CreateUserHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(Command command) {
        String[] parameters = command.getParameters();
        if (parameters.length != 2) {
            throw new CommandFormatException(
                "Esperado 2 parâmetros para CREATE_USER, mas recebeu " + parameters.length);
            }
        userService.createUser(parameters[0], parameters[1]);
    }
}
