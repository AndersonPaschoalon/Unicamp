package com.nexus.service.core.command;

import com.nexus.exception.CommandFormatException;
import com.nexus.model.Command;
import com.nexus.service.core.ReportService;

public class ReportStatusHandler implements CommandHandler {
    private final ReportService reportService;

    public ReportStatusHandler(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void handle(Command command) {
        String[] parameters = command.getParameters();
        if (parameters.length != 0) {
            throw new CommandFormatException(
                    "Esperado 0 parâmetros para REPORT_STATUS, mas recebeu " + parameters.length);
        }
    }

    @Override
    public String handleWithOutput(Command command) {
        handle(command);
        return reportService.generateStatusReport();
    }
    
}
