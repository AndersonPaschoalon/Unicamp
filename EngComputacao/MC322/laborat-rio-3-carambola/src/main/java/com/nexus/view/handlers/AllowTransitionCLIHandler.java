package com.nexus.view.handlers;

import java.util.List;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.service.query.WorkspaceQueryServiceInterface;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class AllowTransitionCLIHandler implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryServiceInterface queryService;

    public AllowTransitionCLIHandler(CommandDispatcher dispatcher, WorkspaceQueryServiceInterface queryService) {
        this.dispatcher = dispatcher;
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "ALLOW_TRANSITION";
    }

    @Override
    public void processInteractive(PromptService prompt) {

        List<String> workflows = queryService.listWorkflowNames();

        if (workflows.isEmpty()) {
            prompt.printError("Nenhum workflow cadastrado.");
            return;
        }

        String workflow = prompt.readWithOptions("Selecione o Workflow (Tab): ", workflows);
        List<String> statuses = queryService.listStatusNames();
        if (statuses.isEmpty()) {
            prompt.printError("Nenhum status cadastrado.");
            return;
        }

        String fromStatus = prompt.readWithOptions("Status Origem (Tab): ", statuses);

        String toStatus = prompt.readWithOptions("Status Destino (Tab): ", statuses);

        Command command = new Command(getTriggerWord(), workflow, fromStatus, toStatus);

        try {
            dispatcher.dispatch(command);
            prompt.printSuccess("Transição adicionada com sucesso.");

        } catch (NexusDomainException e) {

            prompt.printWarning("Regra de negócio: " + e.getMessage());

        } catch (NexusSystemException e) {

            prompt.printError("Erro estrutural: " + e.getMessage());
        }
    }
}
