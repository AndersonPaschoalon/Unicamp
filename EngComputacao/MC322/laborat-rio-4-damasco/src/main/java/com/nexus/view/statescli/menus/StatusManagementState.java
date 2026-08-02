package com.nexus.view.statescli.menus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.view.PromptService;
import com.nexus.view.statescli.ViewState;
import com.nexus.view.statescli.info.AddStatusState;

public class StatusManagementState implements ViewState {

    private final ViewState parent;
    private final WorkspaceQueryService queryService;

    private final Map<String, Supplier<ViewState>> transitions = new HashMap<>();

    public StatusManagementState(ViewState parent) {
        this.parent = parent;
        this.queryService = WorkspaceQueryServiceImpl.getInstance();
        registerTransition("add", () -> new AddStatusState(this), transitions);
        registerTransition("voltar", () -> parent, transitions);
    }

    @Override
    public Collection<String> getAutocompleteCommands() {
        return transitions.keySet();
    }

    @Override
    public void renderHeader(PromptService prompt) {
        clearConsole(prompt);
        ViewState.super.renderHeader(prompt);
        prompt.printInfo("Status atuais do sistema:");
        // Aqui você pode listar os status atuais usando queryService, se necessário
        for (String status : queryService.listStatusNames()) {
            prompt.printInfo(" - " + status);
        }
        
        
        prompt.printInfo("Comandos disponíveis:");
        prompt.printInfo("  add    - Criar e vincular um novo status a um workflow");
        prompt.printInfo("  voltar - Retornar ao menu anterior");
        printFooter(prompt);
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String input = promptService.readString(getPromptText());
        String trimmed = input.trim().toLowerCase();

        Supplier<ViewState> transition = transitions.get(trimmed);
        if (transition != null) {
            return transition.get();
        }

        return this;
    }

    @Override
    public String getPromptText() {
        return "nexus/status> ";
    }
}