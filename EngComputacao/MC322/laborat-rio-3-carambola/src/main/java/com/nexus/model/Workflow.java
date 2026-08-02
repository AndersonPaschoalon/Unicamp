package com.nexus.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.nexus.model.entity.Status;

/**
 * Represents a workflow that defines allowed status transitions for tasks in a
 * project.
 * 
 * <p>
 * A Workflow manages the rules for how tasks can move between different
 * statuses.
 * It maintains a mapping of allowed transitions, where each status can have a
 * set of
 * valid target statuses that tasks can transition to.
 * </p>
 * 
 * <p>
 * This class is used to enforce business rules about task progression, ensuring
 * that tasks follow a predefined path through their lifecycle (e.g., from
 * "TO_DO"
 * to "IN_PROGRESS" to "DONE", but not directly from "TO_DO" to "DONE").
 * </p>
 * 
 * <p>
 * The workflow also respects terminal statuses - tasks cannot transition from
 * a terminal status to any other status.
 * </p>
 */
public class Workflow {
    private String name;
    private final Map<Status, Set<Status>> allowedTransitions = new HashMap<>();
    private Status initialStatus;

    /**
     * Constructs a new Workflow with the specified name.
     * The workflow starts with no allowed transitions defined.
     *
     * @param name the name of the workflow
     */
    public Workflow(String name) {
        this.name = name;
    }

    /**
     * Gets the name of this workflow.
     *
     * @return the workflow name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the initial status for this workflow.
     * 
     * <p>
     * The initial status is the default status assigned to new tasks
     * when they are created within a project that uses this workflow.
     * If no initial status is set, tasks cannot be created in the project.
     * </p>
     *
     * @return the initial status for new tasks, or null if no initial status is set
     */
    public Status getInitialStatus() {
        return initialStatus;
    }

    /**
     * Sets the initial status for this workflow.
     * 
     * <p>
     * The initial status determines what status new tasks will be assigned
     * when they are created within a project that uses this workflow.
     * This must be set before tasks can be created in the project.
     * </p>
     *
     * @param initialStatus the status to assign to new tasks in this workflow
     */
    public void setInitialStatus(Status initialStatus) {
        this.initialStatus = initialStatus;
    }

    /**
     * Gets the map of allowed transitions for this workflow.
     * 
     * <p>
     * The returned map contains the internal representation of allowed transitions,
     * where each key is a source status and the corresponding value is a set of
     * target statuses that can be transitioned to from that source.
     * </p>
     *
     * @return the map of allowed transitions from source status to target statuses
     */
    public Map<Status, Set<Status>> getAllowedTransitions() {
        return allowedTransitions;
    }

    /**
     * Adds an allowed transition from one status to another in this workflow.
     * 
     * <p>
     * If the source status doesn't exist in the transitions map, a new entry
     * is created. If it already exists, the target status is added to the existing
     * set of allowed transitions.
     * </p>
     *
     * @param from the source status to transition from
     * @param to   the target status to transition to
     */
    public void addTransition(Status from, Status to) {
        allowedTransitions.computeIfAbsent(from, k -> new HashSet<>()).add(to);
    }

    /**
     * Checks if a transition from one status to another is allowed by this
     * workflow.
     * 
     * <p>
     * Returns false if:
     * </p>
     * <ul>
     * <li>The source status has no defined transitions</li>
     * <li>The source status is terminal (terminal statuses cannot transition to
     * other states)</li>
     * <li>The target status is not in the list of allowed transitions from the
     * source status</li>
     * </ul>
     *
     * @param from the source status to transition from
     * @param to   the target status to transition to
     * @return true if the transition is allowed, false otherwise
     */
    public boolean canTransition(Status from, Status to) {
        if (!allowedTransitions.containsKey(from) || from.isTerminal() == true) {
            return false;
        }
        return allowedTransitions.get(from).contains(to);
    }

    public Set<Status> getAllowedTransitionsFrom(Status from) {
        return Collections.unmodifiableSet(
                allowedTransitions.getOrDefault(from, Collections.emptySet()));
    }

    public Set<Status> getAllStatuses() {
        Set<Status> statuses = new HashSet<>();
        allowedTransitions.forEach((from, toSet) -> {
            statuses.add(from);
            statuses.addAll(toSet);
        });
        if (initialStatus != null) {
            statuses.add(initialStatus);
        }
        return Collections.unmodifiableSet(statuses);
    }

}