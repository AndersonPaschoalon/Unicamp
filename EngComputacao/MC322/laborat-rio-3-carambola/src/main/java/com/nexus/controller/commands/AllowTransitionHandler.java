package com.nexus.controller.commands;

import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;

import com.nexus.service.core.WorkflowService;

public class AllowTransitionHandler implements CommandHandler {
    private final WorkflowService workflowService;

    public AllowTransitionHandler(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        String[] p = command.getParameters();
        requireParamCount(p, 3, "ALLOW_TRANSITION");
        this.workflowService.allowTransition(this.extractString(p, 0, "workflowName"),
                this.extractString(p, 1, "fromStatus"),
                this.extractString(p, 2, "toStatus"), workspace);
    }
}
