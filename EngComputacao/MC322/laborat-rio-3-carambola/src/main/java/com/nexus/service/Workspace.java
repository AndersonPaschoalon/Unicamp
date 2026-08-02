package com.nexus.service;

import com.nexus.exception.EntityNotFoundException;
import com.nexus.exception.NexusSystemException;
import com.nexus.exception.UnknownCommandException;
import com.nexus.model.Workflow;
import com.nexus.model.entity.Project;
import com.nexus.model.entity.Status;
import com.nexus.model.entity.Task;
import com.nexus.model.entity.User;
import com.nexus.model.valueobject.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.counting;

/**
 * Workspace class manages tasks and projects within a system.
 *
 * This class provides functionality to:
 * <ul>
 * <li>Add and retrieve tasks and projects</li>
 * <li>Identify top performing users based on completed tasks</li>
 * <li>Detect overloaded users with excessive in-progress tasks</li>
 * <li>Calculate project completion health metrics</li>
 * <li>Identify system bottlenecks by task status</li>
 * <li>Query tasks and projects by ID or name</li>
 * </ul>
 * 
 * <p>
 * All methods that work with task statuses include proper null checking to
 * handle tasks
 * that may not have a status assigned yet.
 * </p>
 *
 * @author Workspace Team
 * @version 1.0
 */
public class Workspace {
    private final List<Status> statuses = new ArrayList<>();
    private final List<Workflow> workflows = new ArrayList<>();
    private final List<Task> tasks = new ArrayList<>();
    private final Map<String, Project> projects = new HashMap<String, Project>();

    /**
     * Adds a task to the workspace.
     *
     * @param task the {@link Task} object to be added to the workspace
     */
    public void addTask(Task task) {
        tasks.add(task);
    }

    /**
     * Adds a project to the workspace.
     *
     * @param project the {@link Project} to be added to the workspace
     * @throws NullPointerException if the project parameter is null
     */
    public void addProject(Project project) throws NullPointerException {
        if (project == null)
            throw new NullPointerException("Project cannot be null");
        projects.put(project.getName(), project);
    }

    /**
     * Adds a status to the workspace.
     *
     * @param status the {@link Status} to be added to the workspace
     */
    public void addStatus(Status status) {
        statuses.add(status);
    }

    /**
     * Adds a workflow to the workspace.
     *
     * @param workflow the {@link Workflow} to be added to the workspace
     */
    public void addWorkflow(Workflow workflow) {
        workflows.add(workflow);
    }

