package com.nexus.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.nexus.exception.EntityNotFoundException;
import com.nexus.model.Workflow;
import com.nexus.model.entity.Project;
import com.nexus.model.entity.Status;
import com.nexus.model.entity.Task;
import com.nexus.model.entity.User;
import com.nexus.model.valueobject.Role;
import com.nexus.model.valueobject.Transition;

public class WorkspaceQueryServiceImpl implements WorkspaceQueryService {

    private final Workspace workspace;
    private static WorkspaceQueryServiceImpl instance;

    private WorkspaceQueryServiceImpl(Workspace workspace) {
        this.workspace = workspace;
    }

    public static WorkspaceQueryServiceImpl build(Workspace workspace) {
        if (instance != null) {
            throw new IllegalStateException("WorkspaceQueryServiceImpl already initialized.");
        }
        instance = new WorkspaceQueryServiceImpl(workspace);
        return instance;
    }

    public static WorkspaceQueryServiceImpl getInstance() {
        if (instance == null) {
            throw new IllegalStateException("WorkspaceQueryServiceImpl not initialized. Call build() first.");
        }
        return instance;
    }

    private Task requireTask(String taskId) {
        return workspace.getTask(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa com ID '" + taskId + "' não encontrada."));
    }

    private Project requireProject(String projectName) {
        return workspace.getProject(projectName)
                .orElseThrow(() -> new EntityNotFoundException("Projeto '" + projectName + "' não encontrado."));
    }

    @Override
    public List<String> listProjectNames() {
        return workspace.findAllProjects().stream().map(Project::getName).toList();
    }

    @Override
    public List<String> listUserNames() {
        return workspace.findAllUsers().stream().map(User::consultUsername).toList();
    }

    @Override
    public List<String> listRoles() {
        return Arrays.stream(Role.values()).map(Role::name).toList();
    }

    @Override
    public List<String> listWorkflowNames() {
        return workspace.findAllWorkflows().stream().map(Workflow::getName).toList();
    }

    @Override
    public List<String> listStatusNames() {
        return workspace.findAllStatuses().stream().map(Status::getName).toList();
    }

    @Override
    public List<String> listTaskIdsByProject(String projectName) {
        Project project = requireProject(projectName);
        return workspace.findAllTasks().stream()
                .filter(t -> project.getId().equals(t.getProjectId()))
                .map(Task::getId)
                .toList();
    }

    @Override
    public List<String> listAllowedTransitions(String taskId) {
        Task task = requireTask(taskId);
        String currentStatusId = task.getStatusId();
        if (currentStatusId == null) {
            return Collections.emptyList();
        }

        Project project = workspace.getProject(task.getProjectId()).orElse(null);
        if (project == null || project.getWorkflowId() == null) {
            return Collections.emptyList();
        }
        Workflow workflow = workspace.getWorkflow(project.getWorkflowId()).orElse(null);
        if (workflow == null) {
            return Collections.emptyList();
        }

        return workflow.getAllowedTransitions().stream()
                .filter(t -> t.getFromStatusId().equals(currentStatusId))
                .map(Transition::getToStatusId)
                .toList();
    }

    @Override
    public String getProjectWorkflowName(String projectName) {
        Project project = requireProject(projectName);
        return project.getWorkflowId() != null ? project.getWorkflowId() : "Sem Workflow";
    }

    @Override
    public List<String> listStatusNamesByProject(String projectName) {
        Project project = requireProject(projectName);
        if (project.getWorkflowId() == null) {
            return Collections.emptyList();
        }
        Workflow workflow = workspace.getWorkflow(project.getWorkflowId()).orElse(null);
        if (workflow == null) {
            return Collections.emptyList();
        }
        Set<String> names = new HashSet<>();
        if (workflow.getInitialStatusId() != null) {
            names.add(workflow.getInitialStatusId());
        }
        for (Transition t : workflow.getAllowedTransitions()) {
            names.add(t.getFromStatusId());
            names.add(t.getToStatusId());
        }
        return List.copyOf(names);
    }

    @Override
    public List<String> listTaskIdsByProjectAndStatus(String projectName, String statusName) {
        Project project = requireProject(projectName);
        return workspace.findAllTasks().stream()
                .filter(t -> project.getId().equals(t.getProjectId()))
                .filter(t -> {
                    String s = t.getStatusId();
                    if (s == null)
                        return statusName == null;
                    return s.equals(statusName);
                })
                .map(Task::getId)
                .toList();
    }

    @Override
    public String getTaskTitle(String taskId) {
        return requireTask(taskId).getTitle();
    }

    @Override
    public String getTaskStatusName(String taskId) {
        String statusId = requireTask(taskId).getStatusId();
        return statusId != null ? statusId : "Sem Status";
    }

    @Override
    public String getTaskOwnerName(String taskId) {
        String ownerId = requireTask(taskId).getOwnerId();
        if (ownerId == null)
            return "Não atribuído";
        return workspace.getUser(ownerId).map(User::consultUsername).orElse("Não atribuído");
    }

    @Override
    public String getTaskDeadline(String taskId) {
        return requireTask(taskId).getDeadline().toString();
    }

    @Override
    public String getTaskProjectName(String taskId) {
        Task task = requireTask(taskId);
        Project project = workspace.getProject(task.getProjectId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Projeto '" + task.getProjectId() + "' não encontrado."));
        return project.getName();
    }

    @Override
    public String getTaskOwnerRole(String taskId) {
        Task task = requireTask(taskId);
        String ownerId = task.getOwnerId();
        if (ownerId == null) {
            return "Sem Papel";
        }
        Project project = workspace.getProject(task.getProjectId()).orElse(null);
        if (project == null) {
            return "Sem Papel";
        }
        Role role = project.getTeamMembers().get(ownerId);
        return role != null ? role.name() : "Sem Papel";
    }

    @Override
    public String getUserRoleName(String username) {
        Optional<User> user = workspace.getUser(username);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("Usuário '" + username + "' não encontrado.");
        }
        return user.get().getRole().name();
    }

    @Override
    public boolean isTaskOverdue(String taskId) {
        Task task = requireTask(taskId);
        if (task.getDueDate() == null) {
            return false;
        }
        if (!task.getDueDate().isBefore(java.time.LocalDate.now())) {
            return false;
        }
        String statusId = task.getStatusId();
        if (statusId == null) {
            return true;
        }
        return workspace.getStatus(statusId).map(s -> !s.isTerminal()).orElse(true);
    }
}