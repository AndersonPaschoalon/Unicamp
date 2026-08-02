/**
 * Represents a project that manages tasks within a defined budget.
 * 
 * This class allows creating a project with a name and total budget,
 * and manages a collection of tasks. It ensures that the total estimated
 * effort of all tasks does not exceed the project's total budget.
 * 
 * When adding a new task, the class validates that the sum of all task
 * efforts does not surpass the budget limit. If it would, a 
 * NexusValidationException is thrown.
 */
package com.nexus.model.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.nexus.exception.NexusSystemException;
import com.nexus.model.Workflow;
import com.nexus.model.valueobject.Role;
import com.nexus.service.TelemetryRegistry;

/**
 * Represents a project that manages tasks within a defined budget.
 *
 * This class allows creating a project with a name and total budget,
 * and manages a collection of tasks. It ensures that the total estimated
 * effort of all tasks does not exceed the project's total budget.
 *
 * When adding a new task, the class validates that the sum of all task
 * efforts does not surpass the budget limit. If it would, a 
 * NexusValidationException is thrown.
 */
public class Project {
    private String name;
    private long totalBudget;
    private Workflow workflow;

    private final Map<User, Role> teamMembers = new HashMap<User, Role>();
    private final Set<Task> tasks = new HashSet<Task>();

    /**
     * Constructs a Project with the specified name and total budget.
     *
     * @param name the name of the project
     * @param totalBudget the total budget for the project
     */
    public Project(String name, long totalBudget) {
        this.name = name;
        this.totalBudget = totalBudget;
    }

    /**
     * Adds a task to the project if the total effort does not exceed the project's budget.
     *
     * @param task the Task object to be added to the project
     * @throws NexusSystemException if adding the task would cause the total estimated effort
     *         to exceed the project's total budget
     */
    public void addTask(Task task) throws NexusSystemException {
        int totalEffort = tasks.stream().mapToInt(t -> (int) t.getEstimatedEffort()).sum();
        if (totalEffort + task.getEstimatedEffort() > totalBudget) {
            throw new NexusSystemException("Adicionar esta tarefa excederia o orçamento total do projeto.");
        }
        tasks.add(task);
    }

    public void processStatusChange(Task task, Status newStatus, User user) {
        // 1. Validação de Acesso (RBAC)
        Role userRole = this.teamMembers.get(user);
        if (userRole == null || userRole == Role.VIEWER) {
            throw new NexusSystemException("Usuário '" + user.consultUsername() + "' não tem permissão para mudar o status de tarefas.");
        }

        // 2. Validação do Workflow
        if (this.workflow == null) {
            throw new NexusSystemException("O projeto não possui um workflow vinculado.");
        }

        Status currentStatus = task.getStatus();
        
        // Usa o seu método canTransition original que já lida com isTerminal()
        if (!this.workflow.canTransition(currentStatus, newStatus)) {
            throw new NexusSystemException("Transição de status de '" + 
                (currentStatus != null ? currentStatus.getName() : "null") + 
                "' para '" + newStatus.getName() + "' não é permitida pelo workflow do projeto.");
        }

        // 3. Efetivação
        task.setStatus(newStatus);

        // 4. Ajuste da Telemetria
        if (currentStatus != null && newStatus != null) {
            String prev = currentStatus.getName();
            String next = newStatus.getName();
            
            if (!"IN_PROGRESS".equals(prev) && "IN_PROGRESS".equals(next)) {
                TelemetryRegistry telemetry = TelemetryRegistry.getInstance();
                telemetry.incrementActiveWorkload();
            } else if ("IN_PROGRESS".equals(prev) && !"IN_PROGRESS".equals(next)) {
                TelemetryRegistry telemetry = TelemetryRegistry.getInstance();
                telemetry.decrementActiveWorkload();
            }
        }
    }

    /**
     * Adds a user as a team member to the project with the specified role.
     *
     * @param user the user to add
     * @param role the role to assign to the user
     */
    public void addTeamMember(User user, Role role) {
        teamMembers.put(user, role);
    }

    /**
     * Checks if a task is part of this project.
     *
     * @param task the task to check
     * @return true if the task is in the project, false otherwise
     */
    public boolean isTaskInProject(Task task) {
        return tasks.contains(task);
    }

    /**
     * Gets the name of the project.
     *
     * @return the project name
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the set of tasks associated with this project.
     *
     * @return a Set of Task objects contained in this project
     */
    public Set<Task> getTasks() {
        return tasks;
    }

    /**
     * Gets the map of team members and their roles for this project.
     *
     * @return a map of User to Role
     */
    public Map<User, Role> getTeamMembers() {
        return teamMembers;
    }

    /**
     * Retrieves the total budget allocated for this project.
     *
     * @return the total budget as a long value
     */
    public long getTotalBudget() {
        return totalBudget;
    }

    /**
     * Gets the workflow associated with this project.
     *
     * @return the workflow for this project, or null if no workflow is set
     */
    public Workflow getWorkflow() {
        return workflow;
    }

    /**
     * Sets the workflow for this project.
     *
     * @param workflow the workflow to associate with this project
     */
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    /**
     * Checks equality based on project name.
     *
     * @param o the object to compare
     * @return true if the names are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(name, project.name);
    }

    /**
     * Returns the hash code for this project, based on its name.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}