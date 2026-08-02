package com.nexus.view.interactive;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.view.PromptService;

/**
 * Interactive handler for guided workflow creation.
 *
 * <p>Collects the workflow name and dispatches a {@code CREATE_WORKFLOW}
 * command.</p>
 */
public class AddWorkflowCLIHandler implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;

    /**
     * Constructs the handler with the required dispatcher.
     *
     * @param dispatcher the command dispatcher for execution delegation
     */
    public AddWorkflowCLIHandler(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getTriggerWord() {
        return "ADD_WORKFLOW";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        String name = prompt.readString("Nome do Workflow: ");

        String[] args = {name};
        Command command = new Command("CREATE_WORKFLOW", "CREATE_WORKFLOW", args);

        try {
            dispatcher.dispatchCommand(command);
            prompt.printSuccess("Workflow '" + name + "' criado com sucesso.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }
}
