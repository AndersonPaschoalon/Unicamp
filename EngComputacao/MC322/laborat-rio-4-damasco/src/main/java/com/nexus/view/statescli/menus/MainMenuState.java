package com.nexus.view.statescli.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.nexus.view.PromptService;
import com.nexus.view.statescli.ExitState;
import com.nexus.view.statescli.ViewState;

public class MainMenuState implements ViewState {

    private final Map<String, Supplier<ViewState>> transitions = new HashMap<>();

    public MainMenuState() {
        registerTransition("projetos", () -> new ProjectMenuState(this));
        registerTransition("status", () -> new StatusManagementState(this));
        registerTransition("usuarios", () -> new UserMenuState(this));
        registerTransition("relatorios", () -> new ReportMenuState(this));
        registerTransition("workflows", () -> new WorkflowManagementState(this));
        registerTransition("exit", () -> new ExitState());
    }

    private void registerTransition(String command, Supplier<ViewState> stateSupplier) {
        transitions.put(command, stateSupplier);
    }

    @Override
    public void renderHeader(PromptService prompt) {
        clearConsole(prompt);
        ViewState.super.renderHeader(prompt);
        prompt.printInfo("Comandos Disponíveis: " + String.join(", ", getAutocompleteCommands()));
    }

    @Override
    public ViewState handleInput(PromptService promptService) {
        String input = promptService.readString(getPromptText()).trim();
        String cleanedInput = input.trim().toLowerCase();

        if (cleanedInput.isEmpty())
            return this;

        Supplier<ViewState> transition = transitions.get(cleanedInput);
        if (transition != null) {
            return transition.get();
        }

        promptService.printWarning("Comando inválido. Tente novamente.");
        return this;
    }

    @Override
    public String getPromptText() {
        return "nexus> ";
    }

    @Override
    public List<String> getAutocompleteCommands() {
        List<String> commands = new ArrayList<>();
        commands.addAll(transitions.keySet());
        return commands;
    }
}