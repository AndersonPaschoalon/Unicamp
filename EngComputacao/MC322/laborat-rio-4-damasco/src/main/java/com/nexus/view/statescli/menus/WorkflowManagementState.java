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

public class WorkflowManagementState implements ViewState {

    private final ViewState parent;
    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryService queryService;
    private final Map<String, Function<PromptService, ViewState>> transitions = new HashMap<>();

    public WorkflowManagementState(ViewState parent) {
        if (!SessionContext.getInstance().hasAtLeastRole(Role.MANAGER)) {
            throw new PermissionDeniedException("Apenas MANAGER pode gerenciar workflows.");
        }
        this.parent = parent;
        this.dispatcher = CommandDispatcher.getInstance();
        this.queryService = WorkspaceQueryServiceImpl.getInstance();

        transitions.put("criar", this::criarWorkflow);
        transitions.put("vincular", this::vincularWorkflow);
        transitions.put("status-inicial", this::definirStatusInicial);
        transitions.put("transicao", this::adicionarTransicao);
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
        prompt.printInfo("=== GERENCIAR WORKFLOWS ===");
        prompt.printInfo("Workflows: " + queryService.listWorkflowNames());
        prompt.printInfo("Status disponíveis: " + queryService.listStatusNames());
        prompt.printInfo("");
        prompt.printInfo("criar          - Criar novo workflow");
        prompt.printInfo("vincular       - Vincular workflow a um projeto");
        prompt.printInfo("status-inicial - Definir status inicial de um workflow");
        prompt.printInfo("transicao      - Adicionar transição (de -> para)");
        prompt.printInfo("voltar         - Retornar ao menu anterior");
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
        return "nexus/workflows> ";
    }

    private ViewState criarWorkflow(PromptService prompt) {
        String name = prompt.readString("Nome do novo workflow: ").trim();
        return dispatchAndReport(prompt, "CREATE_WORKFLOW", new String[] { name }, "Workflow criado com sucesso!");
    }

    private ViewState vincularWorkflow(PromptService prompt) {
        String projectName = prompt.readString("Nome do projeto: ").trim();
        String workflowName = prompt.readString("Nome do workflow: ").trim();
        return dispatchAndReport(prompt, "LINK_WORKFLOW", new String[] { projectName, workflowName },
                "Workflow vinculado ao projeto com sucesso!");
    }

    private ViewState definirStatusInicial(PromptService prompt) {
        String workflowName = prompt.readString("Nome do workflow: ").trim();
        String statusName = prompt.readString("Nome do status inicial: ").trim();
        return dispatchAndReport(prompt, "SET_INITIAL_STATUS", new String[] { workflowName, statusName },
                "Status inicial definido com sucesso!");
    }

    private ViewState adicionarTransicao(PromptService prompt) {
        String workflowName = prompt.readString("Nome do workflow: ").trim();
        String fromStatus = prompt.readString("Status de origem: ").trim();
        String toStatus = prompt.readString("Status de destino: ").trim();
        return dispatchAndReport(prompt, "ALLOW_TRANSITION", new String[] { workflowName, fromStatus, toStatus },
                "Transição registrada com sucesso!");
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