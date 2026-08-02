package com.nexus.model;

/**
 * Registry class responsible for tracking system-wide metrics and generating unique identifiers.
 */
public class TelemetryRegistry {

    /// Statics do Task
    private int totalTasksCreated = 0;
    private int totalValidationErrors = 0;
    private int activeWorkload = 0;
    private int nextId = 1;

    /**
     * Generates a unique Task ID and automatically increments the total tasks counter. * @return a
     * unique integer identifier for a new task
     */
    public int getID() {
        this.addTaskCreated();
        return this.nextId++;
    }

    /**
     * Increments the global counter for validation errors. Should be called whenever a command
     * fails due to business rules or syntax.
     */
    public void addTotalValidationErrors() {
        this.totalValidationErrors++;
    }

    /**
     * Increments the count of tasks currently being processed in the system.
     */
    public void addActiveWorkload() {
        this.activeWorkload++;
    }

    /**
     * Retrieves the total number of tasks created during the application lifecycle. * @return total
     * task count
     */
    public int getTotalTaskCreated() {
        return this.totalTasksCreated;
    }

    /**
     * Retrieves the current number of active tasks in the system. * @return current active workload
     */
    public int getActiveWorkload() {
        return this.activeWorkload;
    }

    /**
     * Retrieves the total number of validation errors encountered. * @return total validation error
     * count
     */
    public int getTotalValidationErrors() {
        return this.totalValidationErrors;
    }

    /**
     * Internal helper to increment the task creation metric.
     */
    private void addTaskCreated() {
        this.totalTasksCreated++;
    }

}
