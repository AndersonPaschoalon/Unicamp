package com.nexus.view.handlers;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class CreateProjectCLIHandler
        implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;

    public CreateProjectCLIHandler(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getTriggerWord() {
        return "CREATE_PROJECT";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        String projectName = prompt.readString("Nome do Projeto: ");
        String totalBudget = prompt.readString("Orçamento Total: ");
        Command command = new Command(getTriggerWord(), projectName, totalBudget);

        try {
            dispatcher.dispatch(command);
            prompt.printSuccess("Projeto criado com sucesso.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }

}
