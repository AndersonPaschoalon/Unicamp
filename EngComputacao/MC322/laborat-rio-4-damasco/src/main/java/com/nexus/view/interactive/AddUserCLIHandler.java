package com.nexus.view.interactive;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.view.PromptService;

/**
 * Interactive handler for guided user creation.
 *
 * <p>Collects username and email via {@link PromptService}, builds a
 * {@code CREATE_USER} command, and delegates execution to the
 * {@link CommandDispatcher}.</p>
 */
public class AddUserCLIHandler implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;

    /**
     * Constructs the handler with the required dispatcher.
     *
     * @param dispatcher the command dispatcher for execution delegation
     */
    public AddUserCLIHandler(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getTriggerWord() {
        return "CREATE_USER";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        String name = prompt.readString("Nome: ");
        String email = prompt.readString("Email: ");

        String[] args = {name, email};
        Command command = new Command(getTriggerWord(), getTriggerWord(), args);

        try {
            dispatcher.dispatchCommand(command);
            prompt.printSuccess("Usuário cadastrado com sucesso.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }
}
