package com.nexus.model.entity;

import java.util.Objects;

import com.nexus.model.Entity;

/**
 * Represents a status that can be assigned to tasks in a project management
 * system.
 * 
 * <p>
 * A Status defines a particular state in the task lifecycle, such as "TO_DO",
 * "IN_PROGRESS", "BLOCKED", or "DONE". Each status has a name and can be marked
 * as terminal, indicating that tasks with this status have reached a final
 * state.
 * </p>
 * 
 * <p>
 * Terminal statuses typically represent completed or cancelled tasks that
 * should not transition to other states.
 * </p>
 */
public final class Status implements Entity {
    private String name;
    private boolean isTerminal;

    /**
     * Constructs a Status with the specified name and terminal flag.
     *
     * @param name       the name of the status (e.g., "TO_DO", "IN_PROGRESS",
     *                   "DONE")
     * @param isTerminal true if this status represents a final state, false
     *                   otherwise
     */
    public Status(String name, boolean isTerminal) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome do status não pode ser vazio.");
        }
        this.name = name;
        this.isTerminal = isTerminal;
    }

    /**
     * Retorna o identificador desta entidade para fins de persistência.
     * Corresponde ao nome do status (não há id separado).
     *
     * @return o nome do status
     */
    @Override
    public String getId() {
        return name;
    }

    /**
     * Gets the name of this status.
     *
     * @return the status name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if this status is terminal (represents a final state).
     * 
     * <p>
     * Terminal statuses typically represent completed or cancelled tasks
     * that should not transition to other states. Tasks with terminal statuses
     * are considered to have reached the end of their lifecycle.
     * </p>
     *
     * @return true if this status represents a final state, false otherwise
     */
    public boolean isTerminal() {
        return isTerminal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Status status = (Status) o;
        return this.isTerminal == status.isTerminal && Objects.equals(name, status.name);
    }

    /**
     * Retorna o hash code baseado no nome e na flag terminal.
     *
     * @return o hash code do status
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.isTerminal);
    }

    /**
     * Retorna uma representação textual do status para depuração e logs.
     *
     * @return o nome do status, sinalizando se é terminal
     */
    @Override
    public String toString() {
        return this.name + (this.isTerminal ? " (terminal)" : "");
    }

}