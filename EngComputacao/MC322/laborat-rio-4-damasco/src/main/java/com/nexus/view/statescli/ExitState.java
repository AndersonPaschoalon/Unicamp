package com.nexus.view.statescli;

import com.nexus.view.PromptService;

/**
 * Representa o estado de encerramento da aplicação.
 */
public class ExitState implements ViewState {

    @Override
    public void renderHeader(PromptService prompt) {
        ViewState.super.renderHeader(prompt);
        prompt.printInfo("Encerrando o Nexus. Até logo!");
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        // O loop principal tratará a transição de saída
        return null;
    }

    // Autocomplete vazio ou não necessário
    @Override
    public java.util.List<String> getAutocompleteCommands() {
        return java.util.Collections.emptyList();
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}