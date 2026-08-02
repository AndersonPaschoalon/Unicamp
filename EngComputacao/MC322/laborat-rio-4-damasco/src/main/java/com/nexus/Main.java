package com.nexus;

import java.io.IOException;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import com.nexus.controller.CommandDispatcher;
import com.nexus.controller.LogProcessor;
import com.nexus.service.Workspace;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.service.core.ReportService;
import com.nexus.view.NexusCLI;
import com.nexus.view.PromptService;

/**
 * Entry point for the Nexus application.
 *
 * <p>
 * Uso: sem argumentos inicia a CLI interativa; passando um nome de arquivo
 * (presente no classpath, ex: {@code log_test_p2.txt}), processa o log via
 * {@link LogProcessor} e imprime o relatório de status, sem abrir a CLI —
 * útil para smoke tests enquanto a camada interativa (Parte 1) está em obras.
 * </p>
 */
public class Main {
    public static void main(String[] args) throws IOException {
        // Wiring comum (Workspace, serviços de domínio, CommandDispatcher e
        // WorkspaceQueryService) agora vive em NexusBootstrap, compartilhado
        // com a Web (MainGui). Aqui só ficam as partes específicas da CLI.
        Workspace workspace = NexusBootstrap.initialize();

        CommandDispatcher dispatcher = CommandDispatcher.getInstance();
        LogProcessor logProcessor = new LogProcessor(dispatcher);
        WorkspaceQueryService queryService = WorkspaceQueryServiceImpl.getInstance();
        ReportService reportService = new ReportService(workspace);

        if (args.length > 0) {
            logProcessor.processLog(args[0]);
            System.out.println(reportService.generateStatusReport());
            return;
        }

        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            PromptService promptService = createPromptService(terminal);
            NexusCLI cli = new NexusCLI(promptService, dispatcher, queryService);
            cli.run();
        } catch (IOException e) {
            System.err.println("Failed to initialize terminal: " + e.getMessage());
        }
    }

    private static PromptService createPromptService(Terminal terminal) {
        final PromptService[] holder = { null };
        Completer delegatingCompleter = (reader, line, candidates) -> {
            if (holder[0] != null) {
                Completer active = holder[0].getActiveCompleter();
                if (active != null) {
                    active.complete(reader, line, candidates);
                }
            }
        };
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(delegatingCompleter)
                .option(LineReader.Option.AUTO_LIST, true)
                .option(LineReader.Option.AUTO_MENU, true)
                .option(LineReader.Option.MENU_COMPLETE, true)
                .option(LineReader.Option.LIST_AMBIGUOUS, true)
                .build();
        PromptService promptService = new PromptService(reader, terminal);
        holder[0] = promptService;
        return promptService;
    }
}