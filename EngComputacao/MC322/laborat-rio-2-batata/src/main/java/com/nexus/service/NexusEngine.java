package com.nexus.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import com.nexus.exception.NexusValidationException;
import com.nexus.model.Project;
import com.nexus.model.Status;
import com.nexus.model.Task;
import com.nexus.model.TelemetryRegistry;
import com.nexus.model.User;


/**
 * Recebe a instrução e orquestra as chamadas aos serviços corretos. Esta classe possui a
 * responsabilidade de: - Receber um comando sintaticamente valido. - Orquestrar a chamada dos
 * serviços corretos.
 */
public class NexusEngine {
    private Workspace workspace;
    private TelemetryRegistry registro;

    /**
     * Engine contructor.
     * 
     * @param workspace workspace
     * @param reg telemetry register
     */
    public NexusEngine(Workspace workspace, TelemetryRegistry reg) {
        this.workspace = workspace;
        this.registro = reg;
    }

    /**
     * Create a user.
     * 
     * @param nome user name
     * @param email user email
     * @throws IllegalArgumentException if user or its username is null
     */
    public void create_user(String nome, String email) {
        User novoUsuario = new User(nome, email);
        this.workspace.addUser(novoUsuario);
        System.out.println("[LOG] Usuário criado: " + nome);
    }


    /**
     * Creates a project.
     * 
     * @param nome Project name. Must be unique (case insensitive).
     * @param totalBudgetString Total |Budget (string format)
     * @throws NumberFormatException if the provided string it cannot be converted to an integer
     *         value.
     * @throws IllegalArgumentException if the project name is null or empty
     */
    public void create_project(String nome, String totalBudgetString) {
        Project project = new Project(nome.trim(), Long.parseLong(totalBudgetString));
        this.workspace.addProject(project);
        System.out.println("[LOG] Projeto criado: " + nome);
    }

    /**
     * Creates a Task.
     * 
     * @param nomeTask
     * @param dataString
     * @param estEffortString
     * @param nomeProjeto
     * @throws NexusValidationException Can return this exeption if (1) no project was found with
     *         the name nomeProjeto. (2) if Project does not have any workflow.
     * @throws NumberFormatException throws estEffortString could not be converted to integer
     * @throws DateTimeParseException if dataString could not be converted to LocalDate.
     */
    public void create_task(String nomeTask, String dataString, String estEffortString,
            String nomeProjeto) {
        nomeTask = nomeTask.trim();
        nomeProjeto = nomeProjeto.trim();
        Project project = this.workspace.getProjectByName(nomeProjeto);
        if (!project.temWorkflow()) {
            throw new NexusValidationException(
                    "Projeto" + nomeProjeto + " nao tem Workflow atribuido a ele");
        }
        Status initStatus = project.getInnitStatus();
        if (initStatus == null) {
            throw new NexusValidationException(
                    "É nercessário ser definido um status inicial no Workflow do projeto para ser adicionada uma nova task.");
        }
        long taskEffort = Long.parseLong(estEffortString);
        if (project.remainingBudget() < taskEffort) {
            throw new NexusValidationException(
                    "Projeto" + nomeProjeto + " nao tem Workflow atribuido a ele");
        }
        Task t = new Task(nomeTask, LocalDate.parse(dataString), taskEffort, this.registro, project,
                initStatus);
        this.workspace.addTask(t);
        project.addTask(t);
        System.out.println("[LOG] Tarefa criada: " + nomeTask);
    }

    /**
     * Atribui um usuário como responsável por uma tarefa existente. * @param IdTarefaString ID da
     * tarefa em formato String
     * 
     * @param username Username do responsável
     * @throws NexusValidationException se a tarefa ou o usuário não forem encontrados
     */
    public void assign_user(String IdTarefaString, String username) {
        username = username.trim();
        Task task = this.workspace.getTaskById(Integer.parseInt(IdTarefaString));
        User user = this.workspace.getUserByName(username);
        task.setOwner(user);
        System.out.println(
                "[LOG] Usuário '" + username + "' atribuído à tarefa ID " + IdTarefaString);
    }

    /**
     * Orquestra a transição de status de uma tarefa, validando regras de workflow e permissões.
     * * @param IdTarefaString ID da tarefa
     * 
     * @param novoStatus Nome do status de destino
     * @param usuarioAtuante Username de quem executa a ação
     * @throws NexusValidationException se o projeto não tiver workflow, o usuário não tiver
     *         privilégios ou a transição for proibida.
     */
    public void change_status(String IdTarefaString, String novoStatusStr, String usuarioAtuante) {
        novoStatusStr = novoStatusStr.trim();
        Status novoStatus = this.workspace.getStatusByName(novoStatusStr);

        Task task = this.workspace.getTaskById(Integer.parseInt(IdTarefaString));
        Project projetoDono = task.getProjeto();
        if (!projetoDono.temWorkflow()) {
            throw new NexusValidationException(
                    "Projeto" + projetoDono + " nao tem Workflow atribuido a ele");
        }

        usuarioAtuante = usuarioAtuante.trim();
        User usuario = this.workspace.getUserByName(usuarioAtuante);
        if (!projetoDono.usuarioValido(usuario)) {
            throw new NexusValidationException("Usuario nao tem privilegios nesse projeto");
        }
        if (task.getOwner() == null) {
            throw new NexusValidationException("Tarefa não possui membro associado.");
        }

        projetoDono.change_status(task, novoStatus);
        System.out
                .println("[LOG] Tarefa ID " + IdTarefaString + " movida para status " + novoStatus);
    }

