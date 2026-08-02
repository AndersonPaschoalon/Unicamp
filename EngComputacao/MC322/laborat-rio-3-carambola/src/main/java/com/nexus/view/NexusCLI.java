package com.nexus.view;

import com.nexus.controller.CommandDispatcher;
import com.nexus.controller.LogProcessor;
import com.nexus.service.Workspace;
import com.nexus.service.core.ReportService;
import com.nexus.service.core.UserService;
import org.jline.reader.*;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NexusCLI {

    private final CommandDispatcher dispatcher;
    private final Map<String, InteractiveCLIHandler> interactiveHandlers;

    public NexusCLI(
            CommandDispatcher dispatcher,
            List<InteractiveCLIHandler> handlers) {

        this.dispatcher = dispatcher;

        this.interactiveHandlers = handlers.stream()
                .collect(Collectors.toMap(
                        h -> h.getTriggerWord().toUpperCase(),
                        h -> h));
    }

    public void start() {

        try (Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build()) {

            NexusCLIMenus menus = new NexusCLIMenus(terminal);

            Completer completer = new StringsCompleter(
                    buildCommandList());

            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .build();

            PromptService prompt = new PromptService(
                    terminal,
                    buildCommandList());

            menus.printWelcome();

            while (true) {

                String line;

                try {
                    line = prompt.readString("nexus> ");
                } catch (UserInterruptException | EndOfFileException e) {
                    break;
                }

                if (line.isBlank()) {
                    continue;
                }

                if (line.equalsIgnoreCase("EXIT")) {
                    break;
                }

                routeCommand(line, prompt, menus);
            }

        } catch (Exception e) {
            System.err.println("Erro crítico na CLI: " + e.getMessage());
        }
    }

    private void routeCommand(
            String line,
            PromptService prompt,
            NexusCLIMenus menus) {

        String command = line.toUpperCase();

        if (command.equals("HELP")) {
            menus.printManual();
            return;
        }

        InteractiveCLIHandler handler = this.interactiveHandlers.get(command);

        if (handler != null) {
            handler.processInteractive(prompt);
            return;
        }
        try {
            this.dispatcher.process(line);
        } catch (Exception e) {
            prompt.printError(
                    "Comando " + line + " desconhecido ou inválido.");
        }
    }

    private List<String> buildCommandList() {

        List<String> commands = new ArrayList<>();

        commands.add("HELP");
        commands.add("EXIT");

        commands.addAll(this.interactiveHandlers.keySet());

        return commands;
    }

}
