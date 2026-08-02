package com.nexus.view;

import org.jline.reader.impl.completer.StringsCompleter;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.view.statescli.ViewState;
import com.nexus.view.statescli.menus.LoginState;

public class NexusCLI {
    private final PromptService promptService;
    private ViewState currentState;

    public NexusCLI(PromptService promptService, CommandDispatcher dispatcher, WorkspaceQueryService queryService) {
        this.promptService = promptService;
        this.currentState = new LoginState();
    }

    public void run() {
        while (true) {
            StringsCompleter commandCompleter = new StringsCompleter(currentState.getAutocompleteCommands());
            promptService.setActiveCompleter(commandCompleter);

            currentState.renderHeader(promptService);

            if (currentState.isTerminal()) {
                return;
            }

            try {
                ViewState nextState = currentState.handleInput(promptService);
                // Defensivo: nenhum ViewState deve retornar null (contrato exige sempre
                // um próximo estado válido). Caso ocorra, permanece no estado atual.
                currentState = (nextState != null) ? nextState : currentState;
            } catch (NexusDomainException e) {
                promptService.printWarning("Erro: " + e.getMessage());
                // Permanece no estado atual — exceção de domínio não derruba a app.
            }
        }
    }
}