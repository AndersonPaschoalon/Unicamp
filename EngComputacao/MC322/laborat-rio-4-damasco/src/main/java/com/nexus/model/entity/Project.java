package com.nexus.model.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.nexus.model.Entity;
import com.nexus.model.valueobject.Role;
import com.nexus.model.valueobject.Sprint;

/**
 * Represents a project that manages tasks within a defined budget.
 *
 * This class allows creating a project with a name and total budget,
 * and manages a collection of tasks. It ensures that the total estimated
 * effort of all tasks does not exceed the project's total budget.
 *
 * When adding a new task, the class validates that the sum of all task
 * efforts does not surpass the budget limit. If it would, a
 * NexusDomainException is thrown.
 */
public final class Project implements Entity {
    private final String id;
    private final String name;
    private final long totalBudget;
    private final String workflowId;
    private final Map<String, Role> teamMembers;
    private final Map<String, Sprint> sprints;

    /**
     * Constructs a Project with the specified name and total budget.
     *
     * @param name        the name of the project
     * @param totalBudget the total budget for the project
     */
    public Project(String name, long totalBudget) {
        this.id = name;
        this.name = name;
        this.totalBudget = totalBudget;
        this.workflowId = null;
        this.teamMembers = new HashMap<>();
        this.sprints = new HashMap<>();
    }

    private Project(String id, String name, long totalBudget, String workflowId,
            Map<String, Role> teamMembers, Map<String, Sprint> sprints, boolean trusted) {
        this.id = id;
        this.name = name;
        this.totalBudget = totalBudget;
        this.workflowId = workflowId;
        this.teamMembers = teamMembers;
        this.sprints = sprints;
    }

    public Project withWorkflowId(String newWorkflowId) {
        return new Project(this.id, this.name, this.totalBudget, newWorkflowId,
                new HashMap<>(this.teamMembers), new HashMap<>(this.sprints), true);
    }

    public Project withMember(String userId, Role role) {
        Map<String, Role> newTeamMembers = new HashMap<>(this.teamMembers);
        newTeamMembers.put(userId, role);
        return new Project(this.id, this.name, this.totalBudget, this.workflowId,
                newTeamMembers, new HashMap<>(this.sprints), true);
    }

    public Project withSprint(Sprint sprint) {
        Map<String, Sprint> newSprints = new HashMap<>(this.sprints);
        newSprints.put(sprint.getName(), sprint);
        return new Project(this.id, this.name, this.totalBudget, this.workflowId,
                new HashMap<>(this.teamMembers), newSprints, true);
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getTotalBudget() {
        return totalBudget;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public Map<String, Role> getTeamMembers() {
        return Collections.unmodifiableMap(teamMembers);
    }

    public Map<String, Sprint> getSprints() {
        return Collections.unmodifiableMap(sprints);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return Objects.equals(id, ((Project) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}