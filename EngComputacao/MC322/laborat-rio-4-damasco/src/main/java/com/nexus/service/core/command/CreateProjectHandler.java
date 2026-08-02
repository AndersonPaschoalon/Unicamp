package com.nexus.service.core.command;

import com.nexus.exception.CommandFormatException;
import com.nexus.model.Command;
import com.nexus.service.core.ProjectService;

public class CreateProjectHandler implements CommandHandler {
    private final ProjectService projectService;

    public CreateProjectHandler(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void handle(Command command) {
        String[] parameters = command.getParameters();
        if (parameters.length != 2) {
            throw new CommandFormatException(
                "Esperado 2 parâmetros para CREATE_PROJECT, mas recebeu " + parameters.length);
        }
        projectService.createProject(parameters[0], extractLong(parameters, 1, "Total Budget"));
    }
}
