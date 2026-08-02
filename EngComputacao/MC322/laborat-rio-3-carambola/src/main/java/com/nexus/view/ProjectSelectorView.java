package com.nexus.view;

import java.util.List;

import com.nexus.exception.NexusDomainException;
import com.nexus.model.entity.Project;
import com.nexus.service.query.WorkspaceQueryServiceInterface;

public class ProjectSelectorView extends AbstractNexusView {
    

    public ProjectSelectorView(WorkspaceQueryServiceInterface queryService, ViewState parentView) {
        super(queryService, parentView);
    }

    @Override
    public void render(PromptService prompt) {
        printHeader("Seleção de Projeto");
        System.out.println("\n Projetos Ativos");

        List<String> projectNames = getQueryService().listProjectNames();

        if(projectNames.isEmpty()){
            System.out.println("[Nenhum projeto encontrado]");
        } else{
            
            for (int i = 0; i < projectNames.size(); i++) {
                var proj = projectNames.get(i);
                System.out.printf("[%d] %s (Workflow: %s)\n", i + 1, proj, getQueryService().getWorkflowByProject(proj));
            }
        }
    }

    @Override
    public ViewState handleInput(String input) {

        ViewState nextState = handleGlobalCommands(input);

        if (nextState != null || "VOLTAR".equalsIgnoreCase(input)) {
            return null; 
        }

        List<String> projectNames = getQueryService().listProjectNames();

        try {
            int index = Integer.parseInt(input) - 1; //funciona pelo indice iniciado em 1 da ordem das tarefas
            String project = projectNames.get(index);
            
            if (index >= 0 && index < projectNames.size()) {
                Project projetoSelecionado = getQueryService().getProjectByName(project);
                
                return new KanbanBoardView(projetoSelecionado, this.queryService, this);
            }
            
        } catch (NumberFormatException e) {
            throw new NexusDomainException("Opção inválida! Selecione um número da lista de projetos.");
        }

       throw new NexusDomainException("Projeto não encontrado! Selecione um número válido da lista.");
    }

    public WorkspaceQueryServiceInterface getQueryService(){
        return this.queryService;
    }
}
