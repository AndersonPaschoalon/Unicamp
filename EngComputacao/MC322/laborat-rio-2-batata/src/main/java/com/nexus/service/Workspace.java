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
 * All task and project collections are maintained internally and exposed as unmodifiable views to
 * preserve encapsulation. The class uses streaming operations for efficient filtering and
 * aggregation of task and project data.
 * 
 * @author Workspace Team
 * @version 1.0
 */
package com.nexus.service;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.nexus.exception.NexusValidationException;
import com.nexus.model.Project;
import com.nexus.model.Status;
import com.nexus.model.Task;
import com.nexus.model.User;


public class Workspace {

    private final Map<Integer, Task> tasks = new HashMap<Integer, Task>();
    private final Map<String, Project> projects = new HashMap<>();
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Status> statuses = new HashMap<>();
    private final Map<String, Workflow> workflows = new HashMap<>();
    private final String hLine =
            "===============================================================================";

    /**
     * Class contructor.
     */
    public Workspace() {}

    //
    // Setters
    //

    /**
     * Adds a task to the workspace.
     *
     * @param task the {@link Task} object to be added to the workspace
     * @throws IllegalArgumentException if task is null
     * @throws NexusValidationException if the task has already been added
     */
    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task não pode ser nula.");
        }
        int id = task.getId();
        if (this.tasks.containsKey(id)) {
            throw new NexusValidationException("Task já adicionada. Fornecer outra task.");
        }
        this.tasks.put(id, task);
    }

    /**
     * Adds a project to the workspace.
     *
     * @param project the {@link Project} object to be added to the workspace
     * @throws IllegalArgumentException if the project name is null or empty
     * @throws NexusValidationException if a project with the same name already exists
     */
    public void addProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Projeto não pode ser nulo.");
        }
        if (project.getName() == null || project.getName().isBlank()) {
            throw new IllegalArgumentException("Nome do projeto não pode ser vazio.");
        }
        String key = project.getName().trim().toLowerCase();
        if (this.projects.containsKey(key)) {
            throw new NexusValidationException(
                    "Projeto com nome fornecido já existe. Utilize outro nome.");
        }
        this.projects.put(key, project);
    }

    /**
     * Adds a status to the workspace.
     *
     * @param status the {@link Status} object to be added to the workspace
     * @throws IllegalArgumentException if status or its name is null
     * @throws NexusValidationException if a status with the same name already exists
     */
    public void addStatus(Status status) {
        if (status == null || status.getName() == null) {
            throw new IllegalArgumentException("Status/Nome do Status não pode ser nulo.");
        }
        String key = status.getName().trim().toLowerCase();
        if (this.statuses.containsKey(key)) {
            throw new NexusValidationException(
                    "Status com nome fornecido já existe. Utilize outro nome.");
        }
        this.statuses.put(key, status);
    }

    /**
     * Adds a user to the workspace.
     *
     * @param user the {@link User} object to be added to the workspace
     * @throws IllegalArgumentException if user or its username is null
     * @throws NexusValidationException if a user with the same username already exists
     */
    public void addUser(User user) {
        if (user == null || user.consultUsername() == null) {
            throw new IllegalArgumentException("User/Nome do User não pode ser nulo.");
        }
        String key = user.consultUsername().trim().toLowerCase();
        if (this.users.containsKey(key)) {
            throw new NexusValidationException(
                    "Usuário com este username já existe. Utilize outro username.");
        }
        this.users.put(key, user);
    }

    /**
     * Adds a workflow to the workspace.
     *
     * @param workflow the {@link Workflow} object to be added to the workspace
     * @throws IllegalArgumentException if workflow or its name is null
     * @throws NexusValidationException if a workflow with the same name already exists
     */
    public void addWorkflow(Workflow workflow) {
        if (workflow == null || workflow.getName() == null) {
            throw new IllegalArgumentException("Workflow/Nome do Workflow não pode ser nulo.");
        }
        String key = workflow.getName().trim().toLowerCase();
        if (this.workflows.containsKey(key)) {
            throw new NexusValidationException(
                    "Workflow com nome fornecido já existe. Utilize outro nome.");
        }
        this.workflows.put(key, workflow);
    }

    //
    // Unitary getters
    //

    /**
     * Retrieves a project from the workspace by its name.
     *
     * @param name the name of the project to retrieve (case-insensitive)
     * @return the {@link Project} with the specified name
     * @throws NexusValidationException if name is null or no project with the given name is found
     */
    public Project getProjectByName(String name) throws NexusValidationException {
        if (name == null) {
            throw new NexusValidationException("Projeto com nome nulo");
        }
        Project p = projects.get(name.trim().toLowerCase());
        if (p == null) {
            throw new NexusValidationException("Projeto nao encontrado");
        }
        return p;
    }

    /**
     * Retrieves a task from the workspace by its unique identifier.
     *
     * @param id the unique identifier of the task to retrieve
     * @return the {@link Task} object with the specified id
     * @throws NexusValidationException if no task with the given id is found in the workspace
     */
    public Task getTaskById(int id) throws NexusValidationException {
        Task task = this.tasks.get(id);
        if (task == null) {
            throw new NexusValidationException("Tarefa com ID '" + id + "' não encontrada.");
        }
        return task;
    }

    /**
     * Retrieves a status from the registry by its name.
     *
     * @param name the name of the status (case-insensitive)
     * @return the {@link Status} object
     * @throws IllegalArgumentException if name is null
     * @throws NexusValidationException if the status is not registered
     */
    public Status getStatusByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Status name não pode ser nulo.");
        }
        Status s = statuses.get(name.trim().toLowerCase());
        if (s == null) {
            throw new NexusValidationException("Status desconhecido: " + name);
        }
        return s;
    }

    /**
     * Retrieves a user from the registry by their username.
     *
     * @param name the username to look up (case-insensitive)
     * @return the {@link User} object
     * @throws IllegalArgumentException if name is null
     * @throws NexusValidationException if the user is not found
     */
    public User getUserByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("User name não pode ser nulo.");
        }
        User u = users.get(name.trim().toLowerCase());
        if (u == null) {
            throw new NexusValidationException("Usuário não encontrado: " + name);
        }
        return u;
    }

    /**
     * Retrieves a workflow from the registry by its name.
     *
     * @param name the name of the workflow (case-insensitive)
     * @return the {@link Workflow} object
     * @throws IllegalArgumentException if name is null
     * @throws NexusValidationException if the workflow is not found
     */
    public Workflow getWorkflowByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Workflow name não pode ser nulo.");
        }
        Workflow w = workflows.get(name.trim().toLowerCase());
        if (w == null) {
            throw new NexusValidationException("Workflow não encontrado: " + name);
        }
        return w;
    }


    //
    // Collection Getters
    //

    /**
     * Retrieves an unmodifiable list of all tasks in the workspace.
     *
     * @return an unmodifiable {@link List} of {@link Task} objects contained in this workspace
     */
    public Set<Task> getTasks() {
        return Set.copyOf(this.tasks.values());
    }

    /**
     * Retrieves an unmodifiable list of all projects in this workspace.
     * 
     * @return an unmodifiable {@link List} of {@link Project} objects contained in this workspace.
     *         The returned list cannot be modified directly, ensuring encapsulation of the internal
     *         projects collection. Any attempt to modify the list will throw an
     *         {@link UnsupportedOperationException}.
     * 
     * @see Collections#unmodifiableList(List)
     */
    public Set<Project> getProjects() {
        return Set.copyOf(this.projects.values());
    }

    //
    // Performance Getters
    //

    /**
     * Retrieves the top 3 performers based on the number of completed tasks.
     * 
     * This method filters all tasks to find those with a {@code DONE} status and a non-null owner,
     * then groups them by owner and counts the number of completed tasks per user. The results are
     * sorted in descending order by task count, and only the top 3 performers are returned.
     * 
     * @return a {@code List} of {@code User} objects representing the top 3 performers, sorted by
     *         the number of completed tasks in descending order. Returns an empty list if there are
     *         fewer than 3 performers or no completed tasks exist.
     */
    public List<User> getTopPerformers() {
        return this.tasks.values().stream()
                .filter(t -> t.getStatus().isTerminal() && t.getOwner() != null)
                .collect(groupingBy(Task::getOwner, counting())).entrySet().stream()
                .sorted(Map.Entry.<User, Long>comparingByValue(Comparator.reverseOrder())).limit(3)
                .map(Map.Entry::getKey).toList();
    }

    /**
     * Retrieves a list of users who are overloaded with tasks.
     * 
     * A user is considered overloaded if they own more than 10 tasks that are currently in
     * progress.
     * 
     * @return a list of {@link User} objects representing overloaded users. Returns an empty list
     *         if no users meet the overload criteria.
     */
    public List<User> getOverloadedUsers() {
        return this.tasks.values().stream()
                .filter(t -> !t.getStatus().isTerminal() && t.getOwner() != null)
                .collect(groupingBy(Task::getOwner, counting())).entrySet().stream()
                .filter(entry -> entry.getValue() > 10).map(Map.Entry::getKey).toList();
    }


    /**
     * Calculates the health of a project based on the percentage of completed tasks.
     *
     * @param project the project to evaluate
     * @return the health percentage of the project (0-100), where 100 indicates all tasks are done.
     *         Returns 0 if the project has no tasks.
     */
    public double getProjectHealth(Project project) {
        List<Task> projectTasks = project.getTasks();
        long total = projectTasks.size();
        if (total == 0) {
            return 0;
        }
        long done = projectTasks.stream().filter(t -> t.getStatus().isTerminal()).count();
        return done * 100.0 / total;
    }


    /**
     * Retrieves the global bottleneck status based on the highest number of tasks.
     *
     * @return the {@link Status} that has the most tasks (excluding {@code DONE} tasks), or
     *         {@code null} if no tasks are found.
     */
    public Status getGlobalBottlenecks() {
        return this.tasks.values().stream()
                .filter(t -> !t.getStatus().isTerminal() && t.getOwner() != null)
                .collect(groupingBy(Task::getStatus, counting())).entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }


    /**
     * Aciona a impressão dos relatórios analíticos (Streams) no console. 1. Top Performers: Um
     * método que retorna os 3 usuários que possuem o maior número de tarefas no status DONE. 2.
     * Overloaded Users: Listar todos os usuários cuja carga de trabalho atual (IN_PROGRESS)
     * ultrapassa 10 tarefas. 3. Project Health: Para um dado projeto, calcular o percentual de
     * conclusão (Tarefas DONE / Total de Tarefas). 4. Global Bottlenecks: Identificar qual o status
     * que possui o maior número de tarefas no sistema (exceto DONE).
     */
    public void printRelatoriosAnaliticos() {
        System.out.println(hLine);
        System.out.println("== Relatórios Analíticos");
        System.out.println(hLine);
        System.out.println("");

        System.out.println("1. Top Performers");
        List<User> topPerformers = this.getTopPerformers();

        java.util.stream.IntStream.range(0, topPerformers.size())
                .forEach(i -> System.out.println((i + 1) + ". " + topPerformers.get(i)));
        System.out.println();

        System.out.println("2. Overloaded Users");
        List<User> overloaded = this.getOverloadedUsers();

        java.util.stream.IntStream.range(0, overloaded.size())
                .forEach(i -> System.out.println((i + 1) + ". " + overloaded.get(i)));
        System.out.println();

        System.out.println("3. Project Health");

        this.projects.values().stream().forEach(proj -> System.out.println(proj.getName()
                + " -> Porcentagem de Conclusão: " + this.getProjectHealth(proj) + "%"));
        System.out.println();

        System.out.println("4. Global Bottlenecks");
        System.out.println("Estado com mais tasks: " + this.getGlobalBottlenecks());
        System.out.println();

        System.out.println(hLine);
        System.out.println("");
    }

}
