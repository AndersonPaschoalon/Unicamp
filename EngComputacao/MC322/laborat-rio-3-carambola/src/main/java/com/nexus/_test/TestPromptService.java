package com.nexus._test;

import java.util.List;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import com.nexus.view.PromptService;

public class TestPromptService {

    /**
     * Teste manual do PromptService.
     * 
     * @throws Exception
     */
    public static void run() throws Exception {

        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        PromptService prompt = new PromptService(
                terminal,
                List.of(
                        "HELP",
                        "ASSIGN_USER",
                        "EXIT"));

        terminal.writer().println(
                "Toy CLI iniciada.");

        while (true) {

            String line = prompt.readString("toy> ");

            if (line.equalsIgnoreCase("EXIT")) {
                break;
            }

            if (line.equalsIgnoreCase("HELP")) {

                prompt.printInfo("""
                        HELP
                        ASSIGN_USER
                        EXIT
                        """);

                continue;
            }

            if (line.equalsIgnoreCase("ASSIGN_USER")) {

                String project = prompt.readWithOptions(
                        "Projeto: ",
                        List.of(
                                "AlphaProject",
                                "BetaProject",
                                "GammaProject"));

                String user = prompt.readWithOptions(
                        "Usuário: ",
                        List.of(
                                "alice",
                                "bob",
                                "charlie"));

                prompt.printInfo(
                        "Projeto selecionado: " + project);

                prompt.printInfo(
                        "Usuário selecionado: " + user);

                continue;
            }

            prompt.printError("Comando inválido.");
        }
    }

}