    /**
     * Aciona a geração e impressão dos relatórios analíticos globais.
     */
    public void report_status() {
        System.out.println("[LOG] Status Report:");
        this.workspace.printRelatoriosAnaliticos();
    }

    /**
     * Cria um novo objeto Status e o registra no workspace. * @param nome Nome identificador do
     * status
     * 
     * @param isTerminalString "true" se for um estado final, caso contrário "false"
     */
    public void create_status(String nome, String isTerminalString) {
        nome = nome.trim();
        Boolean isTerminal = Boolean.parseBoolean(isTerminalString.toLowerCase());
        Status s = new Status(nome, isTerminal);
        this.workspace.addStatus(s);
        System.out.println("[LOG] Status " + nome + " criado");
    }

    /**
     * Cria um novo Workflow (grafo de transições) no workspace. * @param nome Nome do workflow
     */
    public void create_workflow(String nome) {
        nome = nome.trim();
        Workflow workflow = new Workflow(nome);
        this.workspace.addWorkflow(workflow);
        System.out.println("[LOG] Workflow " + nome + " criado com sucesso");
    }

    /**
     * Adiciona uma transição permitida entre dois estados em um workflow específico. * @param
     * nomeWorkflow Nome do workflow alvo
     * 
     * @param nomeStatusOrigem Status de partida
     * @param nomeStatusDestino Status de chegada permitido
     */
    public void allow_transition(String nomeWorkflow, String nomeStatusOrigem,
            String nomeStatusDestino) {
        nomeWorkflow = nomeWorkflow.trim();
        nomeStatusOrigem.trim();
        nomeStatusDestino.trim();
        Workflow w = this.workspace.getWorkflowByName(nomeWorkflow);
        Status src = this.workspace.getStatusByName(nomeStatusOrigem);
        Status dst = this.workspace.getStatusByName(nomeStatusDestino);
        w.addTransition(src, dst);
        System.out.println("[LOG] Transicao de " + nomeStatusOrigem + " para " + nomeStatusDestino
                + " permitida");
    }

    /**
     * Associa um workflow de regras a um projeto. * @param projectNome Nome do projeto
     * 
     * @param workflowNome Nome do workflow a ser aplicado
     */
    public void vincular_workflow(String projectNome, String workflowNome) {
        projectNome = projectNome.trim();
        workflowNome = workflowNome.trim();
        Workflow w = this.workspace.getWorkflowByName(workflowNome);
        Project projeto = this.workspace.getProjectByName(projectNome);
        projeto.atribuirWorkflow(w);
        System.out
                .println("[LOG] Workflow " + workflowNome + " atribuido ao projeto " + projectNome);
    }

    /**
     * Adiciona um usuário à equipe de um projeto com um papel (Role) específico. * @param
     * projectName Nome do projeto
     * 
     * @param usuarioNome Nome do usuário
     * @param roleNome Nome do cargo (VIEWER, CONTRIBUTOR, MANAGER)
     */
    public void add_member(String projectName, String usuarioNome, String roleNome) {
        projectName = projectName.trim();
        usuarioNome = usuarioNome.trim();
        roleNome = roleNome.trim();
        User.Role r = User.Role.parseRole(roleNome);
        Project projeto = this.workspace.getProjectByName(projectName);
        User u = this.workspace.getUserByName(usuarioNome);
        projeto.addMember(u, r);
        System.out.println("Usuario " + usuarioNome + " adicionado ao projeto " + projectName
                + " com sucesso");
    }


    /**
     * Define qual será o status inicial de novas tarefas criadas sob um determinado workflow.
     * * @param workflowName Nome do workflow
     * 
     * @param statusName Nome do status inicial
     */
    public void set_initial_status(String workflowName, String statusName) {
        workflowName = workflowName.trim();
        statusName = statusName.trim();
        Workflow w = this.workspace.getWorkflowByName(workflowName);
        Status statusInit = this.workspace.getStatusByName(statusName);
        w.setInitialStatus(statusInit);
        System.out.println("[LOG] Status inicial " + statusName + " atribuido ao Workflow "
                + workflowName + " com sucesso");
    }



}
