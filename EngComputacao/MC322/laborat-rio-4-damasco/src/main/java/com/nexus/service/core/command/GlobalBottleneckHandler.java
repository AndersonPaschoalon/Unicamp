package com.nexus.service.core.command;

import com.nexus.model.Command;
import com.nexus.service.core.ReportService;

public class GlobalBottleneckHandler implements CommandHandler {
    private final ReportService reportService;

    public GlobalBottleneckHandler(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void handle(Command command) {
    }

    @Override
    public String handleWithOutput(Command command) {
        return reportService.generateBottleneckReport();
    }
}
