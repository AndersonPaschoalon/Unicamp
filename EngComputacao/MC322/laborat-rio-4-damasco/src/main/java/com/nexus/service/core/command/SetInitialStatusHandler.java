package com.nexus.service.core.command;

import com.nexus.exception.CommandFormatException;
import com.nexus.model.Command;
import com.nexus.service.core.WorkflowService;

public class SetInitialStatusHandler implements CommandHandler {
    private final WorkflowService workflowService;

    public SetInitialStatusHandler(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public void handle(Command command) {
        String[] parameters = command.getParameters();
        if (parameters.length != 2) {
            throw new CommandFormatException(
                "Esperado 2 parâmetros para SET_INITIAL_STATUS, mas recebeu " + parameters.length);
        }
        workflowService.setInitialStatus(parameters[0], parameters[1]);
    }
    
}
