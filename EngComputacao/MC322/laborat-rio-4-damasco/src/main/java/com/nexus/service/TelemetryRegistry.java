package com.nexus.service;

/**
 * Registry for telemetry metrics such as task creation, validation errors, and active workload.
 */
public class TelemetryRegistry {
    private static final TelemetryRegistry instance = new TelemetryRegistry();
    
    private int totalTasksCreated = 0;
    private int totalValidationErrors = 0;
    private int activeWorkload = 0;

    /**
     * Creates the singleton telemetry registry instance.
     */
    private TelemetryRegistry() {
    }

    /**
     * Returns the singleton telemetry registry instance.
     *
     * @return the global telemetry registry
     */
    public static TelemetryRegistry getInstance() {
        return instance;
    }

    /**
     * Increments the count of total tasks created.
     */
    public void incrementTasksCreated() {
        totalTasksCreated++;
    }

    /**
     * Increments the count of total validation errors.
     */
    public void incrementValidationErrors() {
        totalValidationErrors++;
    }

    /**
     * Increments the count of active workload (tasks in progress).
     */
    public void incrementActiveWorkload() {
        activeWorkload++;
    }

    /**
     * Decrements the count of active workload (tasks in progress).
     */
    public void decrementActiveWorkload() {
        activeWorkload--;
    }

    /**
     * Initialises telemetry counters from previously persisted state.
     *
     * @param tasksCreated  the total number of tasks that already exist
     * @param workloadActive the number of tasks currently in progress
     */
    public void hydrate(int tasksCreated, int workloadActive) {
        this.totalTasksCreated = tasksCreated;
        this.activeWorkload = workloadActive;
    }

    /**
     * Gets the total number of tasks created.
     *
     * @return the total number of tasks created
     */
    public int getTotalTasksCreated() {
        return totalTasksCreated;
    }

    /**
     * Gets the total number of validation errors.
     *
     * @return the total number of validation errors
     */
    public int getTotalValidationErrors() {
        return totalValidationErrors;
    }

    /**
     * Gets the current active workload (tasks in progress).
     *
     * @return the number of tasks in progress
     */
    public int getActiveWorkload() {
        return activeWorkload;
    }
}
