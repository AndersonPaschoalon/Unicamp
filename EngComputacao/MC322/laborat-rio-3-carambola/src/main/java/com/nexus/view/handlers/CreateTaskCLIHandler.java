package com.nexus.view.handlers;

import java.util.List;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.service.query.WorkspaceQueryServiceInterface;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class CreateTaskCLIHandler
        implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryServiceInterface queryService;

    public CreateTaskCLIHandler(CommandDispatcher dispatcher, WorkspaceQueryServiceInterface queryService) {
        this.dispatcher = dispatcher;
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "CREATE_TASK";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        String title = prompt.readString("Título da tarefa: ");
        String deadline = prompt.readString("Deadline (yyyy-MM-dd): ");
        String effort = prompt.readString("Esforço estimado: ");
        List<String> projects = queryService.listProjectNames();
        if (projects.isEmpty()) {
            prompt.printError("Nenhum projeto cadastrado.");
            return;
        }
        String project = prompt.readWithOptions("Selecione o Projeto (Tab): ", projects);
        Command command = new Command(getTriggerWord(), title, deadline, effort, project);

        try {
            dispatcher.dispatch(command);
            prompt.printSuccess("Tarefa criada com sucesso.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Erro estrutural: " + e.getMessage());
        } catch (Exception e) {
            prompt.printError("Entrada inválida: " + e.getMessage());
        }
    }
}