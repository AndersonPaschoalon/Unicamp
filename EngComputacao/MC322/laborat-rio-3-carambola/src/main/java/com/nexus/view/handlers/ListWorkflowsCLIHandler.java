package com.nexus.view.handlers;

import com.nexus.service.query.WorkspaceQueryServiceInterface;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class ListWorkflowsCLIHandler
        implements InteractiveCLIHandler {

    private final WorkspaceQueryServiceInterface queryService;

    public ListWorkflowsCLIHandler(WorkspaceQueryServiceInterface queryService) {
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "LIST_WORKFLOWS";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        var workflows = queryService.listWorkflowNames();
        if (workflows.isEmpty()) {
            prompt.printWarning("Nenhum workflow encontrado.");
            return;
        }
        System.out.println("# Workflows");
        workflows.forEach(workflow -> System.out.println("- " + workflow));
    }
}