package com.nexus.controller.commands;

import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;
import com.nexus.service.core.ProjectService;

public class CreateProjectHandler implements CommandHandler {
    private final ProjectService projectService;

    public CreateProjectHandler(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        String[] p = command.getParameters();
        this.requireParamCount(p, 2, "CREATE_PROJECT");
        this.projectService.createProject(this.extractString(p, 0, "name"), this.extractLong(p, 1, "totalBudget"),
                workspace);
    }
}
