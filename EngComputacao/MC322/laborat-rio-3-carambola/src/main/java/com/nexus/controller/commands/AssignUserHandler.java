
package com.nexus.controller.commands;

import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;
import com.nexus.service.core.TaskService;

public class AssignUserHandler implements CommandHandler {
    private final TaskService taskService;

    public AssignUserHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        String[] p = command.getParameters();
        requireParamCount(p, 2, "ASSIGN_USER");
        this.taskService.assignUserToTask(this.extractString(p, 0, "taskId"), this.extractString(p, 1, "username"),
                workspace);
    }
}
