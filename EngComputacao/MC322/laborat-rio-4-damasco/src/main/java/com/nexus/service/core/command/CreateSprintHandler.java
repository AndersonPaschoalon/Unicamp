package com.nexus.service.core.command;

import com.nexus.exception.CommandFormatException;
import com.nexus.model.Command;
import com.nexus.service.core.ProjectService;

public class CreateSprintHandler implements CommandHandler {
    private final ProjectService projectService;

    public CreateSprintHandler(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void handle(Command command) {
        String[] parameters = command.getParameters();
        if (parameters.length != 4) {
            throw new CommandFormatException(
                    "Esperado 4 parâmetros para CREATE_SPRINT, mas recebeu " + parameters.length);
        }
        projectService.createSprint(parameters[0], parameters[1],
                extractDate(parameters, 2, "Data Início"), extractDate(parameters, 3, "Data Fim"));
    }
}