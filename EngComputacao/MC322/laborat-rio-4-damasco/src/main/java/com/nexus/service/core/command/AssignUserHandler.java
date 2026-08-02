package com.nexus.service.core.command;

import com.nexus.exception.CommandFormatException;
import com.nexus.model.Command;
import com.nexus.service.core.TaskService;

public class AssignUserHandler implements CommandHandler {
    private final TaskService taskService;

    public AssignUserHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void handle(Command command) {
        String[] parameters = command.getParameters();
        if (parameters.length != 2) {
            throw new CommandFormatException(
                "Esperado 2 parâmetros para ASSIGN_USER, mas recebeu " + parameters.length);
        }
        taskService.assignUserToTask(parameters[0], parameters[1]);
    }
    
}
