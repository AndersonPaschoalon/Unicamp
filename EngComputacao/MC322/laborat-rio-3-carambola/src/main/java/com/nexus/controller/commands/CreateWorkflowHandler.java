package com.nexus.controller.commands;

import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;

import com.nexus.service.core.WorkflowService;

public class CreateWorkflowHandler implements CommandHandler {

    private final WorkflowService workflowService;

    public CreateWorkflowHandler(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        String[] p = command.getParameters();
        this.requireParamCount(p, 1, "CREATE_WORKFLOW");
        this.workflowService.createWorkflow(this.extractString(p, 0, "workflowName"), workspace);
    }
}
