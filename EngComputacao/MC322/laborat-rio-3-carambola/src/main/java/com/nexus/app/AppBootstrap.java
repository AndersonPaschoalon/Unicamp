package com.nexus.app;

import com.nexus.controller.CommandDispatcher;
import com.nexus.service.TelemetryRegistry;
import com.nexus.service.Workspace;
import com.nexus.service.core.ProjectService;
import com.nexus.service.core.ReportService;
import com.nexus.service.core.TaskService;
import com.nexus.service.core.UserService;
import com.nexus.service.core.WorkflowService;
import com.nexus.service.persistence.NexusData;
import com.nexus.service.persistence.PersistenceService;
import com.nexus.service.query.WorkspaceQueryService;
import com.nexus.service.query.WorkspaceQueryServiceInterface;

public class AppBootstrap {

    public static AppContext create() {

        PersistenceService persistence = new PersistenceService();

        NexusData data = persistence.load();

        Workspace workspace = data.getWorkspace();
        UserService userService = data.getUserService();

        ReportService reportService = new ReportService();

        WorkflowService workflowService = new WorkflowService();

        TelemetryRegistry telemetryRegistry = TelemetryRegistry.getInstance();

        ProjectService projectService = new ProjectService(userService);

        TaskService taskService = new TaskService(
                userService,
                projectService,
                telemetryRegistry);


        WorkspaceQueryServiceInterface queryService = new WorkspaceQueryService(
                workspace,
                userService);

        CommandDispatcher dispatcher = new CommandDispatcher(
                queryService,
                telemetryRegistry,
                workspace,
                workflowService,
                taskService,
                userService,
                reportService,
                projectService);

        return new AppContext(
                persistence,
                workspace,
                userService,
                workflowService,
                taskService,
                projectService,
                reportService,
                telemetryRegistry,
                dispatcher,
                queryService);
    }
}
