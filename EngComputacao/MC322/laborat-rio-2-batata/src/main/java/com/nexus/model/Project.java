/**
 * Represents a project that manages tasks within a defined budget.
 * 
 * This class allows creating a project with a name and total budget, and manages a collection of
 * tasks. It ensures that the total estimated effort of all tasks does not exceed the project's
 * total budget.
 * 
 * When adding a new task, the class validates that the sum of all task efforts does not surpass the
 * budget limit. If it would, a NexusValidationException is thrown.
 */
package com.nexus.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.nexus.exception.NexusValidationException;
import com.nexus.service.Workflow;

public class Project {

    private String name;
    private long totalBudget;
    private Workflow workflow = null;
    private final Map<User, User.Role> teamMembers = new HashMap<>();
    private final Set<Task> tasks = new HashSet<Task>();

    /**
     * Constructor.
     * 
     * @param name
     * @param totalBudget
     */
    public Project(String name, long totalBudget) {
        this.name = name;
        this.totalBudget = totalBudget;
    }

    /**
     * Calcula o orçamento total já alocado em tarefas. * @return a soma do esforço estimado de
     * todas as tarefas.
     */
    public long getAllocatedBudget() {
        return this.tasks.stream().mapToLong(Task::getEstimatedEffort).sum();
    }

    /**
     * Calcula o orçamento restante disponível para novas tarefas. * @return a diferença entre o
     * orçamento total e o alocado.
     */
    public long remainingBudget() {
        return this.totalBudget - this.getAllocatedBudget();
    }

    /**
     * Adiciona uma tarefa ao projeto se houver orçamento disponível. * @param task a tarefa a ser
     * adicionada.
     * 
     * @throws NexusValidationException se o esforço da tarefa exceder o orçamento restante.
     */
    public void addTask(Task task) throws NexusValidationException {
        if (task.getEstimatedEffort() > this.remainingBudget()) {
            throw new NexusValidationException(
                    "Orçamento insuficiente. Disponível: " + this.remainingBudget());
        }
        this.tasks.add(task);
    }

    /**
     * Pega o nome do projeto
     *
     * @return to nome do projeto
     */
    public String getName() {
        return this.name;
    }

    /**
     * Recebe uma Lista das tarefas
     *
     * @return uma {@link List} de {@link Task} que o projeto contem
     */

    public List<Task> getTasks() {
        List<Task> taskList = new ArrayList<>(this.tasks);
        return Collections.unmodifiableList(taskList);
    }

    /**
     * Retorna o valor do Budget atual do Projeto
     *
     * @return um {@link Long} do budget atual
     */
    public long getTotalBudget() {
        return totalBudget;
    }

    /**
     * adiciona um membro ao Projeto, junto com sua funcao
     *
     * @param usuario o {@link User} que sera adicionario
     * @param cargo o {@link User.Role} que sera o cargo do usuario
     * @throws NexusValidationException se o usuario ja foi adicionado no Projeto
     */

    public void addMember(User usuario, User.Role cargo) {
        if (this.teamMembers.containsKey(usuario)) {

            throw new NexusValidationException("usuario ja adicionado");
        }
        this.teamMembers.put(usuario, cargo);
    }

    /**
     * obtem o status inicial dado pelo {@link Workflow} do projeto
     * 
     * 
     * 
     * 
     * @return um {@link Status} atribuido ao inicializador de uma tarefa nova
     */
    public Status getInnitStatus() {
        return this.workflow.getInitialStatus();

    }


    /**
     * Apenas verifica no hashmap do teamMembers pra verificar se o usuario esta nele e se tem
     * permicao
     * 
     * @param usuario o {@link User} a ser verificado
     * @return um boolean de verdade se for valido
     */

    public Boolean usuarioValido(User usuario) {
        User.Role cargo = this.teamMembers.get(usuario);
        if (cargo == null || cargo == User.Role.VIEWER) {
            return false;
        }
        return true;
    }

    /**
     * Faz a mudanca do status de uma tarefa
     *
     * @param Tarefa a {@link Task} que sera analizada
     * @param novoStatusString que e a versao {@link String} do Status destinatario
     * 
     * @throws NexusValidationException: caso nao e uma transicao valida
     **/

    public void change_status(Task tarefa, Status novoStatus) {
        boolean ret = this.workflow.canTransition(tarefa.getStatus(), novoStatus);
        if (!ret) {
            throw new NexusValidationException("Transição de status " + tarefa.getStatus().getName()
                    + " -> " + novoStatus.getName() + " nao e valido");
        }
        tarefa.setStatus(novoStatus);
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Project(");
        sb.append("name='").append(this.name).append('\'');
        sb.append(", totalBudget=").append(this.totalBudget);
        sb.append(", tasksSize=").append(this.tasks.size());
        sb.append(')');

        String str = sb.toString();
        return str;
    }

    @Override
    public boolean equals(Object o) {
        // Aqui consideramos que dois projetos não podem ter o mesmo nome durante a criação.
        // Ou seja, se os nomes são iguais, os projetos são iguais, não sendo necessárias
        // comparações adcionais.
        if (this == o) {
            return true;
        }

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Project p = (Project) o;

        if (this.name != null) {
            if (!this.name.equals(p.name)) {
                return false;
            }
        } else {
            if (p.name != null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        int prime = 31;
        int nameHash = (this.name != null ? this.name.hashCode() : 0);
        result = prime * result + nameHash;
        return result;
    }

    /**
     * Atribui um workflow ao projeto
     * 
     * 
     * @param workflow um {@link Workflow} que foi escolhido
     */

    public void atribuirWorkflow(Workflow workflow) {
        if (this.workflow != null) {
            throw new NexusValidationException(
                    "O projeto " + this.name + " ja contem um workflow atribuido a ele");
        }
        this.workflow = workflow;
    }

    /**
     * Usado para veriuficar se o projeto tem um workflow atribuido
     * 
     * @return True se tem um workflow atribuido, ou um False se nao
     */
    public Boolean temWorkflow() {
        if (this.workflow == null) {
            return false;
        }
        return true;
    }
}
