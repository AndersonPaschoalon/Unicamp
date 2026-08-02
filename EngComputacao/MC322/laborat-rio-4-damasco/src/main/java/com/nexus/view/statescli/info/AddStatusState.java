package com.nexus.view.statescli.info;

import java.util.Collection;
import java.util.Collections;

import com.nexus.controller.CommandDispatcher;
import com.nexus.view.PromptService;
import com.nexus.view.statescli.ViewState;

public class AddStatusState implements ViewState {

    private final ViewState parent;
    private final CommandDispatcher dispatcher;

    public AddStatusState(ViewState parent) {
        this.parent = parent;
        this.dispatcher = CommandDispatcher.getInstance();
    }

    @Override
    public Collection<String> getAutocompleteCommands() {
        return Collections.emptyList();
    }

    @Override
    public void renderHeader(PromptService prompt) {
        clearConsole(prompt);
        ViewState.super.renderHeader(prompt);
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String input = promptService.readString("Nome do Novo Status: ");
        String nome = input.trim();

        String isFinal = promptService.readString("Este status é final? (s/n): ").trim().toLowerCase();
        if (isFinal.equals("s")) {
            isFinal = "true";
        } else if (isFinal.equals("n")) {
            isFinal = "false";
        } else {
            promptService.printError("Entrada inválida para status final. Use 's' para sim ou 'n' para não.");
            return this; // Permite tentar novamente sem perder o estado
        }
        
        try {
            String originalLine = "CREATE_STATUS;" + nome + ";" + isFinal;            
            dispatcher.process(originalLine);
            return parent; // Retorna ao menu de status após o sucesso
        } catch (Exception e) {
            return this;
        }
    }

    @Override
    public String getPromptText() {
        return "";
    }
}