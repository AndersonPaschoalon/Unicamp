package com.nexus.service.core.command;

import com.nexus.exception.CommandFormatException;
import com.nexus.model.Command;
import com.nexus.service.core.TaskService;

public class ChangeStatusHandler implements CommandHandler {
    private final TaskService taskService;

    public ChangeStatusHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void handle(Command command) {
        String[] parameters = command.getParameters();
        if (parameters.length != 3) {
            throw new CommandFormatException(
                    "Esperado 3 parâmetros para CHANGE_STATUS, mas recebeu " + parameters.length);
        }
        taskService.changeTaskStatus(parameters[0], parameters[1], parameters[2]);
    }
    
}
