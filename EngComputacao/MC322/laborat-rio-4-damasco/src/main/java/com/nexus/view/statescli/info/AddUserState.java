package com.nexus.view.statescli.info;

import java.util.Collection;
import java.util.Collections;

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

public class AddUserState implements ViewState {

    private final ViewState parent;
    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryService queryService;

    public AddUserState(ViewState parent) {
        if (!SessionContext.getInstance().hasAtLeastRole(Role.MANAGER)) {
            throw new PermissionDeniedException("Apenas MANAGER pode criar novos usuários.");
        }
        this.parent = parent;
        this.dispatcher = CommandDispatcher.getInstance();
        this.queryService = WorkspaceQueryServiceImpl.getInstance();
    }

    @Override
    public Collection<String> getAutocompleteCommands() {
        return Collections.emptyList();
    }

    @Override
    public void renderHeader(PromptService prompt) {
        clearConsole(prompt);
        ViewState.super.renderHeader(prompt);
        prompt.printInfo("=== CRIAR NOVO USUÁRIO ===");
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String username = promptService.readString("Username: ").trim();
        String email = promptService.readString("Email: ").trim();

        String confirma = promptService.readString("Confirma criação do usuário? (s/n/voltar): ").trim().toLowerCase();

        if (confirma.equals("voltar")) {
            return parent;
        } else if (confirma.equals("n")) {
            promptService.printInfo("Criação de usuário cancelada.");
            return parent;
        } else if (confirma.equals("s")) {
            try {
                Command command = new Command(null, "CREATE_USER", new String[] { username, email });
                dispatcher.dispatchCommand(command);
                promptService.printInfo("Usuário criado com sucesso!");
                return parent;
            } catch (NexusDomainException e) {
                promptService.printWarning("Erro ao criar usuário: " + e.getMessage());
                return this;
            }
        } else {
            promptService.printWarning("Entrada inválida. Por favor, responda com 's', 'n' ou 'voltar'.");
            return this;
        }
    }

    @Override
    public String getPromptText() {
        return "Username: ";
    }
}