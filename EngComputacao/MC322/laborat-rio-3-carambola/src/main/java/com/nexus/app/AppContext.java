package com.nexus.app;

import com.nexus.controller.CommandDispatcher;
import com.nexus.service.TelemetryRegistry;
import com.nexus.service.Workspace;
import com.nexus.service.core.ProjectService;
import com.nexus.service.core.ReportService;
import com.nexus.service.core.TaskService;
import com.nexus.service.core.UserService;
import com.nexus.service.core.WorkflowService;
import com.nexus.service.persistence.PersistenceService;
import com.nexus.service.query.WorkspaceQueryServiceInterface;

/**
 * The class app context works as a container for all the services and
 * components of the application. It is used to pass these dependencies to the
 * CLI handlers and other parts of the application.
 */
public class AppContext implements AutoCloseable {

    private final PersistenceService persistenceService;
    private final Workspace workspace;
    private final UserService userService;
    private final WorkflowService workflowService;
    private final TaskService taskService;
    private final ProjectService projectService;
    private final ReportService reportService;
    private final TelemetryRegistry telemetryRegistry;

    private final CommandDispatcher dispatcher;
    private final WorkspaceQueryServiceInterface queryService;

    public AppContext(
            PersistenceService persistenceService,
            Workspace workspace,
            UserService userService,
            WorkflowService workflowService,
            TaskService taskService,
            ProjectService projectService,
            ReportService reportService,
            TelemetryRegistry telemetryRegistry,
            CommandDispatcher dispatcher,
            WorkspaceQueryServiceInterface queryService) {

        this.persistenceService = persistenceService;
        this.workspace = workspace;
        this.userService = userService;
        this.workflowService = workflowService;
        this.taskService = taskService;
        this.projectService = projectService;
        this.reportService = reportService;
        this.telemetryRegistry = telemetryRegistry;
        this.dispatcher = dispatcher;
        this.queryService = queryService;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public UserService getUserService() {
        return userService;
    }

    public WorkflowService getWorkflowService() {
        return workflowService;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public ProjectService getProjectService() {
        return projectService;
    }

    public ReportService getReportService() {
        return reportService;
    }

    public TelemetryRegistry getTelemetryRegistry() {
        return telemetryRegistry;
    }

    public CommandDispatcher getDispatcher() {
        return dispatcher;
    }

    public WorkspaceQueryServiceInterface getQueryService() {
        return queryService;
    }

    @Override
    public void close() {
        persistenceService.save(
                workspace,
                userService);
    }
}