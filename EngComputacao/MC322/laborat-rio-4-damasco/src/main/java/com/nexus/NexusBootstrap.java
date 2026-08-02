package com.nexus;

import com.nexus.controller.CommandDispatcher;
import com.nexus.model.valueobject.Role;
import com.nexus.service.TelemetryRegistry;
import com.nexus.service.Workspace;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.service.core.ProjectService;
import com.nexus.service.core.ReportService;
import com.nexus.service.core.TaskService;
import com.nexus.service.core.UserService;
import com.nexus.service.core.WorkflowService;
import com.nexus.service.core.command.CommandFactory;


public final class NexusBootstrap {
    private NexusBootstrap() {
    }

    public static Workspace initialize() {
        Workspace workspace = new Workspace(); 

        int tasksCreated = workspace.findAllTasks().size();
        int workloadActive = (int) workspace.findAllTasks().stream()
                .filter(t -> "IN_PROGRESS".equals(t.getStatusId()))
                .count();
        TelemetryRegistry.getInstance().hydrate(tasksCreated, workloadActive);

        UserService userService = new UserService(workspace);
        WorkflowService workflowService = new WorkflowService(workspace);
        ProjectService projectService = new ProjectService(userService, workflowService, workspace);
        TaskService taskService = new TaskService(userService, projectService, workflowService, workspace);
        ReportService reportService = new ReportService(workspace);

       
        if (workspace.getUser("admin").isEmpty()) {
            userService.createUser("admin", "admin@nexus.com", Role.MANAGER);
        }

        CommandFactory commandFactory = new CommandFactory(
                userService, projectService, taskService, workflowService, reportService);
        CommandDispatcher.build(commandFactory);
        WorkspaceQueryServiceImpl.build(workspace);

        return workspace;
    }
}
