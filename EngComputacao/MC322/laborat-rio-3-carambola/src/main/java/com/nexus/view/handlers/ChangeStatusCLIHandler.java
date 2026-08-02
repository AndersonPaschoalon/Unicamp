package com.nexus.view.handlers;

import java.util.List;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.service.query.WorkspaceQueryServiceInterface;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class ChangeStatusCLIHandler
        implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryServiceInterface queryService;

    public ChangeStatusCLIHandler(CommandDispatcher dispatcher, WorkspaceQueryServiceInterface queryService) {
        this.dispatcher = dispatcher;
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "MOVE_TASK";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        // Seleção de Task
        List<String> tasks = queryService.listTaskIds();
        if (tasks.isEmpty()) {
            prompt.printError("Não existem tarefas cadastradas.");
            return;
        }
        String taskId = prompt.readWithOptions("Selecione a Tarefa (Tab): ", tasks);

        // Próximos status válidos
        List<String> allowedStatuses = queryService.listAllowedTransitions(taskId);
        if (allowedStatuses.isEmpty()) {
            prompt.printError("Nenhuma transição disponível.");
            return;
        }
        String newStatus = prompt.readWithOptions("Novo Status (Tab): ", allowedStatuses);

        // Usuário atuante
        List<String> users = queryService.listUserNames();
        if (users.isEmpty()) {
            prompt.printError("Nenhum usuário cadastrado.");
            return;
        }
        String username = prompt.readWithOptions("Usuário Atuante (Tab): ", users);

        // Dispatch
        Command command = new Command(getTriggerWord(), taskId, newStatus, username);

        try {

            dispatcher.dispatch(command);
            prompt.printSuccess("Status alterado com sucesso.");

        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de negócio: " + e.getMessage());

        } catch (NexusSystemException e) {
            prompt.printError("Erro estrutural: " + e.getMessage());
        }
    }

}
