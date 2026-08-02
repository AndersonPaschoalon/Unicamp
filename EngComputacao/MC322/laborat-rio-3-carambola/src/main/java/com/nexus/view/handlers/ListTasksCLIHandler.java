package com.nexus.view.handlers;

import java.util.List;
import java.util.Map;

import com.nexus.service.query.WorkspaceQueryServiceInterface;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class ListTasksCLIHandler
        implements InteractiveCLIHandler {

    private final WorkspaceQueryServiceInterface queryService;

    public ListTasksCLIHandler(WorkspaceQueryServiceInterface queryService) {
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "LIST_TASKS";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        Map<String, List<String>> tasks = queryService.listTasksGroupedByProject();

        if (tasks.isEmpty()) {
            prompt.printWarning("Nenhuma tarefa encontrada.");
            return;
        }

        tasks.forEach((project, taskList) -> {
            System.out.println("# " + project);
            if (taskList.isEmpty()) {
                System.out.println("- Nenhuma tarefa");
            } else {
                taskList.forEach(task -> System.out.println("- " + task));
            }
            System.out.println();
        });
    }
}
