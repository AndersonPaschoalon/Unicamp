package com.nexus.service.core.command;

import java.time.LocalDate;

import com.nexus.exception.CommandFormatException;
import com.nexus.model.Command;
import com.nexus.service.core.TaskService;

public class CreateTaskHandler implements CommandHandler {
    private final TaskService taskService;

    public CreateTaskHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void handle(Command command) {
        String[] parameters = command.getParameters();
        if (parameters.length != 4 && parameters.length != 5) {
            throw new CommandFormatException(
                    "Esperado 4 ou 5 parâmetros para CREATE_TASK, mas recebeu " + parameters.length);
        }

        LocalDate dueDate = (parameters.length == 5 && !parameters[4].isBlank())
                ? extractDate(parameters, 4, "Data de Vencimento")
                : null;

        taskService.createTask(parameters[0], extractDate(parameters, 1, "Deadline"),
                extractLong(parameters, 2, "Esforço Estimado"),
                parameters[3], dueDate);
    }

}