package com.nexus.view.statescli.menus;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.view.PromptService;
import com.nexus.view.statescli.ViewState;
import com.nexus.view.statescli.info.AddProjectState;

public class ProjectMenuState implements ViewState {

    private final ViewState parent;
    private final WorkspaceQueryService queryService;
    
    // Mapa para comandos fixos (polimórfico)
    private final Map<String, Supplier<ViewState>> transitions = new HashMap<>();

public ProjectMenuState(ViewState parent) {
        this.parent = parent;
        queryService = WorkspaceQueryServiceImpl.getInstance();

        // 1. Registro de Comandos Fixos
        registerTransition("add-project", () -> new AddProjectState(this));
        registerTransition("voltar", () -> parent);

        // 2. Registro Dinâmico de Projetos
        for (String projectName : queryService.listProjectNames()) {
            registerTransition(projectName, () -> new KanbanMenuState(queryService, this, projectName));
        }
    }

    private void registerTransition(String command, Supplier<ViewState> stateSupplier) {
        transitions.put(command, stateSupplier);
    }

    @Override
    public Collection<String> getAutocompleteCommands() {
        // O autocompletar também se torna automático, baseado nas chaves do mapa
        return transitions.keySet();
    }

    @Override
    public void renderHeader(PromptService promptService) {
        clearConsole(promptService);
        ViewState.super.renderHeader(promptService);
        
        List<String> projects = queryService.listProjectNames();
        if (projects.isEmpty()) {
            promptService.printWarning("Nenhum projeto encontrado.");
        } else {
            projects.forEach(p -> promptService.printInfo("- " + p));
        }
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String trimmed = promptService.readString(getPromptText()).trim();
        
        // Polimorfismo puro: a lógica não sabe se está abrindo um projeto ou um comando
        Supplier<ViewState> transition = transitions.get(trimmed);
        
        if (transition != null) {
            return transition.get();
        }

        return this; // Se não encontrar, mantém o estado
    }

    @Override
    public String getPromptText() {
        return "Escolha um projeto ou digite 'add-project': ";
    }
}