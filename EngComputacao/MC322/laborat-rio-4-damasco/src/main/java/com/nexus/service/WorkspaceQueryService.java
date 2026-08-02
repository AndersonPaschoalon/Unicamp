package com.nexus.service;

import java.util.List;

/**
 * Read-only projection service for the View layer.
 *
 * <p>
 * This interface defines a strict boundary between the interactive CLI
 * (View) and the domain model. It exposes only safe, formatted data —
 * primitive types and {@code String} lists — that the interface needs to
 * render menus and feed Tab-completion, without ever returning full domain
 * entities such as {@code Project}, {@code User}, or {@code Task}.
 * </p>
 *
 * <p>
 * Implementations may internally query the same aggregators and
 * repositories used by the Controller, but the compiler guarantees that
 * code holding a {@code WorkspaceQueryService} reference cannot perform
 * any write operation on the domain.
 * </p>
 *
 * @see com.nexus.view.PromptService
 */
public interface WorkspaceQueryService {

    /**
     * Lists the names of all registered projects.
     *
     * @return an unmodifiable list of project names
     */
    List<String> listProjectNames();

    /**
     * Lists the usernames of all registered users.
     *
     * @return an unmodifiable list of usernames
     */
    List<String> listUserNames();

    /**
     * Lists the display names of all available roles.
     *
     * @return an unmodifiable list of role names
     */
    List<String> listRoles();

    /**
     * Lists the names of all registered workflows.
     *
     * @return an unmodifiable list of workflow names
     */
    List<String> listWorkflowNames();

    /**
     * Lists the names of all registered statuses.
     *
     * @return an unmodifiable list of status names
     */
    List<String> listStatusNames();

    /**
     * Lists the task IDs belonging to a given project.
     *
     * @param projectName the name of the project
     * @return an unmodifiable list of task IDs within the project
     * @throws com.nexus.exception.EntityNotFoundException if the project does not
     *                                                     exist
     */
    List<String> listTaskIdsByProject(String projectName);

    /**
     * Lists the names of allowed destination statuses for a task,
     * based on the task's current status and the workflow of its project.
     *
     * <p>
     * This method is used by the interactive MOVE_TASK handler to
     * present only valid transitions in the Tab-completion menu.
     * </p>
     *
     * @param taskId the unique identifier of the task
     * @return an unmodifiable list of reachable status names, which may be
     *         empty if no transitions are allowed or the task has no workflow
     * @throws com.nexus.exception.EntityNotFoundException if the task does not
     *                                                     exist
     */
    List<String> listAllowedTransitions(String taskId);

    /**
     * Returns the workflow name associated with a project.
     *
     * @param projectName the project name
     * @return the workflow name, or {@code "Sem Workflow"} if none is linked
     */
    String getProjectWorkflowName(String projectName);

    /**
     * Lists the status names defined by the workflow of a project.
     *
     * @param projectName the project name
     * @return an ordered list of status names, empty if no workflow is linked
     */
    List<String> listStatusNamesByProject(String projectName);

    /**
     * Lists task IDs in a project that currently have the given status.
     *
     * @param projectName the project name
     * @param statusName  the status name to filter by
     * @return list of matching task IDs
     */
    List<String> listTaskIdsByProjectAndStatus(String projectName, String statusName);

    /**
     * Returns the title of a task.
     *
     * @param taskId the task identifier
     * @return the task title
     */
    String getTaskTitle(String taskId);

    /**
     * Returns the current status name of a task.
     *
     * @param taskId the task identifier
     * @return the status name, or {@code "Sem Status"} if none
     */
    String getTaskStatusName(String taskId);

    /**
     * Returns the username of the task owner.
     *
     * @param taskId the task identifier
     * @return the owner username, or {@code "Não atribuído"} if none
     */
    String getTaskOwnerName(String taskId);

    /**
     * Returns the deadline of a task as a formatted string.
     *
     * @param taskId the task identifier
     * @return the deadline string
     */
    String getTaskDeadline(String taskId);

    /**
     * Returns the name of the project that contains the given task.
     *
     * @param taskId the task identifier
     * @return the project name
     */
    String getTaskProjectName(String taskId);

    /**
     * Returns the role of the task owner within the task's project.
     *
     * @param taskId the task identifier
     * @return the role name, or {@code "Sem Papel"} if not assigned
     */
    String getTaskOwnerRole(String taskId);

    // Papel corporativo do usuário
    String getUserRoleName(String username);

    // true se a task tem dueDate vencida e ainda não está em status terminal.
    boolean isTaskOverdue(String taskId);
}
