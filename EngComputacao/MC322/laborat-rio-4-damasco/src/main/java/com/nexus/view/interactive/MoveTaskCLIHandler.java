package com.nexus.view.interactive;

import java.util.List;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.view.PromptService;

/**
 * Interactive handler for guided task status change.
 *
 * <p>This is a critical delegation test: the handler collects the task ID,
 * the acting user, and queries the read-only service to present
 * <strong>exclusively</strong> the allowed destination statuses via
 * Tab-completion, then dispatches a {@code CHANGE_STATUS} command.</p>
 */
public class MoveTaskCLIHandler implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryService queryService;

    /**
     * Constructs the handler with the required dependencies.
     *
     * @param dispatcher   the command dispatcher for execution delegation
     * @param queryService the read-only query service for transition data
     */
    public MoveTaskCLIHandler(CommandDispatcher dispatcher, WorkspaceQueryService queryService) {
        this.dispatcher = dispatcher;
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "MOVE_TASK";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        prompt.printHeader("NEXUS: MOVER TAREFA");
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

        List<String> taskIds = queryService.listTaskIdsByProject(projectName);
        if (taskIds.isEmpty()) {
            prompt.printError("Este projeto não possui tarefas.");
            return;
        }
        prompt.printInfo("");
        prompt.printNumberedList("Tarefas do Projeto:", taskIds);
        prompt.printInfo("");
        prompt.printFooter();
        String taskId = prompt.readWithOptions("> Selecione o ID da Tarefa (ou Tab para sugestões): ", taskIds);

        List<String> allowedStatuses = queryService.listAllowedTransitions(taskId);
        if (allowedStatuses.isEmpty()) {
            prompt.printWarning("Não há transições permitidas para esta tarefa no estado atual.");
            return;
        }
        prompt.printInfo("");
        prompt.printNumberedList("Transições Permitidas:", allowedStatuses);
        prompt.printInfo("");
        prompt.printFooter();
        String newStatus = prompt.readWithOptions("> Status de Destino (ou Tab para sugestões): ", allowedStatuses);

        List<String> users = queryService.listUserNames();
        prompt.printInfo("");
        prompt.printNumberedList("Usuários Disponíveis:", users);
        prompt.printInfo("");
        prompt.printFooter();
        String userName = prompt.readWithOptions("> Usuário atuante (ou Tab para sugestões): ", users);

        String[] args = {taskId, newStatus, userName};
        Command command = new Command("CHANGE_STATUS", "CHANGE_STATUS", args);

        try {
            dispatcher.dispatchCommand(command);
            prompt.printSuccess("Tarefa '" + taskId + "' movida para '" + newStatus + "'.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }
}
