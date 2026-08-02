package com.nexus.view.interactive;

import java.util.List;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.view.PromptService;

/**
 * Interactive handler for the unified report dashboard.
 *
 * <p>Presents a menu of available report types and dispatches the
 * chosen report command to the {@link CommandDispatcher}. This
 * consolidates multiple report entry points into a single
 * interactive command.</p>
 */
public class GenerateReportCLIHandler implements InteractiveCLIHandler {

    private static final List<String> REPORT_OPTIONS = List.of(
            "REPORT_STATUS",
            "TOP_PERFORMERS",
            "OVERLOADED_USERS",
            "PROJECT_HEALTH",
            "GLOBAL_BOTTLENECK"
    );

    private final CommandDispatcher dispatcher;

    /**
     * Constructs the handler with the required dispatcher.
     *
     * @param dispatcher the command dispatcher for execution delegation
     */
    public GenerateReportCLIHandler(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getTriggerWord() {
        return "GENERATE_REPORT";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        prompt.printInfo("==================================================");
        prompt.printInfo("        NEXUS: DASHBOARD DE RELATÓRIOS");
        prompt.printInfo("==================================================");
        prompt.printInfo("");

        prompt.printInfo("Relatórios Disponíveis:");
        for (int i = 0; i < REPORT_OPTIONS.size(); i++) {
            prompt.printInfo("[" + (i + 1) + "] " + REPORT_OPTIONS.get(i));
        }
        prompt.printInfo("");
        prompt.printInfo("--------------------------------------------------");

        String choice = prompt.readWithOptions("> Selecione o relatório (ou Tab para sugestões): ", REPORT_OPTIONS);

        String action = choice.trim().toUpperCase();
        if (!REPORT_OPTIONS.contains(action)) {
            prompt.printWarning("Opção inválida: " + choice);
            return;
        }

        Command command = new Command(action, action, new String[0]);

        try {
            String output = dispatcher.dispatchCommand(command);
            if (output != null) {
                prompt.printInfo(output);
            }
        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());
        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }
}
