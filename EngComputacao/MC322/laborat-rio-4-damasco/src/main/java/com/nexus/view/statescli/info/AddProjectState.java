package com.nexus.view.statescli.info;

import java.util.Collection;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.PermissionDeniedException;
import com.nexus.model.valueobject.Role;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.view.PromptService;
import com.nexus.view.SessionContext;
import com.nexus.view.statescli.ViewState;

public class AddProjectState implements ViewState {

    private final ViewState parent;
    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryService queryService;

    public AddProjectState(ViewState parent) {
        // Gate MANAGER — CREATE_PROJECT é ação estrutural (mesmo nível já
        // aplicado no AddUserState da CLI e no AddProjectState da Web).
        // Resolve a verificação cruzada apontada na seção 1 do plano de ataque.
        if (!SessionContext.getInstance().hasAtLeastRole(Role.MANAGER)) {
            throw new PermissionDeniedException("Apenas MANAGER pode criar projetos.");
        }
        this.parent = parent;
        this.dispatcher = CommandDispatcher.getInstance(); // Acessa o dispatcher centralizado
        this.queryService = WorkspaceQueryServiceImpl.getInstance(); // Acessa o serviço de consulta centralizado
    }

    @Override
    public Collection<String> getAutocompleteCommands() {
        return queryService.listWorkflowNames();
    }

    @Override
    public void renderHeader(PromptService prompt) {
        clearConsole(prompt);
        ViewState.super.renderHeader(prompt);
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String name = promptService.readString("Nome do projeto: ").trim();
        String carga = promptService.readString("Carga horária: ").trim();
        String confirma = promptService.readString("Confirma criação do projeto? (s/n/voltar): ").trim().toLowerCase();

        if (confirma.equals("voltar")) {
            return parent; 
        } else if (confirma.equals("n")) {
            System.out.println("Criação de projeto cancelada.");
            return parent; 
        } else if (confirma.equals("s")) {
          try {
                String originalLine = "CREATE_PROJECT;" + name + ";" + carga;
                dispatcher.process(originalLine);
                
                return parent; 
            } catch (Exception e) {
                System.out.println("Erro ao criar projeto: " + e.getMessage());
                return this; 
            }
        } else {
            System.out.println("Entrada inválida. Por favor, responda com 's', 'n' ou 'voltar'.");
            return this; 
        }
    }

    @Override
    public String getPromptText() {
        return "Nome do projeto: ";
    }
}