package com.nexus.service.core;

import java.time.LocalDate;
import java.util.List;

import com.nexus.exception.NexusSystemException;
import com.nexus.model.Workflow;
import com.nexus.model.entity.Project;
import com.nexus.model.entity.Status;
import com.nexus.model.entity.Task;
import com.nexus.model.entity.User;
import com.nexus.service.TelemetryRegistry;
import com.nexus.service.Workspace;

/**
 * Service class for managing tasks, including creation, assignment, and status
 * changes.
 */
public class TaskService {
    private final UserService userService;
    private final ProjectService projectService;
    private final TelemetryRegistry telemetryRegistry;

    /**
     * Constructs a TaskService with the required dependencies.
     *
     * @param userService       the UserService to use
     * @param projectService    the ProjectService to use
     * @param telemetryRegistry the TelemetryRegistry to use
     */
    public TaskService(UserService userService, ProjectService projectService, TelemetryRegistry telemetryRegistry) {
        this.userService = userService;
        this.projectService = projectService;
        this.telemetryRegistry = telemetryRegistry;
    }

    /**
     * Creates a new task and adds it to the specified project and workspace.
     *
     * @param title           the title of the task
     * @param deadline        the deadline for the task
     * @param estimatedEffort the estimated effort for the task
     * @param projectName     the name of the project to add the task to
     * @param workspace       the workspace to add the task to
     */
    public void createTask(String title, LocalDate deadline, long estimatedEffort, String projectName,
            Workspace workspace) {
        Project project = this.projectService.getProjectByName(projectName, workspace);
        Workflow workflow = project.getWorkflow();
        if (workflow == null || workflow.getInitialStatus() == null) {
            throw new NexusSystemException("O projeto '" + projectName + "' não tem um estado inicial definido.");
        }
        Task task = new Task(title, deadline, estimatedEffort);
        task.setStatus(workflow.getInitialStatus());
        this.projectService.addTaskToProject(project, task);
        workspace.addTask(task);
        this.telemetryRegistry.incrementTasksCreated();
        System.out.println("[LOG] Tarefa criada: " + title + " no projeto " + projectName);
    }

    /**
     * Assigns a user to a task by task ID.
     *
     * @param taskId    the ID of the task
     * @param username  the username of the user to assign
     * @param users     the list of users to search
     * @param workspace the workspace containing the task
     */
    public void assignUserToTask(String taskId, String username, Workspace workspace) {
        Task task = workspace.getTaskById(taskId);
        User user = this.userService.getUserByUsername(username);
        task.setOwner(user);
        System.out.println("[LOG] Usuário '" + username + "' atribuído à tarefa ID " + taskId);
    }

    /**
     * Changes the status of a task, if the user has permission.
     *
     * @param taskId    the ID of the task
     * @param newStatus the new status to set (e.g., "IN_PROGRESS", "DONE",
     *                  "BLOCKED")
     * @param username  the username of the user requesting the change
     * @param users     the list of users to search
     * @param workspace the workspace containing the task
     * @throws NexusValidationException if the user does not have permission
     */
    public void changeTaskStatus(String taskId, String newStatusName, String username, List<User> users,
            Workspace workspace) {
        Task task = workspace.getTaskById(taskId);
        Project project = this.projectService.getProjectByTask(task, workspace);
        User user = this.userService.getUserByUsername(username);
        Status newStatus = workspace.getStatusByName(newStatusName);
        project.processStatusChange(task, newStatus, user);
    }
}
