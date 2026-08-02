package com.nexus.view.handlers;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class CreateStatusCLIHandler implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;

    public CreateStatusCLIHandler(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getTriggerWord() {
        return "CREATE_STATUS";
    }

    @Override
    public void processInteractive(PromptService prompt) {

        // 1. Coleta guiada
        String statusName = prompt.readString("Nome do status: ");
        String terminalInput = prompt.readString("É terminal? (S/N): ");
        String isTerminal = terminalInput.equalsIgnoreCase("S") ? "true" : "false";

        Command command = new Command(getTriggerWord(), statusName, isTerminal);

        // 3. Delegação
        try {
            dispatcher.dispatch(command);
            prompt.printSuccess("Status criado com sucesso.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }
}