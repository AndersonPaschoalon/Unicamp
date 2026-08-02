package com.nexus.service.core.command;

import com.nexus.exception.CommandFormatException;
import com.nexus.model.Command;
import com.nexus.service.core.WorkflowService;

public class CreateWorkflowHandler implements CommandHandler {
    private final WorkflowService workflowService;

    public CreateWorkflowHandler(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public void handle(Command command) {
        String[] parameters = command.getParameters();
        if (parameters.length != 1) {
            throw new CommandFormatException(
                "Esperado 1 parâmetro para CREATE_WORKFLOW, mas recebeu " + parameters.length);
        }
        String workflowName = parameters[0];
        workflowService.createWorkflow(workflowName);
    }
}
