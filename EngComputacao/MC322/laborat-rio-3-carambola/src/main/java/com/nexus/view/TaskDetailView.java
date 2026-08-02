package com.nexus.view;

import com.nexus.exception.NexusDomainException;
import com.nexus.model.entity.Project;
import com.nexus.model.entity.Task;
import com.nexus.service.query.WorkspaceQueryServiceInterface;

public class TaskDetailView extends AbstractNexusView {

    private final Task task;
    private final Project project;

    public TaskDetailView(Task task, Project project, WorkspaceQueryServiceInterface queryService, ViewState parentView) {
        super(queryService, parentView);
        this.task = task;
        this.project = project;
    }

    @Override
    public void render(PromptService prompt) {

        printHeader("INSPEÇÃO: " + task.getId());

        System.out.println("\nTítulo: " + task.getTitle());
        
        String nomeProjeto = project.getName();
        System.out.println("Projeto: " + nomeProjeto);
        
        System.out.println("Nome do status Atual: " + task.getStatus().getName());

        System.out.println("Esforço: " + task.getEstimatedEffort());
        System.out.println("Owner da task: " + task.getOwner().consultUsername());        
        System.out.println("\n--------------------------------------------------");
    }

    @Override
    public ViewState handleInput(String input) {
        
        ViewState globalState = handleGlobalCommands(input);
        if (globalState != null) {
            return globalState;
        }

        if ("VOLTAR".equalsIgnoreCase(input.trim())) {
            return this.parentView;
        }

        //Como essa tela é fim da linha na árvore qualquer outra entrada diferente de voltar é considerada um comando invalido
        throw new NexusDomainException("Comando inválido. Digite VOLTAR para retornar ao quadro Kanban.");
    }
}