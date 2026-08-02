package com.nexus.view.handlers;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class CreateUserCLIHandler
        implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;

    public CreateUserCLIHandler(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getTriggerWord() {
        return "CREATE_USER";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        String username = prompt.readString("Username: ");
        String email = prompt.readString("Email: ");
        Command command = new Command(getTriggerWord(), username, email);

        try {
            dispatcher.dispatch(command);
            prompt.printSuccess("Usuário criado com sucesso.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Erro estrutural: " + e.getMessage());
        }
    }
}
