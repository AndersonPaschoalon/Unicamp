package com.nexus.view;

import org.jline.reader.*;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;

import java.util.List;

public class PromptService {

    /*
     * Terminal físico.
     * Responsável por escrever no console.
     */
    private final Terminal terminal;

    /*
     * Objeto principal do JLine.
     * Ele lê entradas do usuário.
     */
    private final LineReader reader;

    /**
     * Completer atualmente ativo.
     * Pode mudar dinamicamente conforme o contexto.
     */
    private Completer activeCompleter;

    public PromptService(Terminal terminal, List<String> rootCommands) {

        this.terminal = terminal;

        /*
         * Completer delegado:
         *
         * O LineReader sempre conversa com ele.
         * Ele por sua vez delega para o contexto atual.
         */
        Completer delegatingCompleter = (reader, line, candidates) -> {

            if (activeCompleter != null) {
                activeCompleter.complete(
                        reader,
                        line,
                        candidates);
            }
        };

        this.reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(delegatingCompleter)
                .build();

        /*
         * Contexto inicial:
         * comandos principais da CLI.
         */
        this.activeCompleter = new StringsCompleter(rootCommands);
    }

    /*
     * Leitura simples sem trocar contexto.
     *
     * Exemplo:
     *
     * String line = prompt.readString("nexus> ");
     */
    public String readString(String promptText) {

        return reader.readLine(promptText).trim();
    }

    /*
     * Leitura temporária com novo contexto.
     *
     * Exemplo:
     *
     * String project =
     * prompt.readWithOptions(
     * "Projeto: ",
     * List.of("Alpha", "Beta")
     * );
     *
     * Durante essa chamada:
     *
     * TAB sugere Alpha/Beta.
     *
     * Depois:
     *
     * o contexto anterior é restaurado.
     */
    public String readWithOptions(
            String promptText,
            List<String> options) {

        /*
         * Guarda contexto anterior.
         */
        Completer previous = this.activeCompleter;

        try {

            /*
             * Troca contexto temporariamente.
             */
            this.activeCompleter = new StringsCompleter(options);

            /*
             * Faz leitura.
             */
            return reader.readLine(promptText).trim();

        } finally {

            /*
             * Restaura o contexto anterior.
             */
            this.activeCompleter = previous;
        }
    }

    public void printSuccess(String message) {
        terminal.writer().println(
                "\u001B[32m✔ " + message + "\u001B[0m");
    }

    public void printInfo(String message) {
        terminal.writer().println(
                "\u001B[34m🔵 " + message + "\u001B[0m");
    }

    public void printWarning(String message) {
        terminal.writer().println(
                "\u001B[33m⚠ " + message + "\u001B[0m");
    }

    public void printError(String message) {
        terminal.writer().println(
                "\u001B[31m❌ " + message + "\u001B[0m");
    }
}