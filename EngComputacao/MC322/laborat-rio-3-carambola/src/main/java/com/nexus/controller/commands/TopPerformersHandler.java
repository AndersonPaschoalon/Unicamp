
package com.nexus.controller.commands;

import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;

import com.nexus.service.core.ReportService;

public class TopPerformersHandler implements CommandHandler {

    private final ReportService reportService;

    public TopPerformersHandler(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        this.requireParamCount(command.getParameters(), 0, "TOP_PERFORMERS");
        this.reportService.generateTopPerformersReport(workspace);
    }
}
