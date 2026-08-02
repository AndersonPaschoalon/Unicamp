package com.nexus.view.statescli.menus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.nexus.model.entity.User;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.view.PromptService;
import com.nexus.view.statescli.ViewState;
import com.nexus.view.statescli.info.AddUserState;

public class UserMenuState implements ViewState {

    private final ViewState parent;
    private final WorkspaceQueryService queryService;
    
    // Mapa para comandos fixos (polimórfico)
    private final Map<String, Supplier<ViewState>> transitions = new HashMap<>();

    public UserMenuState(ViewState parent) {
        this.parent = parent;
        this.queryService = WorkspaceQueryServiceImpl.getInstance();

        // 1. Registro de Comandos Fixos
        registerTransition("add-user", () -> new AddUserState(this));
        registerTransition("voltar", () -> parent);
    }

    private void registerTransition(String command, Supplier<ViewState> stateSupplier) {
        transitions.put(command, stateSupplier);
    }

    @Override
    public Collection<String> getAutocompleteCommands() {
        return transitions.keySet();
    }

    @Override
    public void renderHeader(PromptService promptService) {
        clearConsole(promptService);
        ViewState.super.renderHeader(promptService);
        
        // Recupera a coleção de usuários do serviço de consulta
        // Nota: Valide se o método na sua interface chama-se listUserNames() ou similar
        Collection<String> userNames = queryService.listUserNames();
        
        if (userNames == null || userNames.isEmpty()) {
            promptService.printWarning("Nenhum usuário encontrado.");
        } else {
            userNames.forEach(name -> promptService.printInfo(name));
        }
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String trimmed = promptService.readString(getPromptText()).trim();
        
        Supplier<ViewState> transition = transitions.get(trimmed);
        
        if (transition != null) {
            return transition.get();
        }

        return this; // Se não encontrar o comando, mantém o estado
    }

    @Override
    public String getPromptText() {
        return "Digite 'add-user' ou 'voltar': ";
    }
}