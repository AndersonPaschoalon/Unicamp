package com.nexus.view.statescli.menus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.nexus.controller.CommandDispatcher;
import com.nexus.model.Command;
import com.nexus.view.PromptService;
import com.nexus.view.statescli.ViewState;

public class ReportMenuState implements ViewState {

    private final ViewState parent;
    private final CommandDispatcher dispatcher;
    private final Map<String, Supplier<ViewState>> transitions = new HashMap<>();

    public ReportMenuState(ViewState parent) {
        this.parent = parent;
        this.dispatcher = CommandDispatcher.getInstance();

        registerTransition("performers", () -> runReport("TOP_PERFORMERS"));
        registerTransition("overloaded", () -> runReport("OVERLOADED_USERS"));
        registerTransition("health", () -> runReport("PROJECT_HEALTH"));
        registerTransition("bottleneck", () -> runReport("GLOBAL_BOTTLENECK"));
        registerTransition("status", () -> runReport("REPORT_STATUS"));
        registerTransition("voltar", () -> parent);
    }

    private void registerTransition(String command, Supplier<ViewState> stateSupplier) {
        transitions.put(command, stateSupplier);
    }

    private ViewState runReport(String action) {
        Command command = new Command(null, action, new String[] {});
        String output = dispatcher.dispatchCommand(command);
        if (output != null) {
            System.out.println(output);
        }
        return this;
    }

    @Override
    public Collection<String> getAutocompleteCommands() {
        return transitions.keySet();
    }

    @Override
    public void renderHeader(PromptService prompt) {
        clearConsole(prompt);
        ViewState.super.renderHeader(prompt);
        prompt.printInfo("=== RELATÓRIOS ===");
        prompt.printInfo("performers  - Top 3 usuários com mais tarefas concluídas");
        prompt.printInfo("overloaded  - Usuários com mais de 10 tarefas em andamento");
        prompt.printInfo("health      - Percentual de conclusão por projeto");
        prompt.printInfo("bottleneck  - Status não-terminal com mais tarefas");
        prompt.printInfo("status      - Relatório completo (todos os anteriores)");
        prompt.printInfo("voltar      - Retornar ao menu anterior");
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String input = promptService.readString(getPromptText()).trim().toLowerCase();

        Supplier<ViewState> transition = transitions.get(input);
        if (transition != null) {
            return transition.get();
        }

        promptService.printWarning("Comando inválido. Tente novamente.");
        return this;
    }

    @Override
    public String getPromptText() {
        return "nexus/relatorios> ";
    }
}