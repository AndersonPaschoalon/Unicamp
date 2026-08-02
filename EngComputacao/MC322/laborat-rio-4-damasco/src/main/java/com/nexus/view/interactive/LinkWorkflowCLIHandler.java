package com.nexus.view.interactive;

import java.util.List;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.view.PromptService;

/**
 * Interactive handler for linking a workflow to a project.
 *
 * <p>Guides the user through selecting a project and a workflow —
 * both with Tab-completion — then dispatches a {@code LINK_WORKFLOW}
 * command.</p>
 */
public class LinkWorkflowCLIHandler implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryService queryService;

    /**
     * Constructs the handler with the required dependencies.
     *
     * @param dispatcher   the command dispatcher for execution delegation
     * @param queryService the read-only query service for listings
     */
    public LinkWorkflowCLIHandler(CommandDispatcher dispatcher, WorkspaceQueryService queryService) {
        this.dispatcher = dispatcher;
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "LINK_WORKFLOW";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        prompt.printHeader("NEXUS: VINCULAR WORKFLOW");
        prompt.printInfo("");

        List<String> projects = queryService.listProjectNames();
        if (projects.isEmpty()) {
            prompt.printError("Nenhum projeto cadastrado.");
            return;
        }
        prompt.printNumberedList("Projetos Disponíveis:", projects);
        prompt.printInfo("");
        prompt.printFooter();
        String projectName = prompt.readWithOptions("> Selecione o Projeto (ou Tab para sugestões): ", projects);

        List<String> workflows = queryService.listWorkflowNames();
        if (workflows.isEmpty()) {
            prompt.printError("Nenhum workflow cadastrado.");
            return;
        }
        prompt.printInfo("");
        prompt.printNumberedList("Workflows Disponíveis:", workflows);
        prompt.printInfo("");
        prompt.printFooter();
        String workflowName = prompt.readWithOptions("> Selecione o Workflow (ou Tab para sugestões): ", workflows);

        String[] args = {projectName, workflowName};
        Command command = new Command("LINK_WORKFLOW", "LINK_WORKFLOW", args);

        try {
            dispatcher.dispatchCommand(command);
            prompt.printSuccess("Workflow '" + workflowName + "' vinculado ao projeto '" + projectName + "'.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }
}
