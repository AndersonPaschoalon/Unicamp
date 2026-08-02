package com.nexus.service.core.command;

import com.nexus.exception.CommandFormatException;
import com.nexus.model.Command;
import com.nexus.service.core.ProjectService;

public class AddTaskToSprintHandler implements CommandHandler {
    private final ProjectService projectService;

    public AddTaskToSprintHandler(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void handle(Command command) {
        String[] parameters = command.getParameters();
        if (parameters.length != 3) {
            throw new CommandFormatException(
                    "Esperado 3 parâmetros para ADD_TASK_TO_SPRINT, mas recebeu " + parameters.length);
        }
        projectService.addTaskToSprint(parameters[0], parameters[1], parameters[2]);
    }
}