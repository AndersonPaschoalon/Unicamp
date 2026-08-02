package com.nexus.view.statescli.info;

import java.util.Collection;

import com.nexus.controller.CommandDispatcher;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.view.PromptService;
import com.nexus.view.statescli.ViewState;

/**
 * View state responsible for adding a new member to a specific project workspace.
 */
public class AddMemberState implements ViewState {

    private final WorkspaceQueryService queryService;
    private final ViewState parent;
    private final CommandDispatcher dispatcher; // Replace Object with your actual Dispatcher type
    private final String taskId;

    public AddMemberState(ViewState parent, String taskId) {
        this.parent = parent;
        this.taskId = taskId;
        this.queryService = WorkspaceQueryServiceImpl.getInstance();
        this.dispatcher = CommandDispatcher.getInstance();
    }

    @Override
    public Collection<String> getAutocompleteCommands() {
        Collection<String> commands = new java.util.ArrayList<>(queryService.listUserNames());
        commands.add("voltar");
        return commands;
    }

    @Override
    public void renderHeader(PromptService prompt) {
        prompt.printInfo("=== ADICIONAR MEMBRO AO PROJETO ===");
        prompt.printInfo("");
    }

    @Override
    public String getPromptText() {
        return "> Pressione [Enter] para iniciar os campos de cadastro...";
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String name = promptService.readString("Nome do membro: ").trim();
        String confirma = promptService.readString("Confirma adição do membro? (s/n/voltar): ").trim().toLowerCase();

        if (confirma.equals("voltar")){
            return parent; 
        } else if (confirma.equals("n")) {
            System.out.println("Adição de membro cancelada.");
            return parent; 
        } else if (confirma.equals("s")) {
            try {
                String originalLine = "ASSIGN_USER;" + taskId + ";" + name;
                dispatcher.process(originalLine);
                
                System.out.println("Membro adicionado com sucesso!");
                return parent; 
            } catch (Exception e) {
                System.out.println("Erro ao adicionar membro: " + e.getMessage());
                return this; 
            }
        } else {
            System.out.println("Entrada inválida. Por favor, responda com 's', 'n' ou 'voltar'.");
            return this; 
        }
    }
}