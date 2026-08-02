package com.nexus.view;

import java.util.List;

import com.nexus.exception.NexusDomainException;
import com.nexus.model.entity.Project;
import com.nexus.model.entity.Task;
import com.nexus.service.query.WorkspaceQueryServiceInterface;

public class KanbanBoardView extends AbstractNexusView {

    private final Project project;

    public KanbanBoardView(Project project, WorkspaceQueryServiceInterface queryService, ViewState parentView) {
        super(queryService, parentView);
        this.project = project;
    }

    @Override
    public void render(PromptService prompt) {
        printHeader("KANBAN telinhas: " + project.getName());

        List<String> colunas = queryService.listStatusNames();
        

        for (String coluna : colunas) {
            System.out.printf("\n[ %s ]\n", coluna.toUpperCase());

            List<Task> tarefas = queryService.findTasksByProjectAndStatus(project, coluna);
            //System.out.println(tarefas);

            if (tarefas.isEmpty()) {
                System.out.println("  - (Nenhuma tarefa neste status)");
            } else {
                for (Task tarefa : tarefas) {

                    if (tarefa.getOwner() != null) {
                        System.out.printf("  - %s: %s (Resp: %s)\n",
                                tarefa.getId(), tarefa.getTitle(), tarefa.getOwner());
                    } else {
                        System.out.printf("  - %s: %s\n", tarefa.getId(), tarefa.getTitle());
                    }
                }
            }
        }

        System.out.println("\n--------------------------------------------------");
    }

    @Override
    public ViewState handleInput(String input) {

        ViewState globalState = handleGlobalCommands(input);

        if (globalState != null) {
            return globalState;
        }

        Task tarefaBuscada = queryService.getTaskById(input);
        if (project.isTaskInProject(tarefaBuscada)) {

            return new TaskDetailView(tarefaBuscada, project, this.queryService, this);
        }

        throw new NexusDomainException("Tarefa '" + input + "' não encontrada no projeto " + project.getName() + ".");
    }
}