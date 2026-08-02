package com.nexus.view.handlers;

import java.util.List;
import java.util.Map;

import com.nexus.service.query.WorkspaceQueryServiceInterface;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class ListUsersCLIHandler
        implements InteractiveCLIHandler {

    private final WorkspaceQueryServiceInterface queryService;

    public ListUsersCLIHandler(WorkspaceQueryServiceInterface queryService) {
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "LIST_USERS";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        List<String> users = this.queryService.listUserNames();
        if (users.isEmpty()) {
            prompt.printWarning("Nenhum usuário encontrado.");
            return;
        }
        users.forEach(user -> {
            System.out.println("- " + user);
        });
    }
}