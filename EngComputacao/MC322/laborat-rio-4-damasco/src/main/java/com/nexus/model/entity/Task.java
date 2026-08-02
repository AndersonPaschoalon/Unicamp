package com.nexus.model.entity;

import java.time.LocalDate;
import java.util.Objects;

import com.nexus.model.Entity;

/**
 * Represents a task in a project management system.
 * 
 * <p>
 * A Task encapsulates all the essential information needed to track work items,
 * including a unique identifier, title, deadline, estimated effort, current
 * status,
 * and assigned owner. Each task is automatically assigned a unique ID upon
 * creation.
 * </p>
 * 
 * <p>
 * Tasks are immutable in terms of their core properties (ID, title, deadline,
 * estimated effort) once created, but their status and owner can be modified as
 * the task progresses through its lifecycle.
 * </p>
 * 
 * <p>
 * The class implements proper equals() and hashCode() methods based on the
 * task's title, deadline, and estimated effort to ensure correct behavior in
 * collections.
 * </p>
 */
public class Task implements Entity {
    private final String id;
    private final String title;
    private final LocalDate deadline;
    private final long estimatedEffort;
    private final String description;
    private final LocalDate dueDate;
    private final String projectId;
    private final String ownerId;
    private final String statusId;

    /**
     * Constructs a new Task with the specified title, deadline, and estimated
     * effort.
     * The task is automatically assigned a unique ID and initially has no status or
     * owner.
     *
     * @param title           the title or name of the task
     * @param deadline        the deadline by which the task should be completed
     * @param estimatedEffort the estimated effort required to complete the task (in
     *                        hours)
     */
    // public Task(String title, LocalDate deadline, long estimatedEffort) {
    // this.id = generateReadableId(title);
    // this.deadline = deadline;
    /// this.title = title;
    // this.estimatedEffort = estimatedEffort;
    // }

    public Task(String title, LocalDate deadline, long estimatedEffort, String projectId) {
        this(title, deadline, estimatedEffort, projectId, null, null, null, null);
    }

    public Task(String title, LocalDate deadline, long estimatedEffort, String projectId,
            String description, LocalDate dueDate, String assignedUserId, String statusId) {
        this(Task.generateReadableId(title, deadline, estimatedEffort, projectId), title, deadline, estimatedEffort,
                projectId,
                description, dueDate, assignedUserId, statusId, true);
    }

    private Task(String id, String title, LocalDate deadline, long estimatedEffort,
            String projectId, String description, LocalDate dueDate,
            String ownerId, String statusId, boolean trusted) {
        this.id = id;
        this.title = title;
        this.deadline = deadline;
        this.estimatedEffort = estimatedEffort;
        this.projectId = projectId;
        this.description = description;
        this.dueDate = dueDate;
        this.ownerId = ownerId;
        this.statusId = statusId;
    }

    /**
     * Generates a readable identifier using a normalized title prefix and a short
     * hash suffix.
     *
     * @param title the task title used to build the identifier prefix
     * @return an uppercase readable identifier in the format {@code PREFIX-HASH}
     */
    private static String generateReadableIdOld(String title) {
        String titlePart = title.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .substring(0, Math.min(title.length(), 8));

        String hashPart = Integer.toHexString(title.hashCode()).substring(0, 4);

        return (titlePart + "-" + hashPart).toUpperCase();
    }

    /**
     * Generates a readable identifier using a normalized title prefix and a short
     * hash suffix.
     *
     * @param title the task title used to build the identifier prefix
     * @return an uppercase readable identifier in the format {@code PREFIX-HASH}
     */
    private static String generateReadableIdHash(String title, LocalDate deadline, long estimatedEffort,
            String projectId) {
        String titlePart = title.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .substring(0, Math.min(title.length(), 8));
        String seed = title + "|" + deadline + "|" + estimatedEffort + "|" + projectId;
        String hashPart = Integer.toHexString(seed.hashCode()).substring(0, 4);
        return (titlePart + "-" + hashPart).toUpperCase();
    }

    /**
     * Generates a readable identifier using a normalized title prefix and a short
     * hash suffix.
     *
     * @param title the task title used to build the identifier prefix
     * @return an uppercase readable identifier in the format {@code PREFIX-HASH}
     */
    private static String generateReadableId(String title, LocalDate deadline, long estimatedEffort, String projectId) {
        String titlePart = title.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("^-+|-+$", "");
        titlePart = titlePart.substring(0, Math.min(titlePart.length(), 8));

        String suffixSeed = deadline + "-" + estimatedEffort + "-" + projectId;
        String suffixPart = suffixSeed.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("^-+|-+$", "");

        return (titlePart + "-" + suffixPart).toUpperCase();
    }

    public Task withStatusId(String statusId) {
        return new Task(this.id, this.title, this.deadline, this.estimatedEffort,
                this.projectId, this.description, this.dueDate,
                this.ownerId, statusId, true);
    }

    public Task withOwnerId(String ownerId) {
        return new Task(this.id, this.title, this.deadline, this.estimatedEffort,
                this.projectId, this.description, this.dueDate,
                ownerId, this.statusId, true);
    }

    /**
     * Gets the unique identifier of this task.
     *
     * @return the task ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets task's project ID.
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Gets the current status ID of this task.
     *
     * @return the current {@link Status} of the task, or null if no status is set
     */
    public String getStatusId() {
        return this.statusId;
    }

    /**
     * Gets the title of this task.
     *
     * @return the task title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the deadline for this task.
     *
     * @return the task deadline as a {@link LocalDate}
     */
    public LocalDate getDeadline() {
        return deadline;
    }

    public LocalDate getDueDate() {
        return deadline;
    }

    /**
     * Gets the owner ID of this task.
     *
     * @return the {@link User} who owns this task, or null if no owner is assigned
     */
    public String getOwnerId() {
        return this.ownerId;
    }

    /**
     * Gets the estimated effort required to complete this task.
     *
     * @return the estimated effort in hours
     */
    public long getEstimatedEffort() {
        return estimatedEffort;
    }

    /**
     * Compares this task with another object for equality.
     * Two tasks are considered equal if they have the same title, deadline, and
     * estimated effort.
     *
     * @param o the object to compare with this task
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return Objects.equals(id, ((Task) o).id);
    }

    /**
     * Returns the hash code for this task.
     * The hash code is computed based on the task's title, deadline, and estimated
     * effort.
     *
     * @return the hash code value for this task
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}