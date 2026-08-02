package com.nexus.view.handlers;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.service.query.WorkspaceQueryServiceInterface;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.ProjectSelectorView;
import com.nexus.view.PromptService;
import com.nexus.view.ViewState;

public class ViewKanbanCLIHandler implements InteractiveCLIHandler {
    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryServiceInterface queryService;

    public ViewKanbanCLIHandler(CommandDispatcher dispatcher, WorkspaceQueryServiceInterface queryService) {
        this.dispatcher = dispatcher;
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "VIEW_KANBAN";
    }

    @Override
    public void processInteractive(PromptService prompt) {

        Command command = new Command(getTriggerWord(), new String[] {});

        try {
            this.dispatcher.dispatch(command);
            ViewState currentState = new ProjectSelectorView(this.queryService, null);
            while (currentState != null) {
                currentState.render(prompt);
                String input = prompt.readString("> Selecione uma opção (ou VOLTAR): ");
                try {
                    currentState = currentState.handleInput(input);
                } catch (NexusDomainException e) {
                    prompt.printWarning("Aviso: " + e.getMessage());
                    prompt.readString("Pressione ENTER para continuar...");
                }
            }
            prompt.printSuccess("Navegação do Kanban encerrada.");

        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Erro estrutural: " + e.getMessage());
        }
    }

}
