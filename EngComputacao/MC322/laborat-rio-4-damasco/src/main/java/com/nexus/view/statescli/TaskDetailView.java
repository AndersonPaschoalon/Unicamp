package com.nexus.view.statescli;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import com.nexus.service.WorkspaceQueryService;
import com.nexus.view.PromptService;
import com.nexus.view.statescli.info.AddMemberState;
import com.nexus.view.statescli.info.ChangeTaskStatusState;
import com.nexus.view.statescli.menus.ProjectMenuState;


/**
 * Deep-inspection state that displays all available details of a single task.
 *
 * <p>The only accepted input is {@code VOLTAR}, which returns to the
 * {@link ProjectMenuState}.</p>
 */
public class TaskDetailView implements ViewState {

    private final String taskId;
    private final WorkspaceQueryService queryService;
    private final ViewState parent;
    private final Map<String, Supplier<ViewState>> transitions;

    /**
     * Constructs the detail view for a specific task.
     *
     * @param queryService the read-only query service
     * @param parent       the parent view to return to
     * @param taskId       the ID of the task to inspect
     */
    public TaskDetailView(WorkspaceQueryService queryService, ViewState parent, String taskId) {
        this.queryService = queryService;
        this.parent = parent;
        this.taskId = taskId;
        this.transitions = new java.util.HashMap<>();

        registerTransition("voltar", () -> parent, transitions);
        registerTransition("add-member", () -> new AddMemberState(parent, taskId), transitions);
        registerTransition("move-task", () -> new ChangeTaskStatusState(parent, taskId), transitions);
    }

    @Override
    public Collection<String> getAutocompleteCommands() {
        return transitions.keySet();
    }

    @Override
    public void renderHeader(PromptService prompt) {
        ViewState.super.renderHeader(prompt);
        prompt.printInfo("NEXUS INSPEÇÃO: " + taskId);
        prompt.printInfo("");

        prompt.printInfo("Título: " + queryService.getTaskTitle(taskId));
        prompt.printInfo("Projeto: " + queryService.getTaskProjectName(taskId));
        prompt.printInfo("Status Atual: " + queryService.getTaskStatusName(taskId));

        String ownerName = queryService.getTaskOwnerName(taskId);
        String ownerRole = queryService.getTaskOwnerRole(taskId);
        if ("Não atribuído".equals(ownerName)) {
            prompt.printInfo("Responsável: " + ownerName);
        } else {
            prompt.printInfo("Responsável: " + ownerName + " (Role: " + ownerRole + ")");
        }

        prompt.printInfo("Prazo: " + queryService.getTaskDeadline(taskId));

        prompt.printInfo("");
        printFooter(prompt);
    }

    @Override
    public String getPromptText() {
        return "> Digite VOLTAR para retornar ao Kanban: ";
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String input = promptService.readString(getPromptText()).trim();
        Supplier<ViewState> transition = transitions.get(input.toLowerCase());
        if (transition != null) {
            return transition.get();
        } else {
            promptService.printWarning("Comando inválido.");
            return this;
        }
    }
}
