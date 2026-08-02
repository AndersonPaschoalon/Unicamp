package com.nexus.view.statescli;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import com.nexus.view.PromptService;

public interface ViewState {

    ViewState handleInput(PromptService prompt);

    Collection<String> getAutocompleteCommands();

    // --- MÉTODOS DEFAULT COMPARTILHADOS ---

    default void renderHeader(PromptService prompt) {
        prompt.printInfo("\n========================================");
        prompt.printInfo("============NEXUS CORE==================");
        prompt.printInfo("========================================");
    }

    default void registerTransition(String command, Supplier<ViewState> stateSupplier,
            Map<String, Supplier<ViewState>> transitions) {
        transitions.put(command, stateSupplier);
    }

    default void clearConsole(PromptService prompt) {
        prompt.printInfo("\033[H\033[2J");
        System.out.flush();
    }

    default void printFooter(PromptService prompt) {
        prompt.printInfo("\nDigite 'VOLTAR' para retornar ao menu anterior.");
    }

    default String getPromptText() {
        return "> ";
    }

    default boolean isTerminal() {
        return false;
    }
}