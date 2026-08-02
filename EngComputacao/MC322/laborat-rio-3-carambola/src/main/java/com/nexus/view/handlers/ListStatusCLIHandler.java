package com.nexus.view.handlers;

import com.nexus.service.query.WorkspaceQueryServiceInterface;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class ListStatusCLIHandler
        implements InteractiveCLIHandler {

    private final WorkspaceQueryServiceInterface queryService;

    public ListStatusCLIHandler(WorkspaceQueryServiceInterface queryService) {

        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "LIST_STATUS";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        var statuses = this.queryService.listStatusNames();
        if (statuses.isEmpty()) {
            prompt.printWarning("Nenhum status encontrado.");
            return;
        }
        System.out.println("# Status");
        statuses.forEach(status -> System.out.println("- " + status));
    }
}