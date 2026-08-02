package com.nexus.model;

import java.time.LocalDate;


/**
 * Representa uma unidade de trabalho dentro de um projeto.
 */
public class Task {
    private int id;
    private LocalDate deadline; // Imutável após o nascimento
    private String title;
    private Status status;
    private User owner;
    private long estimatedEffort;
    private Project projetoPertente;

    /**
     * Cria uma nova tarefa e registra sua criação na telemetria.
     */
    public Task(String title, LocalDate deadline, long estimatedEffort, TelemetryRegistry reg,
            Project projeto, Status iniStatus) {
        if (reg == null) {
            throw new IllegalArgumentException("TelemetryRegistry não pode ser null.");
        }
        this.deadline = deadline;
        this.title = title;
        this.estimatedEffort = estimatedEffort;
        this.status = iniStatus;
        this.projetoPertente = projeto;
        this.id = reg.getID();
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
     * Troca o status da tarefa
     *
     * @param statusNovo o {@link Status} a ser settado
     */
    public void setStatus(Status statusNovo) {
        this.status = statusNovo;
    }

    // Getters

    /**
     * ID da tarefa.
     */
    public int getId() {
        return id;
    }

    /**
     * Status atual no fluxo de trabalho.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Título descritivo da tarefa.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Prazo final para conclusão.
     */
    public LocalDate getDeadline() {
        return deadline;
    }

    /**
     * Usuário atribuído à tarefa.
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Esforço estimado em horas.
     */
    public long getEstimatedEffort() {
        return estimatedEffort;
    }

    /**
     * Projeto ao qual esta tarefa pertence.
     */
    public Project getProjeto() {
        return projetoPertente;
    }

    @Override
    public String toString() {
        return "Task(id=" + this.id + ", title='" + this.title + "', status=" + this.status + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Task t = (Task) o;
        if (this.id != t.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + this.id;
        return result;
    }

}
