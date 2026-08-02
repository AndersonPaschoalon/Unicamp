package com.nexus.service;

import java.util.List;
import com.nexus.exception.NexusValidationException;
import com.nexus.model.TelemetryRegistry;


/**
 * Traduz uma linha de texto em um objeto ou instrução compreensível. Esta classe é responsável por:
 * 
 */
public class CommandParser {

    private TelemetryRegistry reg;
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";


    /**
     * Construtor.
     * 
     * @param reg
     */
    public CommandParser(TelemetryRegistry reg) {
        this.reg = reg;
    }

    /**
     * Roda a versao automatica do programa para validacao
     * 
     * 
     * @param nome_arq um {@link String} com o nome do arquivo a ser lido
     * @param engine o {@link NexusEngine} que tem o coracao do programa
     * 
     */
    public void rodar(String nome_arq, NexusEngine engine) {
        FileLoader arquivoLido = new FileLoader();

        arquivoLido.Carregar(nome_arq);
        List<String> comandosRodar = arquivoLido.getComandos();

        for (String comandoRaw : comandosRodar) {
            comandoRaw = comandoRaw.trim();
            if (comandoRaw.isBlank() || comandoRaw.startsWith("#"))
                continue;
            String comando = comandoRaw.split("#")[0].trim();
            CommandParser.printCommand(comando);
            String[] argumentos = comando.split(";");
            String qual_comando = argumentos[0].toUpperCase();
            try {
                switch (qual_comando) {

                    case "CREATE_USER" -> {
                        if (argumentos.length != 3) {
                            throw new NexusValidationException(
                                    "Esperado 2 parametros para CREATE_USER, mas recebeu "
                                            + (argumentos.length - 1));
                        }
                        engine.create_user(argumentos[1], argumentos[2]);
                    }
                    case "CREATE_PROJECT" -> {
                        if (argumentos.length != 3) {
                            throw new NexusValidationException(
                                    "Esperado 2 parâmetros para CREATE_PROJECT, mas recebeu "
                                            + (argumentos.length - 1));
                        }
                        engine.create_project(argumentos[1], argumentos[2]);
                    }
                    case "CREATE_TASK" -> {
                        if (argumentos.length != 5) {
                            throw new NexusValidationException(
                                    "Esperado 4 parâmetros para CREATE_TASK, mas recebeu "
                                            + (argumentos.length - 1));
                        }
                        engine.create_task(argumentos[1], argumentos[2], argumentos[3],
                                argumentos[4]);
                    }
                    case "ASSIGN_USER" -> {
                        if (argumentos.length != 3) {
                            throw new NexusValidationException(
                                    "Esperado 2 parametros para ASSIGN_USER, mas recebeu "
                                            + (argumentos.length - 1));
                        }
                        engine.assign_user(argumentos[1], argumentos[2]);
                    }
                    case "CHANGE_STATUS" -> {
                        if (argumentos.length != 4) {
                            throw new NexusValidationException(
                                    "Esperado 3 parametros para CHANGE_STATUS, mas recebeu "
                                            + (argumentos.length - 1));
                        }
                        engine.change_status(argumentos[1], argumentos[2], argumentos[3]);
                    }
                    case "REPORT_STATUS" -> {
                        if (argumentos.length != 1) {
                            throw new NexusValidationException(
                                    "Esperado 0 parâmetros para REPORT_STATUS, mas recebeu "
                                            + (argumentos.length - 1));
                        }
                        engine.report_status();
                    }
                    case "CREATE_STATUS" -> {
                        if (argumentos.length != 3) {
                            throw new NexusValidationException(
                                    "Esperado 2 parametros para CREATE_STATUS, mas recebeu "
                                            + (argumentos.length - 1));
                        }
                        engine.create_status(argumentos[1], argumentos[2]);
                    }
                    case "CREATE_WORKFLOW" -> {
                        if (argumentos.length != 2) {
                            throw new NexusValidationException(
                                    "Esperado 1 parametro para CREATE_WORKFLOW, mas recebeu "
                                            + (argumentos.length - 1));
                        }
                        engine.create_workflow(argumentos[1]);
                    }
                    case "ALLOW_TRANSITION" -> {
                        if (argumentos.length != 4) {
                            throw new NexusValidationException(
                                    "Esperado 3 parametro para ALLOW_TRANSITION, mas recebeu "
                                            + (argumentos.length - 1));
                        }
                        engine.allow_transition(argumentos[1], argumentos[2], argumentos[3]);
                    }
                    case "SET_INITIAL_STATUS" -> {
                        if (argumentos.length != 3) {
                            throw new NexusValidationException(
                                    "Esperado 2 parametro para SET_INITIAL_STATUS, mas recebeu "
                                            + (argumentos.length - 1));
                        }
                        engine.set_initial_status(argumentos[1], argumentos[2]);
                    }
                    case "VINCULAR_WORKFLOW" -> {
                        if (argumentos.length != 3) {
                            throw new NexusValidationException(
                                    "Esperado 2 parametro para VINCULAR_WORKFLOW, mas recebeu "
                                            + (argumentos.length - 1));
                        }
                        engine.vincular_workflow(argumentos[1], argumentos[2]);
                    }
                    case "ADD_MEMBER" -> {
                        if (argumentos.length != 4) {
                            throw new NexusValidationException(
                                    "Esperado 3 parametro para ADD_MEMBER, mas recebeu "
                                            + (argumentos.length - 1));
                        }
                        engine.add_member(argumentos[1], argumentos[2], argumentos[3]);
                    }
                }
            } catch (NexusValidationException e) {
                reg.getTotalValidationErrors();
                CommandParser.handleErrorLog("REGRAS", comandoRaw, e.getMessage());
                // System.err.println(
                // "[ERRO DE REGRAS] Falha no comando ' " + comando + " ': " + e.getMessage());
            } catch (IllegalArgumentException e) {
                // System.err.println("[ERRO DE FORMATO] Falha no comando ' " + comando + " ': "
                // + e.getMessage());
                CommandParser.handleErrorLog("FORMATO", comandoRaw, e.getMessage());
            } catch (Exception e) {
                // System.err.println("[ERRO INESPERADO] Falha no comando ' " + comando + " ': "
                // + e.getMessage());
                CommandParser.handleErrorLog("INESPERADO", comandoRaw, e.getMessage());
            }
        }
    }


    /**
     * Imprime um comando (em verde).
     * 
     * @param cmd
     */
    private static void printCommand(String cmd) {
        System.out.println(CommandParser.ANSI_GREEN + cmd + CommandParser.ANSI_RESET);
    }

    /**
     * Método mara facilitar o debug. Imprime o erro obtido, e logo abaixo o erro esperado, caso ele
     * esteja anotado no comentário com @Error{}. Se não encontrar a anotação, no formato correto,
     * não faz nada.
     * 
     * @param tipo
     * @param comando
     * @param msg
     */
    private static void handleErrorLog(String tipo, String comando, String msg) {
        System.err.println(ANSI_RED + "[ERRO DE " + tipo + "] Falha no comando: '" + comando + "'"
                + ANSI_RESET);
        System.err.println(ANSI_RED + "Mensagem: " + msg + ANSI_RESET);
        if (comando != null && comando.contains("@Error(")) {
            try {
                int inicio = comando.indexOf("@Error(") + 7;
                int fim = comando.indexOf(")", inicio);
                if (fim > inicio) {
                    String debugMsg = comando.substring(inicio, fim);
                    System.out.println(
                            ANSI_YELLOW + "    [DEBUG] Erro Esperado: " + debugMsg + ANSI_RESET);
                }
            } catch (Exception ex) {
            }
        }
    }

}
