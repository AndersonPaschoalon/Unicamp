package com.nexus.view.handlers;

import java.util.List;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.service.query.WorkspaceQueryServiceInterface;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class SetInitialStatusCLIHandler implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryServiceInterface queryService;

    public SetInitialStatusCLIHandler(CommandDispatcher dispatcher, WorkspaceQueryServiceInterface queryService) {
        this.dispatcher = dispatcher;
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "SET_INITIAL_STATUS";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        List<String> workflows = this.queryService.listWorkflowNames();
        if (workflows.isEmpty()) {
            prompt.printError("Nenhum workflow cadastrado.");
            return;
        }
        String workflow = prompt.readWithOptions(
                "Selecione o Workflow (Tab): ",
                workflows);
        List<String> statuses = this.queryService.listStatusNames();
        if (statuses.isEmpty()) {
            prompt.printError("Nenhum status cadastrado.");
            return;
        }
        String status = prompt.readWithOptions("Selecione o Status Inicial (Tab): ", statuses);
        Command command = new Command(getTriggerWord(), workflow, status);
        try {
            this.dispatcher.dispatch(command);
            prompt.printSuccess("Status inicial definido com sucesso.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de negócio: " + e.getMessage());

        } catch (NexusSystemException e) {
            prompt.printError("Falha estrutural: " + e.getMessage());
        }
    }
}