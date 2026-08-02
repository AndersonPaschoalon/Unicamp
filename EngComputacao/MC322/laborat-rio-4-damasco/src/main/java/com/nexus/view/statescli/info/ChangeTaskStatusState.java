package com.nexus.view.statescli.info;

import java.util.Collection;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.model.Command;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.view.PromptService;
import com.nexus.view.SessionContext;
import com.nexus.view.statescli.ViewState;

public class ChangeTaskStatusState implements ViewState {

    private final ViewState parent;
    private final CommandDispatcher dispatcher;
    private final String taskId;
    private final WorkspaceQueryService queryService;

    public ChangeTaskStatusState(ViewState parent, String taskId) {
        this.parent = parent;
        this.taskId = taskId;
        this.dispatcher = CommandDispatcher.getInstance();
        this.queryService = WorkspaceQueryServiceImpl.getInstance();
    }

    @Override
    public Collection<String> getAutocompleteCommands() {
        Collection<String> commands = new java.util.ArrayList<>(queryService.listAllowedTransitions(taskId));
        commands.add("voltar");
        return commands;
    }

    @Override
    public void renderHeader(PromptService prompt) {
        prompt.printInfo("=== ALTERAR STATUS DA TAREFA ===");
        prompt.printInfo("Status atual: " + queryService.getTaskStatusName(taskId));
        prompt.printInfo("Transições permitidas: " + queryService.listAllowedTransitions(taskId));
        prompt.printInfo("");
    }

    @Override
    public String getPromptText() {
        return "> Novo status da tarefa: ";
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String newStatus = promptService.readString(getPromptText()).trim();

        if (newStatus.equalsIgnoreCase("voltar")) {
            return parent;
        }

        String confirma = promptService.readString("Confirma alteração de status? (s/n/voltar): ").trim().toLowerCase();

        if (confirma.equals("voltar") || confirma.equals("n")) {
            promptService.printInfo("Alteração de status cancelada.");
            return parent;
        } else if (confirma.equals("s")) {
            try {
                String username = SessionContext.getInstance().getUsername();
                Command command = new Command(null, "CHANGE_STATUS", new String[] { taskId, newStatus, username });
                dispatcher.dispatchCommand(command);

                promptService.printInfo("Status da tarefa alterado com sucesso!");
                return parent;
            } catch (NexusDomainException e) {
                promptService.printWarning("Erro ao alterar status da tarefa: " + e.getMessage());
                return this;
            }
        } else {
            promptService.printWarning("Entrada inválida. Por favor, responda com 's', 'n' ou 'voltar'.");
            return this;
        }
    }
}