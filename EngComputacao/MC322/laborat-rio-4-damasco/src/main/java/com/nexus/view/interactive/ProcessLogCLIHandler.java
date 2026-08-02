package com.nexus.view.interactive;

import java.util.List;

import com.nexus.controller.LogProcessor;
import com.nexus.exception.NexusSystemException;
import com.nexus.view.PromptService;

/**
 * Interactive handler for guided log file processing.
 *
 * <p>Presents the available log files and delegates processing to
 * {@link LogProcessor}. Because log processing is a batch operation
 * that reads and dispatches multiple commands internally, this handler
 * invokes the processor directly rather than building a single
 * {@code Command}.</p>
 */
public class ProcessLogCLIHandler implements InteractiveCLIHandler {

    private static final List<String> LOG_OPTIONS = List.of("1", "2", "3");

    private final LogProcessor logProcessor;

    /**
     * Constructs the handler with the required log processor.
     *
     * @param logProcessor the processor used to parse and execute log files
     */
    public ProcessLogCLIHandler(LogProcessor logProcessor) {
        this.logProcessor = logProcessor;
    }

    @Override
    public String getTriggerWord() {
        return "PROCESS_LOG";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        prompt.printInfo("==================================================");
        prompt.printInfo("        NEXUS: IMPORTAÇÃO DE LOG");
        prompt.printInfo("==================================================");
        prompt.printInfo("");

        prompt.printInfo("Arquivos Disponíveis:");
        prompt.printInfo("[1] log_v1.txt (Básico)");
        prompt.printInfo("[2] log_v2.txt (Intermediário)");
        prompt.printInfo("[3] log_v3.txt (Avançado)");
        prompt.printInfo("");
        prompt.printInfo("--------------------------------------------------");

        String choice = prompt.readWithOptions("> Selecione o arquivo de log: ", LOG_OPTIONS);

        String file = switch (choice) {
            case "1" -> "log_v1.txt";
            case "2" -> "log_v2.txt";
            case "3" -> "log_v3.txt";
            default -> null;
        };

        if (file != null) {
            try {
                logProcessor.processLog(file);
                prompt.printSuccess("Log processado com sucesso.");
            } catch (NexusSystemException e) {
                prompt.printError("Erro ao ler arquivo: " + e.getMessage());
            }
        } else {
            prompt.printWarning("Opção inválida.");
        }
    }
}