    /**
     * Retrieves the top 3 performers based on the number of completed tasks.
     * This method filters all tasks to find those with a {@code DONE} status and a
     * non-null owner,
     * then groups them by owner and counts the number of completed tasks per user.
     * The results are sorted in descending order by task count, and only the top 3
     * performers are returned.
     * 
     * <p>
     * Tasks with null status are automatically excluded from the analysis.
     * </p>
     * 
     * @return a {@code List} of {@code User} objects representing the top 3
     *         performers,
     *         sorted by the number of completed tasks in descending order.
     *         Returns an empty list if there are fewer than 3 performers or no
     *         completed tasks exist.
     */
    public List<User> getTopPerformers() {
        return tasks.stream()
                .filter(t -> t.getStatus() != null && t.getStatus().getName().equals("DONE") && t.getOwner() != null)
                .collect(groupingBy(Task::getOwner, counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<User, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Retrieves a list of users who are overloaded with tasks.
     * 
     * A user is considered overloaded if they own more than 10 tasks
     * that are currently in progress.
     * 
     * <p>
     * Tasks with null status are automatically excluded from the analysis.
     * </p>
     * 
     * @return a list of {@link User} objects representing overloaded users.
     *         Returns an empty list if no users meet the overload criteria.
     */
    public List<User> getOverloadedUsers() {
        return tasks.stream()
                .filter(t -> t.getStatus() != null && t.getStatus().getName().equals("IN_PROGRESS")
                        && t.getOwner() != null)
                .collect(groupingBy(Task::getOwner, counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 10)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Calculates the health of a project based on the percentage of completed
     * tasks.
     * 
     * <p>
     * Tasks with null status are not counted as completed, but are included in the
     * total count.
     * </p>
     *
     * @param project the project to evaluate
     * @return the health percentage of the project (0-100), where 100 indicates all
     *         tasks are done.
     *         Returns 0 if the project has no tasks.
     */
    public double getProjectHealth(Project project) {
        Set<Task> projectTasks = project.getTasks();
        long total = projectTasks.size();
        if (total == 0) {
            return 0;
        }
        long done = projectTasks.stream()
                .filter(t -> t.getStatus() != null && t.getStatus().getName().equals("DONE"))
                .count();
        return done * 100.0 / total;
    }

    /**
     * Retrieves the global bottleneck status based on the highest number of tasks.
     * 
     * <p>
     * This method analyzes all tasks (excluding DONE tasks and tasks with null
     * status)
     * to find which status has the most tasks assigned to it, indicating a
     * potential bottleneck.
     * </p>
     *
     * @return the {@link Status} that has the most tasks (excluding {@code DONE}
     *         tasks and null statuses),
     *         or {@code null} if no qualifying tasks are found.
     */
    public String getGlobalBottlenecks() {
        return tasks.stream()
                .filter(t -> t.getStatus() != null && !t.getStatus().getName().equals("DONE") && t.getOwner() != null)
                .collect(groupingBy(Task::getStatus, counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .map(Status::getName)
                .orElse(null);
    }

    /**
     * Retrieves a project from the workspace by its name.
     * 
     * <p>
     * The lookup is case-sensitive and must match the project name exactly.
     * </p>
     *
     * @param name the name of the project to retrieve (case-sensitive)
     * @return the {@link Project} with the specified name
     * @throws EntityNotFoundException if no project with the given name is found
     */
    public Project getProjectByName(String name) throws EntityNotFoundException {
        Project p = projects.get(name);
        if (p != null) {
            return p;
        }
        throw new EntityNotFoundException("Projeto '" + name + "' não encontrado.");
    }

    /**
     * Retrieves the project that contains the specified task.
     *
     * @param task the task to search for
     * @return the {@link Project} that contains the specified task
     * @throws EntityNotFoundException if no project contains the given task
     */
    public Project getProjectByTask(Task task) throws EntityNotFoundException {
        return projects.values().stream()
                .filter(p -> p.isTaskInProject(task))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Projeto para a tarefa ID '" + task.getId() + "' não encontrado."));
    }

    /**
     * Retrieves a workflow from the workspace by its name.
     *
     * @param name the name of the workflow to retrieve
     * @return the {@link Workflow} with the specified name
     * @throws EntityNotFoundException if no workflow with the given name is found
     */
    public Workflow getWorkflowByName(String name) throws EntityNotFoundException {
        return workflows.stream()
                .filter(w -> w.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Workflow '" + name + "' não encontrado."));
    }

    /**
     * Retrieves a status from the workspace by its name.
     *
     * @param name the name of the status to retrieve
     * @return the {@link Status} with the specified name
     * @throws EntityNotFoundException if no status with the given name is found
     */
    public Status getStatusByName(String name) throws EntityNotFoundException {
        return statuses.stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Status '" + name + "' não encontrado."));
    }

    /**
     * Retrieves a task from the workspace by its unique identifier.
     *
     * @param id the unique identifier of the task to retrieve
     * @return the {@link Task} object with the specified id
     * @throws EntityNotFoundException if no task with the given id is found in the
     *                                 workspace
     */
    public Task getTaskById(String id) throws EntityNotFoundException {
        return tasks.stream()
                // .filter(t -> t.getId() == id)
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Tarefa com ID '" + id + "' não encontrada."));
    }

    /**
     * Retrieves an unmodifiable list of all tasks in the workspace.
     *
     * @return an unmodifiable {@link List} of {@link Task} objects contained in
     *         this workspace
     */
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Retrieves an unmodifiable list of all projects in this workspace.
     * 
     * @return an unmodifiable {@link List} of {@link Project} objects contained in
     *         this workspace.
     *         The returned list cannot be modified directly, ensuring encapsulation
     *         of the internal
     *         projects collection. Any attempt to modify the list will throw an
     *         {@link UnsupportedOperationException}.
     * 
     * @see Collections#unmodifiableList(List)
     */
    public List<Project> getProjects() {
        return Collections.unmodifiableList(new ArrayList<>(projects.values()));
    }

    /**
     * Retrieves the list of all statuses in the workspace.
     *
     * @return the mutable {@link List} of {@link Status} objects contained in this
     *         workspace
     */
    public List<Status> getStatuses() {
        return Collections.unmodifiableList(statuses);
    }

    /**
     * Retrieves the list of all workflows in the workspace.
     * 
     *
     * @return the mutable {@link List} of {@link Workflow} objects contained in
     *         this workspace
     */
    public List<Workflow> getWorkflows() {
        return Collections.unmodifiableList(workflows);
    }

    // Novos métodos

    public boolean hasProject(String name) {
        return projects.containsKey(name);
    }

    public boolean hasWorkflow(String name) {
        return workflows.stream()
                .anyMatch(w -> w.getName().equals(name));
    }

    public boolean hasStatus(String name) {
        return statuses.stream()
                .anyMatch(s -> s.getName().equals(name));
    }

    public List<Task> getTasksFromProject(String projectName) {
        Project project = getProjectByName(projectName);
        List<Task> tasks = project.getTasks().stream().toList();
        return Collections.unmodifiableList(tasks);
    }

    public Workflow getWorkflowFromProject(String projectName) {
        Project project = getProjectByName(projectName);
        return project.getWorkflow();
    }

    public List<Status> getAllowedTransitions(Task task) {
        Project project = getProjectByTask(task);

        Workflow workflow = project.getWorkflow();

        if (workflow == null || task.getStatus() == null) {
            return List.of();
        }

        return workflow.getAllowedTransitionsFrom(task.getStatus())
                .stream()
                .toList();
    }

    public Map<Project, Set<Task>> getAllProjectTasks() {
        return Collections.unmodifiableMap(
                projects.values()
                        .stream()
                        .collect(Collectors.toMap(
                                p -> p,
                                Project::getTasks)));
    }

    public Map<Project, Map<User, Role>> getAllProjectMembers() {
        return Collections.unmodifiableMap(
                projects.values()
                        .stream()
                        .collect(Collectors.toMap(
                                p -> p,
                                Project::getTeamMembers)));
    }

}