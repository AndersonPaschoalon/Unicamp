package com.nexus.controller.commands;

import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;

import com.nexus.service.core.WorkflowService;

public class CreateStatusHandler implements CommandHandler {

    private final WorkflowService workflowService;

    public CreateStatusHandler(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        String[] p = command.getParameters();
        this.requireParamCount(p, 2, "CREATE_STATUS");
        this.workflowService.createStatus(this.extractString(p, 0, "statusName"), this.extractBoolean(p, 1, "isFinal"),
                workspace);
    }
}