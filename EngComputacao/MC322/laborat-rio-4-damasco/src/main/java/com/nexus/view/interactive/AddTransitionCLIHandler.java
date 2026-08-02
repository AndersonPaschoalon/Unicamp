package com.nexus.view.interactive;

import java.util.List;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.view.PromptService;

/**
 * Interactive handler for guided workflow transition creation.
 *
 * <p>Guides the user through selecting a workflow, source status, and
 * destination status — all with Tab-completion — then dispatches an
 * {@code ALLOW_TRANSITION} command.</p>
 */
public class AddTransitionCLIHandler implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryService queryService;

    /**
     * Constructs the handler with the required dependencies.
     *
     * @param dispatcher   the command dispatcher for execution delegation
     * @param queryService the read-only query service for listings
     */
    public AddTransitionCLIHandler(CommandDispatcher dispatcher, WorkspaceQueryService queryService) {
        this.dispatcher = dispatcher;
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "ADD_TRANSITION";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        prompt.printHeader("NEXUS: ADICIONAR TRANSIÇÃO");
        prompt.printInfo("");

        List<String> workflows = queryService.listWorkflowNames();
        if (workflows.isEmpty()) {
            prompt.printError("Nenhum workflow cadastrado.");
            return;
        }
        prompt.printNumberedList("Workflows Disponíveis:", workflows);
        prompt.printInfo("");
        prompt.printFooter();
        String workflowName = prompt.readWithOptions("> Selecione o Workflow (ou Tab para sugestões): ", workflows);

        List<String> statuses = queryService.listStatusNames();
        if (statuses.isEmpty()) {
            prompt.printError("Nenhum status cadastrado.");
            return;
        }
        prompt.printInfo("");
        prompt.printNumberedList("Status Disponíveis:", statuses);
        prompt.printInfo("");
        prompt.printFooter();
        String fromStatus = prompt.readWithOptions("> Status de Origem (ou Tab para sugestões): ", statuses);
        String toStatus = prompt.readWithOptions("> Status de Destino (ou Tab para sugestões): ", statuses);

        String[] args = {workflowName, fromStatus, toStatus};
        Command command = new Command("ALLOW_TRANSITION", "ALLOW_TRANSITION", args);

        try {
            dispatcher.dispatchCommand(command);
            prompt.printSuccess("Transição '" + fromStatus + "' -> '" + toStatus + "' adicionada ao workflow '" + workflowName + "'.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }
}
