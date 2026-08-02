package com.nexus.view.handlers;

import java.util.List;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.service.query.WorkspaceQueryServiceInterface;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class AddMemberCLIHandler implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher; // O Controller
    private final WorkspaceQueryServiceInterface queryService; // Serviço puramente de leitura para a View

    public AddMemberCLIHandler(CommandDispatcher dispatcher, WorkspaceQueryServiceInterface queryService) {
        this.dispatcher = dispatcher;
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "ADD_MEMBER";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        // 1. Coleta guiada com autocompletar para TODOS os campos
        List<String> projectNames = queryService.listProjectNames();
        if (projectNames.isEmpty()) {
            prompt.printError("Não há projetos disponíveis para esta operação.");
            return;
        }
        String projectName = prompt.readWithOptions("Selecione o Projeto (Tab): ", projectNames);

        List<String> userNames = queryService.listUserNames();
        if (userNames.isEmpty()) {
            prompt.printError("Não há usuários cadastrados.");
            return;
        }
        String userName = prompt.readWithOptions("Selecione o Usuário (Tab): ", userNames);

        List<String> roles = queryService.listRoles();
        String role = prompt.readWithOptions("Selecione o Papel (Tab): ", roles);

        // 2. Empacotamento
        String[] commandArgs = { projectName, userName, role };
        Command command = new Command(getTriggerWord(), commandArgs);

        // 3. Delegação para o Controller com Polimorfismo de Exceções
        try {
            dispatcher.dispatch(command);
            prompt.printSuccess("Usuário atribuído com sucesso.");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }
}
