package com.nexus.view.statescli.menus;

import java.util.Collection;

import com.nexus.model.valueobject.Role;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.view.PromptService;
import com.nexus.view.SessionContext;
import com.nexus.view.statescli.ViewState;

public class LoginState implements ViewState {

    private final WorkspaceQueryService queryService;

    public LoginState() {
        this.queryService = WorkspaceQueryServiceImpl.getInstance();
    }

    @Override
    public Collection<String> getAutocompleteCommands() {
        return queryService.listUserNames();
    }

    @Override
    public void renderHeader(PromptService prompt) {
        clearConsole(prompt);
        prompt.printInfo("========================================");
        prompt.printInfo("============ NEXUS — LOGIN =============");
        prompt.printInfo("========================================");
    }

    @Override
    public String getPromptText() {
        return "Username: ";
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String username = promptService.readString(getPromptText()).trim();

        if (!queryService.listUserNames().contains(username)) {
            promptService.printWarning("Usuário '" + username + "' não encontrado.");
            return this;
        }

        Role role = Role.valueOf(queryService.getUserRoleName(username));
        SessionContext.build(username, role);

        promptService.printInfo("Login realizado com sucesso. Bem-vindo, " + username + "!");
        return new MainMenuState();
    }
}