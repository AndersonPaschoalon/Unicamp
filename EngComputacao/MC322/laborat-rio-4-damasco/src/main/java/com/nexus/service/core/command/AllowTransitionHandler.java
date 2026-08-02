package com.nexus.service.core.command;

import com.nexus.exception.CommandFormatException;
import com.nexus.model.Command;
import com.nexus.service.core.WorkflowService;

public class AllowTransitionHandler implements CommandHandler {
    private final WorkflowService workflowService;

    public AllowTransitionHandler(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public void handle(Command command) {
        String[] parameters = command.getParameters();
        if (parameters.length != 3) {
            throw new CommandFormatException(
                    "Esperado 3 parâmetros para ALLOW_TRANSITION, mas recebeu " + parameters.length);
        }
        workflowService.allowTransition(parameters[0], parameters[1], parameters[2]);
    }
}
