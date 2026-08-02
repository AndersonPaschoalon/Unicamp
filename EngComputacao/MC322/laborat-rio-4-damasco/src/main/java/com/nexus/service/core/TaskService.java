package com.nexus.service.core;

import java.time.LocalDate;

import com.nexus.exception.EntityNotFoundException;
import com.nexus.exception.InvalidStateException;
import com.nexus.exception.InvalidTransitionException;
import com.nexus.exception.PermissionDeniedException;
import com.nexus.model.Workflow;
import com.nexus.model.entity.Project;
import com.nexus.model.entity.Task;
import com.nexus.model.entity.User;
import com.nexus.model.valueobject.Role;
import com.nexus.service.TelemetryRegistry;
import com.nexus.service.Workspace;

public class TaskService {
    private final UserService userService;
    private final ProjectService projectService;
    private final WorkflowService workflowService;
    private final Workspace workspace;
    private final TelemetryRegistry telemetryRegistry;

    public TaskService(UserService userService, ProjectService projectService,
            WorkflowService workflowService, Workspace workspace) {
        this.userService = userService;
        this.projectService = projectService;
        this.workflowService = workflowService;
        this.workspace = workspace;
        this.telemetryRegistry = TelemetryRegistry.getInstance();
    }

    public void createTask(String title, LocalDate deadline, long estimatedEffort, String projectName) {
        createTask(title, deadline, estimatedEffort, projectName, null);
    }

    public void createTask(String title, LocalDate deadline, long estimatedEffort, String projectName,
            LocalDate dueDate) {
        Project project = projectService.getProjectByName(projectName);

        if (project.getWorkflowId() == null) {
            throw new InvalidStateException("O projeto '" + projectName + "' não tem um workflow vinculado.");
        }
        Workflow workflow = workflowService.getWorkflowByName(project.getWorkflowId());
        if (workflow.getInitialStatusId() == null) {
            throw new InvalidStateException(
                    "O workflow do projeto '" + projectName + "' não tem um estado inicial definido.");
        }

        long usedBudget = workspace.findAllTasks().stream()
                .filter(t -> project.getId().equals(t.getProjectId()))
                .mapToLong(Task::getEstimatedEffort)
                .sum();
        if (usedBudget + estimatedEffort > project.getTotalBudget()) {
            throw new InvalidStateException("Adicionar esta tarefa excederia o orçamento total do projeto.");
        }

        Task task = new Task(title, deadline, estimatedEffort, project.getId(),
                null, dueDate, null, workflow.getInitialStatusId());
        workspace.saveTask(task);
        telemetryRegistry.incrementTasksCreated();
    }

    public Task getTaskById(String taskId) {
        return workspace.getTask(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa com ID '" + taskId + "' não encontrada."));
    }

    public void assignUserToTask(String taskId, String username) {
        Task task = getTaskById(taskId);
        User user = userService.getUserByUsername(username);
        workspace.saveTask(task.withOwnerId(user.getId()));
    }

    public void changeTaskStatus(String taskId, String newStatusName, String username) {
        Task task = getTaskById(taskId);
        User user = userService.getUserByUsername(username);
        Project project = workspace.getProject(task.getProjectId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Projeto '" + task.getProjectId() + "' não encontrado."));

        Role userRole = project.getTeamMembers().get(user.getId());
        if (userRole == null || userRole == Role.VIEWER) {
            throw new PermissionDeniedException(
                    "Usuário '" + username + "' não tem permissão para mudar o status de tarefas.");
        }

        if (project.getWorkflowId() == null) {
            throw new InvalidStateException("O projeto não possui um workflow vinculado.");
        }
        Workflow workflow = workflowService.getWorkflowByName(project.getWorkflowId());

        workflowService.getStatusByName(newStatusName);

        String currentStatusId = task.getStatusId();
        if (!workflow.canTransition(currentStatusId, newStatusName)) {
            throw new InvalidTransitionException("Transição de status de '" +
                    (currentStatusId != null ? currentStatusId : "null") +
                    "' para '" + newStatusName + "' não é permitida pelo workflow do projeto.");
        }

        workspace.saveTask(task.withStatusId(newStatusName));

        boolean prevInProgress = "IN_PROGRESS".equals(currentStatusId);
        boolean nextInProgress = "IN_PROGRESS".equals(newStatusName);
        if (!prevInProgress && nextInProgress) {
            telemetryRegistry.incrementActiveWorkload();
        } else if (prevInProgress && !nextInProgress) {
            telemetryRegistry.decrementActiveWorkload();
        }
    }
}