package com.nexus.service.core.command;

import com.nexus.model.Command;
import com.nexus.service.core.ReportService;

/**
 * Handles the {@code LIST_TASKS} command by delegating to {@link ReportService}.
 */
public class ListTasksHandler implements CommandHandler {
    private final ReportService reportService;

    /**
     * Constructs the handler with the required report service.
     *
     * @param reportService the report service used to list tasks
     */
    public ListTasksHandler(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void handle(Command command) {
    }

    @Override
    public String handleWithOutput(Command command) {
        return reportService.listTasks();
    }
}
