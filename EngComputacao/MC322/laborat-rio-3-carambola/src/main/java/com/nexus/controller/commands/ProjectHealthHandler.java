package com.nexus.controller.commands;

import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;

import com.nexus.service.core.ReportService;

public class ProjectHealthHandler implements CommandHandler {

    private final ReportService reportService;

    public ProjectHealthHandler(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        this.requireParamCount(command.getParameters(), 0, "PROJECT_HEALTH");
        this.reportService.generateProjectHealthReport(workspace);
    }
}
