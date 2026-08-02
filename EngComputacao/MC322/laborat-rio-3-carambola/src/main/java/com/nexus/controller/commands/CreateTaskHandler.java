package com.nexus.controller.commands;

import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;
import com.nexus.service.core.TaskService;

public class CreateTaskHandler implements CommandHandler {

    private final TaskService taskService;

    public CreateTaskHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        String[] p = command.getParameters();
        requireParamCount(p, 4, "CREATE_TASK");
        // taskService.createTask(
        // extractString(p, 0, "title"),
        // extractDate(p, 1, "dueDate"),
        // extractLong(p, 2, "projectId"),
        // extractString(p, 3, "assignee"), workspace);
        this.taskService.createTask(
                this.extractString(p, 0, "title"),
                this.extractDate(p, 1, "deadline"),
                this.extractLong(p, 2, "estimatedEffort"),
                this.extractString(p, 3, "projectName"),
                workspace);

    }
}
