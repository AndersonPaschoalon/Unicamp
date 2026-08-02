package com.nexus;

import static com.nexus.utils.StrFormatter.truncar;
import java.util.Scanner;
import java.util.Set;
import com.nexus.exception.NexusValidationException;
import com.nexus.model.Task;
import com.nexus.model.TelemetryRegistry;
import com.nexus.model.User;
import com.nexus.service.CommandParser;
import com.nexus.service.NexusEngine;
import com.nexus.service.Workspace;

/**
 * Ponto de entrada para a aplicação Nexus. * Esta classe fornece uma interface simples baseada em
 * console usada no trabalho de laboratório. Gerencia uma coleção de {@link User usuários} e um
 * {@link Workspace} onde as {@link Task tarefas} são armazenadas. As operações são realizadas
 * através de um laço de menu e delegadas a métodos auxiliares.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Workspace workspace = new Workspace();
    private static final TelemetryRegistry registro = new TelemetryRegistry();
    private static final CommandParser command_parser = new CommandParser(Main.registro);

    /**
     * Inicia a aplicação e processa comandos do usuário até a terminação.
     *
     * @param args argumentos de linha de comando (ignorados)
     */
    public static void main(String[] args) {
        boolean running = true;
        NexusEngine engine = new NexusEngine(Main.workspace, Main.registro);

        while (running) {
            displayMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "0" -> {
                    System.out.println("Encerrando Nexus Motor...");
                    running = false;
                }
                case "1" -> addUser(engine);

                case "2" -> addTask(engine, registro);
                case "3" -> listTasks();
                case "4" -> {
                    System.out.println(
                            "1. Carregar Log V1 (Básico)\n2. Carregar Log V2 (Desafio)\n3. Carregar Log V3\n4. Carregar Log V4\n5. Carregar Log V5");
                    String logChoice = scanner.nextLine();
                    String file = switch (logChoice) {
                        case "1" -> "log_v1.txt";
                        case "2" -> "log_v2.txt";
                        case "3" -> "log_v3.txt";
                        case "4" -> "log_v4.txt";
                        case "5" -> "log_v5.txt";
                        default -> null;
                    };
                    if (file == null) {
                        System.out.println("\n[!] Opção inválida.");
                    } else {
                        command_parser.rodar(file, engine);
                    }
                }
                default -> System.out.println("\n[!] Opção inválida.");
            }
        }
    }

    /**
     * Imprime o menu principal na saída padrão.
     * <p>
     * As opções do menu correspondem às escolhas tratadas em {@link #main(String[])}.
     * </p>
     */
    private static void displayMenu() {
        System.out.print("""

                ======= NEXUS CORE: MENU =======
                1. Adicionar Usuário
                2. Adicionar Tarefa
                3. Listar Todas as Tarefas
                4. Processar Log de Ações
                0. Sair
                Escolha uma opção:\s""");
    }

    /**
     * Solicita ao usuário nome de usuário e email, cria um novo {@link User} e adiciona-o à lista
     * interna. Exceções de validação são relatadas no fluxo de erro.
     */
    private static void addUser(NexusEngine engine) {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();

            engine.create_user(username, email);

        } catch (NexusValidationException e) {
            System.err.println("[ERRO DE REGRAS] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("[ERRO DE FORMATO] " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERRO INESPERADO] " + e.getMessage());
        }
    }

    /**
     * Coleta detalhes da tarefa do usuário, constrói uma {@link Task} e a acrescenta ao workspace.
     * Erros de parsing de data são informados no stderr.
     */
    private static void addTask(NexusEngine engine, TelemetryRegistry registry) {
        try {
            System.out.print("Título da Tarefa: ");
            String title = scanner.nextLine();
            System.out.print("Prazo (AAAA-MM-DD): ");
            String deadlineStrnString = scanner.nextLine();
            System.out.print("Esforço Estimado (em horas): ");
            String estimatedEffort = scanner.nextLine();
            System.out.print("Nome do Projeto: ");
            String projetoNome = scanner.nextLine();

            engine.create_task(title, deadlineStrnString, estimatedEffort, projetoNome);

        } catch (NexusValidationException e) {
            System.err.println("[ERRO DE REGRAS] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("[ERRO DE FORMATO] " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERRO INESPERADO] " + e.getMessage());
        }
    }

    /**
     * Exibe todas as tarefas atualmente armazenadas no {@link Workspace} em formato de tabela
     * simples. Se não existirem tarefas, imprime uma mensagem de notificação.
     */
    private static void listTasks() {
        Set<Task> tasks = workspace.getTasks();
        if (tasks.isEmpty()) {
            System.out.println("\n[!] Nenhuma tarefa no sistema.");
            return;
        }

        String header =
                "+----+----------------------+-------------+------------+-----------------+";
        System.out.println("\n" + header);
        System.out.printf("| %-2s | %-20s | %-11s | %-10s | %-15s |%n", "ID", "TÍTULO", "STATUS",
                "DEADLINE", "ESFORÇO ESTIMADO");
        System.out.println(header);

        for (Task t : tasks) {
            System.out.printf("| %-2d | %-20s | %-11s | %-10s | %-15d |%n", t.getId(),
                    truncar(t.getTitle(), 20), t.getStatus(), t.getDeadline(),
                    t.getEstimatedEffort());
        }
        System.out.println(header);
        System.out.println("Total de tarefas: " + registro.getTotalTaskCreated());
    }
}
