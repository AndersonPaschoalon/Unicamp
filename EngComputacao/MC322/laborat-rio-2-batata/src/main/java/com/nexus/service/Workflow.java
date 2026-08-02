package com.nexus.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.nexus.exception.NexusValidationException;
import com.nexus.model.Status;


public class Workflow {
    private final String name;
    private Status initialStatus;

    private final Map<Status, Set<Status>> allowedTransitions = new HashMap<>();

    public Workflow(String name) {
        this.name = name;
    }

    /**
     * Marca qual o status inicial das futuras tarefas
     * 
     * @param status {@link Status} com o status
     */
    public void setInitialStatus(Status status) {
        this.initialStatus = status;
    }

    /**
     * Recebe o status inicial
     * 
     * @return o {@link Status} inicial
     */
    public Status getInitialStatus() {
        return this.initialStatus;
    }

    /**
     * Retorna o nome do workflow
     * 
     * @return um {@link String} com o nome do workflow
     */
    public String getName() {
        return this.name;
    }

    /**
     * Adiciona uma nova transição possivel de um determinado status from para um status to. Caso o
     * status from ainda não esteja cadastrado, ele é inserido automaticamente.
     * 
     * @param from
     * @param to
     * @throws IllegalArgumentException: caso algum dos status de entrada sejam nulos.
     */
    public void addTransition(Status from, Status to) {
        if (from == null && to == null) {
            throw new IllegalArgumentException("Status de origem e destino não podem ser nulos.");
        } else if (from == null) {
            throw new IllegalArgumentException("Status de origem não pode ser nulo.");
        } else if (to == null) {
            throw new IllegalArgumentException("Status de destino não pode ser nulo.");
        }

        if (from.isTerminal()) {
            throw new NexusValidationException("Status de inicio e um Status Terminal");
        }

        if (!this.allowedTransitions.containsKey(from)) {
            Set<Status> newDestinations = new HashSet<>();
            this.allowedTransitions.put(from, newDestinations);
        }

        Set<Status> currentDsts = this.allowedTransitions.get(from);
        currentDsts.add(to);
    }

    /**
     * Verifica se um Status pode transicionar para outro
     * 
     * @param from {@link Status} de origem
     * @param to {@link Status} de chegada
     * @return um {@link Boolean} true se for possivel ou False se nao
     */
    public boolean canTransition(Status from, Status to) {
        Set<Status> allowerdDsts = this.allowedTransitions.get(from);

        if (allowerdDsts == null) {
            return false;
        }

        boolean isAllowed = allowerdDsts.contains(to);
        return isAllowed;
    }

    /**
     * Verifica se o Status esta presente no workflow
     *
     * @param statusString a {@link String} do status para encontrar o {@link Status} atribuido
     * 
     * @throws NexusValidationException: se o Status nao existir no workflow
     **/
    public Status getStatus(String statusString) {
        for (Status status : this.allowedTransitions.keySet()) {
            if (status.getName().equals(statusString)) {
                return status;
            }
        }
        throw new NexusValidationException(
                "status " + statusString + " nao existente no workflow " + this.name);


    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Workflow w = (Workflow) o;
        boolean namesAreEqual = this.name.equals(w.getName());
        return namesAreEqual;
    }

    @Override
    public int hashCode() {
        int hash = Objects.hashCode(this.name);
        return hash;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Workflow: ").append(name).append("\n");
        sb.append("Initial Status: ")
                .append(initialStatus != null ? initialStatus.getName() : "none").append("\n");
        sb.append("\nTransitions:\n");

        if (allowedTransitions.isEmpty()) {
            sb.append("  (no transitions defined)");
        } else {
            for (Map.Entry<Status, Set<Status>> entry : allowedTransitions.entrySet()) {
                Status from = entry.getKey();
                Set<Status> destinations = entry.getValue();

                if (destinations.isEmpty()) {
                    sb.append("  ").append(from.getName()).append(" -> (no destinations)\n");
                } else {
                    sb.append("  ").append(from.getName()).append(" -> ");
                    List<String> destNames = destinations.stream().map(Status::getName).sorted()
                            .collect(Collectors.toList());
                    sb.append(String.join(", ", destNames));
                    sb.append("\n");
                }
            }
        }

        return sb.toString();
    }


    /**
     * Uma funcao que encontra um workflow pelo nome
     * 
     * @param workflowList {@link List} de {@link Workflow}
     * @param workflowName {@link String} com o nome do workflow
     * @return o {@link Workflow} desejado
     * @throws NexusValidationException se nao encontrar o {@link Workflow}
     */
    public static Workflow getWorkflowByName(List<Workflow> workflowList, String workflowName) {
        return workflowList.stream().filter(u -> u.getName().equals(workflowName)).findFirst()
                .orElseThrow(() -> new NexusValidationException(
                        "Workflow '" + workflowName + "' não encontrado."));
    }

}
