package com.nexus.view;

import org.jline.terminal.Terminal;

public class NexusCLIMenus {
    private final Terminal terminal;

    NexusCLIMenus(Terminal terminal) {
        this.terminal = terminal;
    }

    public void printWelcome() {
        this.terminal.writer().println("========================================");
        this.terminal.writer().println("    NEXUS OS - CLI INTERATIVA v2.0    ");
        this.terminal.writer().println("========================================");
        this.terminal.writer().println("Digite 'HELP' para ver o manual de comandos (Tab p/ sugestões).");
    }

    public void printManual() {

        this.terminal.writer().println("\n==================================================");
        this.terminal.writer().println("              NEXUS - MANUAL DE COMANDOS");
        this.terminal.writer().println("==================================================");

        this.terminal.writer().println("\n[ CRIAÇÃO DE ENTIDADES ]");
        this.terminal.writer().println("CREATE_USER");
        this.terminal.writer().println("  -> Cria um novo usuário.");
        this.terminal.writer().println("  Sintaxe:");
        this.terminal.writer().println("    CREATE_USER;username;email");

        this.terminal.writer().println("\nCREATE_PROJECT");
        this.terminal.writer().println("  -> Cria um novo projeto.");
        this.terminal.writer().println("  Sintaxe:");
        this.terminal.writer().println("    CREATE_PROJECT;projectName");

        this.terminal.writer().println("\nCREATE_STATUS");
        this.terminal.writer().println("  -> Cria um novo status.");
        this.terminal.writer().println("  Sintaxe:");
        this.terminal.writer().println("    CREATE_STATUS;statusName;isTerminal");

        this.terminal.writer().println("\nCREATE_WORKFLOW");
        this.terminal.writer().println("  -> Cria um workflow vazio.");
        this.terminal.writer().println("  Sintaxe:");
        this.terminal.writer().println("    CREATE_WORKFLOW;workflowName");

        this.terminal.writer().println("\nCREATE_TASK");
        this.terminal.writer().println("  -> Cria uma nova tarefa vinculada a um projeto.");
        this.terminal.writer().println("  Sintaxe:");
        this.terminal.writer().println("    CREATE_TASK;taskName;deadline;effort;projectName");

        this.terminal.writer().println("\n--------------------------------------------------");

        this.terminal.writer().println("\n[ CONFIGURAÇÃO DE WORKFLOW ]");

        this.terminal.writer().println("\nSET_INITIAL_STATUS");
        this.terminal.writer().println("  -> Define o status inicial automático do workflow.");
        this.terminal.writer().println("  Sintaxe:");
        this.terminal.writer().println("    SET_INITIAL_STATUS;workflowName;statusName");

        this.terminal.writer().println("\nALLOW_TRANSITION");
        this.terminal.writer().println("  -> Permite uma transição entre estados.");
        this.terminal.writer().println("  Sintaxe:");
        this.terminal.writer().println("    ALLOW_TRANSITION;workflowName;statusOrigem;statusDestino");

        this.terminal.writer().println("\nLINK_WORKFLOW");
        this.terminal.writer().println("  -> Associa um workflow a um projeto.");
        this.terminal.writer().println("  Sintaxe:");
        this.terminal.writer().println("    LINK_WORKFLOW;projectName;workflowName");

        this.terminal.writer().println("\n--------------------------------------------------");

        this.terminal.writer().println("\n[ GERENCIAMENTO DE TAREFAS ]");

        this.terminal.writer().println("\nASSIGN_USER");
        this.terminal.writer().println("  -> Define o responsável por uma tarefa.");
        this.terminal.writer().println("  Sintaxe:");
        this.terminal.writer().println("    ASSIGN_USER;taskId;username");

        this.terminal.writer().println("\nADD_MEMBER");
        this.terminal.writer().println("  -> Adiciona um usuário ao projeto com uma role.");
        this.terminal.writer().println("  Sintaxe:");
        this.terminal.writer().println("    ADD_MEMBER;projectName;username;ROLE");

        this.terminal.writer().println("\nMOVE_TASK");
        this.terminal.writer().println("  -> Move uma tarefa entre status.");
        this.terminal.writer().println("  Sintaxe:");
        this.terminal.writer().println("    MOVE_TASK;taskId;newStatusName;usernameAtuante");

        this.terminal.writer().println("\n--------------------------------------------------");

        this.terminal.writer().println("\n[ CONSULTAS ]");

        this.terminal.writer().println("\nLIST_PROJECTS");
        this.terminal.writer().println("  -> Lista todos os projetos.");

        this.terminal.writer().println("\nLIST_TASKS");
        this.terminal.writer().println("  -> Lista todas as tarefas agrupadas por projeto.");

        this.terminal.writer().println("\nLIST_STATUS");
        this.terminal.writer().println("  -> Lista todos os status cadastrados.");

        this.terminal.writer().println("\nLIST_WORKFLOWS");
        this.terminal.writer().println("  -> Lista todos os workflows cadastrados.");

        this.terminal.writer().println("\nLIST_USERS");
        this.terminal.writer().println("  -> Lista usuários e suas roles por projeto.");

        this.terminal.writer().println("\n--------------------------------------------------");

        this.terminal.writer().println("\n[ RELATÓRIOS ]");

        this.terminal.writer().println("\nGENERATE_REPORT");
        this.terminal.writer().println("  -> Abre o menu interativo de relatórios.");
        this.terminal.writer().println("  Relatórios disponíveis:");
        this.terminal.writer().println("     - REPORT_STATUS");
        this.terminal.writer().println("     - TOP_PERFORMERS");
        this.terminal.writer().println("     - OVERLOADED_USERS");
        this.terminal.writer().println("     - PROJECT_HEALTH");
        this.terminal.writer().println("     - GLOBAL_BOTTLENECK");

        this.terminal.writer().println("\n--------------------------------------------------");

        this.terminal.writer().println("\n[ SISTEMA ]");

        this.terminal.writer().println("\nHELP");
        this.terminal.writer().println("  -> Exibe este manual.");

        this.terminal.writer().println("\nEXIT");
        this.terminal.writer().println("  -> Encerra a aplicação.");

        this.terminal.writer().println("\n==================================================");
        this.terminal.writer().println("Você também pode utilizar comandos diretamente");
        this.terminal.writer().println("no formato sem interação:");
        this.terminal.writer().println();
        this.terminal.writer().println("  CREATE_PROJECT;MeuProjeto");
        this.terminal.writer().println("  CREATE_STATUS;DONE;true");
        this.terminal.writer().println("  MOVE_TASK;TASK-123;DONE;alice");
        this.terminal.writer().println("==================================================\n");

        this.terminal.flush();
    }

}
