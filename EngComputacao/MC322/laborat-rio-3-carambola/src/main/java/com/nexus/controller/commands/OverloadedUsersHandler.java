
package com.nexus.controller.commands;

import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;

import com.nexus.service.core.ReportService;

public class OverloadedUsersHandler implements CommandHandler {

    private final ReportService reportService;

    public OverloadedUsersHandler(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        this.requireParamCount(command.getParameters(), 0, "OVERLOADED_USERS");
        this.reportService.generateOverloadedUsersReport(workspace);
    }
}