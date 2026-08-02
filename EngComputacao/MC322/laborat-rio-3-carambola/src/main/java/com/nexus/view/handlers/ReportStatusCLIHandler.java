package com.nexus.view.handlers;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class ReportStatusCLIHandler implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;

    public ReportStatusCLIHandler(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getTriggerWord() {
        return "REPORT_STATUS";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        Command command = new Command(getTriggerWord(), new String[] {});
        try {
            dispatcher.dispatch(command);
            prompt.printSuccess("Relatório geral gerado com sucesso.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }
}