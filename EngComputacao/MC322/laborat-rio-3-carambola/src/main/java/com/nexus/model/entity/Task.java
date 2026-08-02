package com.nexus.model.entity;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

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
public class Task {
    private final String id;
    private LocalDate deadline; // Imutável após o nascimento
    private String title;
    private Status status;
    private User owner;
    private long estimatedEffort;

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
    public Task(String title, LocalDate deadline, long estimatedEffort) {
        this.id = generateReadableId(title);
        this.deadline = deadline;
        this.title = title;
        this.estimatedEffort = estimatedEffort;
    }

    private String generateReadableId(String title) {

        String normalized = title.toUpperCase()
                .replaceAll("[^A-Z0-9]", "");

        String prefix = normalized.substring(
                0,
                Math.min(normalized.length(), 8));

        String uuidPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return prefix + "-" + uuidPart;
    }

    /**
     * Sets the owner of this task.
     *
     * @param owner the {@link User} to set as the owner of this task
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     * Sets the status of this task.
     *
     * @param status the new {@link Status} for this task
     */
    public void setStatus(Status status) {
        this.status = status;
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
     * Gets the current status of this task.
     *
     * @return the current {@link Status} of the task, or null if no status is set
     */
    public Status getStatus() {
        return status;
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

    /**
     * Gets the owner of this task.
     *
     * @return the {@link User} who owns this task, or null if no owner is assigned
     */
    public User getOwner() {
        return owner;
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
        Task task = (Task) o;
        return estimatedEffort == task.estimatedEffort &&
                Objects.equals(title, task.title) &&
                Objects.equals(deadline, task.deadline);
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
        return Objects.hash(title, deadline, estimatedEffort);
    }
}