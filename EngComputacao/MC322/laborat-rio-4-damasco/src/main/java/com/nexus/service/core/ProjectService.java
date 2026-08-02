package com.nexus.service.core;

import java.time.LocalDate;
import java.util.List;

import com.nexus.exception.CommandFormatException;
import com.nexus.exception.EntityNotFoundException;
import com.nexus.model.Workflow;
import com.nexus.model.entity.Project;
import com.nexus.model.entity.User;
import com.nexus.model.valueobject.DateRange;
import com.nexus.model.valueobject.Role;
import com.nexus.model.valueobject.Sprint;
import com.nexus.service.Workspace;

public class ProjectService {
    private final UserService userService;
    private final WorkflowService workflowService;
    private final Workspace workspace;

    public ProjectService(UserService userService, WorkflowService workflowService, Workspace workspace) {
        this.userService = userService;
        this.workflowService = workflowService;
        this.workspace = workspace;
    }

    public List<Project> getProjects() {
        return workspace.findAllProjects();
    }

    public void createProject(String projectName, long totalBudget) {
        workspace.saveProject(new Project(projectName, totalBudget));
    }

    public Project getProjectByName(String projectName) {
        return workspace.getProject(projectName)
                .orElseThrow(() -> new EntityNotFoundException("Projeto '" + projectName + "' não encontrado."));
    }

    public void addTeamMemberToProject(String projectName, String username, String role) {
        Project project = getProjectByName(projectName);
        User user = userService.getUserByUsername(username);
        try {
            Role roleEnum = Role.valueOf(role);
            workspace.saveProject(project.withMember(user.getId(), roleEnum));
        } catch (IllegalArgumentException e) {
            throw new CommandFormatException(
                    "Papel '" + role + "' é inválido. Use 'VIEWER', 'CONTRIBUTOR' ou 'MANAGER'.");
        }
    }

    public boolean hasChangeStatusPermission(Project project, User user) {
        Role role = project.getTeamMembers().get(user.getId());
        return role == Role.CONTRIBUTOR || role == Role.MANAGER;
    }

    public void linkWorkflow(String projectName, String workflowName) {
        Project project = getProjectByName(projectName);
        Workflow workflow = workflowService.getWorkflowByName(workflowName);
        workspace.saveProject(project.withWorkflowId(workflow.getId()));
    }

    public void createSprint(String projectName, String sprintName, LocalDate start, LocalDate end) {
        Project project = getProjectByName(projectName);
        Sprint sprint = new Sprint(sprintName, new DateRange(start, end));
        workspace.saveProject(project.withSprint(sprint));
    }

    public void addTaskToSprint(String projectName, String sprintName, String taskId) {
        Project project = getProjectByName(projectName);
        Sprint sprint = project.getSprints().get(sprintName);
        if (sprint == null) {
            throw new EntityNotFoundException(
                    "Sprint '" + sprintName + "' não encontrada no projeto '" + projectName + "'.");
        }
        workspace.saveProject(project.withSprint(sprint.withTaskAdded(taskId)));
    }
}