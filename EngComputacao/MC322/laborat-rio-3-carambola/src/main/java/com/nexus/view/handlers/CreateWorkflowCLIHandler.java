package com.nexus.view.handlers;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class CreateWorkflowCLIHandler implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;

    public CreateWorkflowCLIHandler(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getTriggerWord() {
        return "CREATE_WORKFLOW";
    }

    @Override
    public void processInteractive(PromptService prompt) {

        // 1. Coleta guiada
        String workflowName = prompt.readString("Nome do workflow: ");

        // 2. Empacotamento
        Command command = new Command(getTriggerWord(), workflowName);

        // 3. Delegação
        try {
            dispatcher.dispatch(command);
            prompt.printSuccess("Workflow criado com sucesso.");

        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());

        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }
}
