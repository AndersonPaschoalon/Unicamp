package com.nexus.view.statescli.menus;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.nexus.service.WorkspaceQueryService;
import com.nexus.view.PromptService;
import com.nexus.view.statescli.TaskDetailView;
import com.nexus.view.statescli.ViewState;
import com.nexus.view.statescli.info.AddTaskState;
import com.nexus.view.statescli.menus.SprintManagementState;

public class KanbanMenuState implements ViewState {

    private final String projectName;
    private final Map<String, Supplier<ViewState>> transitions = new HashMap<>();
    private final WorkspaceQueryService queryService;
    private final ViewState parent;

    public KanbanMenuState(WorkspaceQueryService queryService, ViewState parent, String projectName) {
        this.projectName = projectName;
        this.queryService = queryService;
        this.parent = parent;

        registerTransition("voltar", () -> parent);
        registerTransition("add task", () -> new AddTaskState(this, projectName));
        registerTransition("sprints",
                () -> new SprintManagementState(this, projectName));

        for (String statusName : queryService.listStatusNamesByProject(projectName)) {
            for (String taskId : queryService.listTaskIdsByProjectAndStatus(projectName, statusName)) {
                registerTransition(taskId, () -> new TaskDetailView(queryService, this, taskId));
            }
        }
    }

    private void registerTransition(String command, Supplier<ViewState> target) {
        transitions.put(command, target);
    }

    @Override
    public Collection<String> getAutocompleteCommands() {
        return transitions.keySet();
    }

    @Override
    public void renderHeader(PromptService prompt) {
        prompt.printInfo("=== KANBAN BOARD: " + projectName + " ===");
        prompt.printInfo("");

        List<String> statuses = queryService.listStatusNamesByProject(projectName);
        if (statuses.isEmpty()) {
            prompt.printWarning("Projeto sem workflow vinculado ou sem status definidos.");
        }

        for (String statusName : statuses) {
            prompt.printInfo("[ " + statusName + " ]");
            List<String> taskIds = queryService.listTaskIdsByProjectAndStatus(projectName, statusName);

            if (taskIds.isEmpty()) {
                prompt.printInfo("  (vazio)");
            } else {
                for (String taskId : taskIds) {
                    registerTransition(taskId, () -> new TaskDetailView(queryService, this, taskId));

                    String title = queryService.getTaskTitle(taskId);
                    String owner = queryService.getTaskOwnerName(taskId);
                    String line = "- " + taskId + ": " + title;
                    if (!"Não atribuído".equals(owner)) {
                        line += " (Resp: " + owner + ")";
                    }
                    if (queryService.isTaskOverdue(taskId)) {
                        line += " [ATRASADA]";
                    }
                    prompt.printInfo(line);
                }
            }
            prompt.printInfo("");
        }
    }

    @Override
    public String getPromptText() {
        return "> Digite o ID de uma tarefa, 'add task' para criar ou 'voltar': ";
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String input = promptService.readString(getPromptText()).trim();

        if (transitions.containsKey(input)) {
            return transitions.get(input).get();
        }

        promptService.printWarning("Comando ou ID de tarefa inválido. Tente novamente.");
        return this;
    }
}