package com.nexus.view.interactive;

import java.util.List;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.view.PromptService;

/**
 * Interactive handler for guided team member addition.
 *
 * <p>Guides the user through selecting a project, user, and role —
 * all with Tab-completion — then dispatches an {@code ADD_MEMBER}
 * command.</p>
 */
public class AddTeamMemberCLIHandler implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryService queryService;

    /**
     * Constructs the handler with the required dependencies.
     *
     * @param dispatcher   the command dispatcher for execution delegation
     * @param queryService the read-only query service for listings
     */
    public AddTeamMemberCLIHandler(CommandDispatcher dispatcher, WorkspaceQueryService queryService) {
        this.dispatcher = dispatcher;
        this.queryService = queryService;
    }

    @Override
    public String getTriggerWord() {
        return "ADD_TEAM_MEMBER";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        prompt.printHeader("NEXUS: ADICIONAR MEMBRO");
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

        List<String> users = queryService.listUserNames();
        if (users.isEmpty()) {
            prompt.printError("Nenhum usuário cadastrado.");
            return;
        }
        prompt.printInfo("");
        prompt.printNumberedList("Usuários Disponíveis:", users);
        prompt.printInfo("");
        prompt.printFooter();
        String userName = prompt.readWithOptions("> Selecione o Usuário (ou Tab para sugestões): ", users);

        List<String> roles = queryService.listRoles();
        prompt.printInfo("");
        prompt.printNumberedList("Papéis Disponíveis:", roles);
        prompt.printInfo("");
        prompt.printFooter();
        String role = prompt.readWithOptions("> Selecione o Papel (ou Tab para sugestões): ", roles);

        String[] args = {projectName, userName, role};
        Command command = new Command("ADD_MEMBER", "ADD_MEMBER", args);

        try {
            dispatcher.dispatchCommand(command);
            prompt.printSuccess("Usuário '" + userName + "' adicionado ao projeto '" + projectName + "' como " + role + ".");
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }
}
