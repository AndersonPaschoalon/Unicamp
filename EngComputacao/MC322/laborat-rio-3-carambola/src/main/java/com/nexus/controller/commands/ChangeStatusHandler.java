
package com.nexus.controller.commands;

import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;
import com.nexus.service.core.TaskService;
import com.nexus.service.core.UserService;

public class ChangeStatusHandler implements CommandHandler {

    private final TaskService taskService;
    private final UserService userService;

    public ChangeStatusHandler(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        String[] p = command.getParameters();
        requireParamCount(p, 3, "MOVE_TASK");
        this.taskService.changeTaskStatus(this.extractString(p, 0, "taskId"), this.extractString(p, 1, "fromStatus"),
                this.extractString(p, 2, "toStatus"), this.userService.getUsers(), workspace);
    }
}
