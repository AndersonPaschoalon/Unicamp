package com.nexus.view.handlers;

import com.nexus.service.query.WorkspaceQueryServiceInterface;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class ListProjectsCLIHandler
        implements InteractiveCLIHandler {

    private final WorkspaceQueryServiceInterface queryService;

    public ListProjectsCLIHandler(WorkspaceQueryServiceInterface queryService) {
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "LIST_PROJECTS";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        var projects = this.queryService.listProjectNames();

        if (projects.isEmpty()) {
            prompt.printWarning("Nenhum projeto encontrado.");
            return;
        }

        System.out.println("# Projetos");
        projects.forEach(project -> System.out.println("- " + project));
    }
}