package com.nexus.view.handlers;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class TopPerformersCLIHandler implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;

    public TopPerformersCLIHandler(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getTriggerWord() {
        return "TOP_PERFORMERS";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        Command command = new Command(getTriggerWord(), new String[] {});
        try {
            this.dispatcher.dispatch(command);
            prompt.printSuccess("Relatório de top performers gerado.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }
}