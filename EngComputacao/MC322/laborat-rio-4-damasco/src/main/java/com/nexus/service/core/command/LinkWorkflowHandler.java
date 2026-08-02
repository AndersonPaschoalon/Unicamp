package com.nexus.service.core.command;

import com.nexus.exception.CommandFormatException;
import com.nexus.model.Command;
import com.nexus.service.core.ProjectService;

public class LinkWorkflowHandler implements CommandHandler {
    private final ProjectService projectService;

    public LinkWorkflowHandler(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void handle(Command command) {
        String[] parameters = command.getParameters();
        if (parameters.length != 2) {
            throw new CommandFormatException(
                    "Esperado 2 parâmetros para VINCULAR_WORKFLOW, mas recebeu " + parameters.length);
        }
        projectService.linkWorkflow(parameters[0], parameters[1]);
    }
    
}
