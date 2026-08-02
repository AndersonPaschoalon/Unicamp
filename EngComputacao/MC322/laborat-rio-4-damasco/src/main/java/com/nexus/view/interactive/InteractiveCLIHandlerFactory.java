package com.nexus.view.interactive;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.nexus.controller.CommandDispatcher;
import com.nexus.controller.LogProcessor;
import com.nexus.service.WorkspaceQueryService;

/**
 * Factory that registers and provides {@link InteractiveCLIHandler}
 * instances keyed by their trigger word.
 *
 * <p>Mirrors the {@link com.nexus.service.core.command.CommandFactory}
 * pattern: all handler wiring is centralised in the constructor, and
 * lookup is performed via {@link #getHandler(String)}.</p>
 */
public class InteractiveCLIHandlerFactory {

    private final Map<String, InteractiveCLIHandler> handlers = new HashMap<>();

    /**
     * Creates the factory, registering every interactive CLI handler.
     *
     * @param dispatcher   the command dispatcher used by handlers to delegate execution
     * @param queryService the read-only query service used for autocompletion data
     * @param logProcessor the log processor used by the log-import handler
     */
    public InteractiveCLIHandlerFactory(CommandDispatcher dispatcher,
                                        WorkspaceQueryService queryService,
                                        LogProcessor logProcessor) {
        handlers.put("PROCESS_LOG", new ProcessLogCLIHandler(logProcessor));
        handlers.put("ADD_TEAM_MEMBER", new AddTeamMemberCLIHandler(dispatcher, queryService));
        handlers.put("ADD_WORKFLOW", new AddWorkflowCLIHandler(dispatcher));
        handlers.put("ADD_TRANSITION", new AddTransitionCLIHandler(dispatcher, queryService));
        handlers.put("LINK_WORKFLOW", new LinkWorkflowCLIHandler(dispatcher, queryService));
        handlers.put("MOVE_TASK", new MoveTaskCLIHandler(dispatcher, queryService));
        handlers.put("GENERATE_REPORT", new GenerateReportCLIHandler(dispatcher));
    }

    /**
     * Returns the interactive handler registered for the given trigger word,
     * or {@code null} if none is registered.
     *
     * @param triggerWord the command keyword (upper-case)
     * @return the matching handler, or {@code null}
     */
    public InteractiveCLIHandler getHandler(String triggerWord) {
        return handlers.get(triggerWord);
    }

    /**
     * Returns the set of all registered trigger words.
     *
     * @return an unmodifiable view of the registered keywords
     */
    public Set<String> getTriggerWords() {
        return handlers.keySet();
    }
}
