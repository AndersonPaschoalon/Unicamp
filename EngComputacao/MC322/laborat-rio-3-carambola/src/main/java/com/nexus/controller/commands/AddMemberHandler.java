package com.nexus.controller.commands;

import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;

import com.nexus.service.core.ProjectService;

public class AddMemberHandler implements CommandHandler {

    private final ProjectService projectService;

    public AddMemberHandler(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        String[] p = command.getParameters();
        requireParamCount(p, 3, "ADD_MEMBER");
        this.projectService.addTeamMemberToProject(this.extractString(p, 0, "projectId"),
                this.extractString(p, 1, "username"),
                this.extractString(p, 2, "role"), workspace);
    }
}
