package com.nexus.view.handlers;

import java.util.List;
import java.util.Map;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.view.InteractiveCLIHandler;
import com.nexus.view.PromptService;

public class GenerateReportCLIHandler
        implements InteractiveCLIHandler {

    private final CommandDispatcher dispatcher;

    private static final Map<String, String> REPORT_OPTIONS = Map.of(
            "1", "REPORT_STATUS",
            "2", "TOP_PERFORMERS",
            "3", "OVERLOADED_USERS",
            "4", "PROJECT_HEALTH",
            "5", "GLOBAL_BOTTLENECK");

    public GenerateReportCLIHandler(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getTriggerWord() {
        return "GENERATE_REPORT";
    }

    @Override
    public void processInteractive(PromptService prompt) {
        this.printMenu();
        String option = prompt.readString("Selecione o relatório: ");
        String commandName = REPORT_OPTIONS.get(option);

        if (commandName == null) {
            prompt.printWarning("Opção inválida.");
            return;
        }

        Command command = new Command(commandName, new String[] {});

        try {
            dispatcher.dispatch(command);
            prompt.printSuccess("Relatório gerado com sucesso.");

        } catch (NexusDomainException e) {
            prompt.printWarning("Regra de Negócio: " + e.getMessage());

        } catch (NexusSystemException e) {
            prompt.printError("Falha Estrutural: " + e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("NEXUS REPORTS:");
        System.out.println("1 - REPORT_STATUS");
        System.out.println("2 - TOP_PERFORMERS");
        System.out.println("3 - OVERLOADED_USERS");
        System.out.println("4 - PROJECT_HEALTH");
        System.out.println("5 - GLOBAL_BOTTLENECK");
        System.out.println();
    }
}