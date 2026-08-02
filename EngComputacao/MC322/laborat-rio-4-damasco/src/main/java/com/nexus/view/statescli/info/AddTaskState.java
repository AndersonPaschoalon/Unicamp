package com.nexus.view.statescli.info;

import java.util.Collection;
import java.util.Collections;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.model.Command;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.view.PromptService;
import com.nexus.view.statescli.ViewState;

public class AddTaskState implements ViewState {

    private final WorkspaceQueryService queryService;
    private final ViewState parent;
    private final String projectName;
    private final CommandDispatcher dispatcher;

    public AddTaskState(ViewState parent, String projectName) {
        this.queryService = WorkspaceQueryServiceImpl.getInstance();
        this.dispatcher = CommandDispatcher.getInstance();
        this.parent = parent;
        this.projectName = projectName;
    }

    @Override
    public Collection<String> getAutocompleteCommands() {
        return Collections.singletonList("voltar");
    }

    @Override
    public void renderHeader(PromptService prompt) {
        prompt.printInfo("=== CRIAR NOVA TAREFA ===");
        prompt.printInfo("Projeto: " + projectName);
        prompt.printInfo("Dica: Digite 'voltar' no título para cancelar o processo.");
        prompt.printInfo("");
    }

    @Override
    public String getPromptText() {
        return "> Digite o título da nova tarefa: ";
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String title = promptService.readString("Título da tarefa: ").trim();
        String deadline = promptService.readString("Prazo final (AAAA-MM-DD): ").trim();
        String estimatedHours = promptService.readString("Horas estimadas: ").trim();
        String dueDate = promptService.readString("Data de vencimento (AAAA-MM-DD, opcional, Enter para pular): ")
                .trim();

        String confirma = promptService.readString("Confirma criação da tarefa? (s/n/voltar): ").trim().toLowerCase();

        if (confirma.equals("voltar")) {
            return parent;
        } else if (confirma.equals("n")) {
            promptService.printInfo("Criação de tarefa cancelada.");
            return parent;
        } else if (confirma.equals("s")) {
            try {
                Command command = new Command(null, "CREATE_TASK",
                        new String[] { title, deadline, estimatedHours, projectName, dueDate });
                dispatcher.dispatchCommand(command);
                promptService.printInfo("Tarefa criada com sucesso!");
                return parent;
            } catch (NexusDomainException e) {
                promptService.printWarning("Erro ao criar tarefa: " + e.getMessage());
                return this;
            }
        } else {
            promptService.printWarning("Entrada inválida. Por favor, responda com 's', 'n' ou 'voltar'.");
            return this;
        }
    }
}