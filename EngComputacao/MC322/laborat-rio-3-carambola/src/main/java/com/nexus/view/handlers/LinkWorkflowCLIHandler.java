package com.nexus.view.handlers;

import java.util.List;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.service.query.WorkspaceQueryServiceInterface;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class LinkWorkflowCLIHandler
        implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryServiceInterface queryService;

    public LinkWorkflowCLIHandler(CommandDispatcher dispatcher, WorkspaceQueryServiceInterface queryService) {
        this.dispatcher = dispatcher;
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "LINK_WORKFLOW";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        List<String> projects = this.queryService.listProjectNames();
        if (projects.isEmpty()) {
            prompt.printError("Nenhum projeto cadastrado.");
            return;
        }

        String project = prompt.readWithOptions("Selecione o Projeto (Tab): ", projects);
        List<String> workflows = this.queryService.listWorkflowNames();

        if (workflows.isEmpty()) {
            prompt.printError("Nenhum workflow cadastrado.");
            return;
        }

        String workflow = prompt.readWithOptions("Selecione o Workflow (Tab): ", workflows);
        Command command = new Command(getTriggerWord(), project, workflow);

        try {
            dispatcher.dispatch(command);
            prompt.printSuccess("Workflow vinculado com sucesso.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Erro estrutural: " + e.getMessage());
        }
    }
}
