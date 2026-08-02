package com.nexus.service.core;

import java.util.List;

import com.nexus.model.Workflow;
import com.nexus.model.entity.Project;
import com.nexus.model.entity.Task;
import com.nexus.model.entity.User;
import com.nexus.model.valueobject.Role;
import com.nexus.service.Workspace;

/**
 * Service class for managing projects, including creation, team management, and
 * task association.
 */
public class ProjectService {
    private final UserService userService;

    /**
     * Constructs a ProjectService with the given UserService dependency.
     *
     * @param userService the UserService to use for user lookups
     */
    public ProjectService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a new project and adds it to the workspace.
     *
     * @param projectName the name of the project
     * @param totalBudget the total budget for the project
     * @param workspace   the workspace to add the project to
     */
    public void createProject(String projectName, long totalBudget, Workspace workspace) {
        Project project = new Project(projectName, totalBudget);
        workspace.addProject(project);
        System.out.println("[LOG] Projeto criado: " + projectName);
    }

    /**
     * Adds a task to the specified project.
     *
     * @param project the project to add the task to
     * @param task    the task to add
     */
    public void addTaskToProject(Project project, Task task) {
        project.addTask(task);
    }

    /**
     * Retrieves a project by its name from the workspace.
     *
     * @param projectName the name of the project
     * @param workspace   the workspace to search in
     * @return the Project with the given name, or null if not found
     */
    public Project getProjectByName(String projectName, Workspace workspace) {
        return workspace.getProjectByName(projectName);
    }

    /**
     * Retrieves the project that contains the specified task.
     *
     * @param task      the task to search for
     * @param workspace the workspace to search in
     * @return the Project containing the task, or null if not found
     */
    public Project getProjectByTask(Task task, Workspace workspace) {
        return workspace.getProjectByTask(task);
    }

    /**
     * Adds a user as a team member to a project with a specific role.
     *
     * @param projectName the name of the project
     * @param username    the username of the user to add
     * @param role        the role to assign (must match a Role enum value)
     * @param users       the list of users to search
     * @param workspace   the workspace containing the project
     * @throws IllegalArgumentException if the role is invalid
     */
    public void addTeamMemberToProject(String projectName, String username, String role, Workspace workspace) {
        Project project = getProjectByName(projectName, workspace);
        User user = userService.getUserByUsername(username);
        try {
            Role roleEnum = Role.valueOf(role);
            project.addTeamMember(user, roleEnum);
            System.out.println("[LOG] Usuário '" + username + "' adicionado ao projeto '" + projectName
                    + "' com o papel de " + role);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Papel '" + role + "' é inválido. Use 'VIEWER', 'CONTRIBUTOR' ou 'MANAGER'.");
        }
    }

    /**
     * Checks if a user has permission to change the status of tasks in a project.
     *
     * @param project the project to check
     * @param user    the user to check
     * @return true if the user is a CONTRIBUTOR or MANAGER, false otherwise
     */
    public boolean hasChangeStatusPermission(Project project, User user) {
        Role role = project.getTeamMembers().get(user);
        return role == Role.CONTRIBUTOR || role == Role.MANAGER;
    }

    /**
     * Links a workflow to a project, enabling workflow-based status transitions for
     * the project's tasks.
     *
     * @param projectName  the name of the project to link the workflow to
     * @param workflowName the name of the workflow to link
     * @param workspace    the workspace containing both the project and workflow
     */
    public void linkWorkflow(String projectName, String workflowName, Workspace workspace) {
        Project project = getProjectByName(projectName, workspace);
        Workflow workflow = workspace.getWorkflowByName(workflowName);
        project.setWorkflow(workflow);
        System.out.println("[LOG] Workflow '" + workflowName + "' vinculado ao projeto '" + projectName + "'.");
    }

}