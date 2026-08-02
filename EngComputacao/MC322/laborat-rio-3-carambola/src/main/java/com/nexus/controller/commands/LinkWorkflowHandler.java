package com.nexus.controller.commands;

import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;

import com.nexus.service.core.ProjectService;

public class LinkWorkflowHandler implements CommandHandler {
    private final ProjectService projectService;

    public LinkWorkflowHandler(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        String[] p = command.getParameters();
        this.requireParamCount(p, 2, "LINK_WORKFLOW");
        this.projectService.linkWorkflow(this.extractString(p, 0, "projectId"),
                this.extractString(p, 1, "workflowName"), workspace);
    }
}
