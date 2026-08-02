package com.nexus.view.handlers;

import java.util.List;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.service.query.WorkspaceQueryServiceInterface;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class AssignUserCLIHandler implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryServiceInterface queryService;

    public AssignUserCLIHandler(CommandDispatcher dispatcher, WorkspaceQueryServiceInterface queryService) {
        this.dispatcher = dispatcher;
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "ASSIGN_USER";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        List<String> tasks = queryService.listTaskIds();
        if (tasks.isEmpty()) {
            prompt.printError("Nenhuma tarefa cadastrada.");
            return;
        }

        String taskId = prompt.readWithOptions("Selecione a Tarefa (Tab): ", tasks);
        List<String> users = queryService.listUserNames();
        if (users.isEmpty()) {
            prompt.printError("Nenhum usuário cadastrado.");
            return;
        }

        String username = prompt.readWithOptions("Selecione o Usuário (Tab): ", users);
        Command command = new Command(getTriggerWord(), taskId, username);
        try {
            dispatcher.dispatch(command);
            prompt.printSuccess("Usuário atribuído com sucesso.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Erro estrutural: " + e.getMessage());
        }
    }
}