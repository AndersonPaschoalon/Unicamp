package com.nexus.view.statescli.menus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.PermissionDeniedException;
import com.nexus.model.Command;
import com.nexus.model.valueobject.Role;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.view.PromptService;
import com.nexus.view.SessionContext;
import com.nexus.view.statescli.ViewState;

public class SprintManagementState implements ViewState {

    private final ViewState parent;
    private final String projectName;
    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryService queryService;
    private final Map<String, Function<PromptService, ViewState>> transitions = new HashMap<>();

    public SprintManagementState(ViewState parent, String projectName) {
        if (!SessionContext.getInstance().hasAtLeastRole(Role.MANAGER)) {
            throw new PermissionDeniedException("Apenas MANAGER pode gerenciar sprints.");
        }
        this.parent = parent;
        this.projectName = projectName;
        this.dispatcher = CommandDispatcher.getInstance();
        this.queryService = WorkspaceQueryServiceImpl.getInstance();

        transitions.put("criar", this::criarSprint);
        transitions.put("add-task", this::adicionarTaskNaSprint);
        transitions.put("voltar", prompt -> parent);
    }

    @Override
    public Collection<String> getAutocompleteCommands() {
        return transitions.keySet();
    }

    @Override
    public void renderHeader(PromptService prompt) {
        clearConsole(prompt);
        ViewState.super.renderHeader(prompt);
        prompt.printInfo("=== SPRINTS: " + projectName + " ===");
        prompt.printInfo("");
        prompt.printInfo("criar    - Criar nova sprint (nome, data início, data fim)");
        prompt.printInfo("add-task - Adicionar tarefa existente a uma sprint");
        prompt.printInfo("voltar   - Retornar ao menu anterior");
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String input = promptService.readString(getPromptText()).trim().toLowerCase();

        Function<PromptService, ViewState> transition = transitions.get(input);
        if (transition != null) {
            return transition.apply(promptService);
        }

        promptService.printWarning("Comando inválido. Tente novamente.");
        return this;
    }

    @Override
    public String getPromptText() {
        return "nexus/sprints> ";
    }

    private ViewState criarSprint(PromptService prompt) {
        String sprintName = prompt.readString("Nome da sprint: ").trim();
        String start = prompt.readString("Data início (AAAA-MM-DD): ").trim();
        String end = prompt.readString("Data fim (AAAA-MM-DD): ").trim();
        return dispatchAndReport(prompt, "CREATE_SPRINT",
                new String[] { projectName, sprintName, start, end }, "Sprint criada com sucesso!");
    }

    private ViewState adicionarTaskNaSprint(PromptService prompt) {
        String sprintName = prompt.readString("Nome da sprint: ").trim();
        String taskId = prompt.readString("ID da tarefa: ").trim();
        return dispatchAndReport(prompt, "ADD_TASK_TO_SPRINT",
                new String[] { projectName, sprintName, taskId }, "Tarefa adicionada à sprint com sucesso!");
    }

    private ViewState dispatchAndReport(PromptService prompt, String action, String[] params, String successMessage) {
        try {
            dispatcher.dispatchCommand(new Command(null, action, params));
            prompt.printInfo(successMessage);
        } catch (NexusDomainException e) {
            prompt.printWarning("Erro: " + e.getMessage());
        }
        return this;
    }
}