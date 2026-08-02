package com.nexus.controller.commands;

import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;

import com.nexus.service.core.WorkflowService;

public class SetInitialStatusHandler implements CommandHandler {

    private final WorkflowService workflowService;

    public SetInitialStatusHandler(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        String[] p = command.getParameters();
        this.requireParamCount(p, 2, "SET_INITIAL_STATUS");
        this.workflowService.setInitialStatus(this.extractString(p, 0, "workflowName"),
                this.extractString(p, 1, "statusName"),
                workspace);
    }
}
