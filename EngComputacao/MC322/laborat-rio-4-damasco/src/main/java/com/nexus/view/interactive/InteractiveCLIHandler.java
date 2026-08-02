package com.nexus.view.interactive;

import com.nexus.view.PromptService;

/**
 * Contract for interactive CLI command handlers.
 *
 * <p>An {@code InteractiveCLIHandler} is activated exclusively when the CLI
 * parser detects that the user typed only the command keyword (or provided
 * insufficient arguments) and an interactive handler is registered for that
 * keyword. The handler then takes control of the terminal to guide the user
 * step-by-step through data collection.</p>
 *
 * <p>Implementations must <strong>never</strong> apply business rules or
 * mutate domain state directly. Their sole responsibility is to collect
 * input (using {@link PromptService} for I/O and
 * {@link com.nexus.service.WorkspaceQueryService} for read-only data),
 * build a {@link com.nexus.model.Command}, and delegate execution to the
 * {@link com.nexus.controller.CommandDispatcher}.</p>
 */
public interface InteractiveCLIHandler {

    /**
     * Returns the command keyword that activates this handler.
     *
     * @return the trigger word (e.g. {@code "CREATE_USER"})
     */
    String getTriggerWord();

    /**
     * Takes control of the terminal to collect missing data interactively
     * and, upon completion, dispatches the fully-formed command to the
     * Controller layer.
     *
     * @param prompt the prompt service used for all terminal I/O
     */
    void processInteractive(PromptService prompt);
}
