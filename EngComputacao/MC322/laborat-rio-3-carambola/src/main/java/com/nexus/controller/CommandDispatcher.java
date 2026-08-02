package com.nexus.controller;

import java.util.HashMap;
import java.util.Map;

import com.nexus.controller.commands.AddMemberHandler;
import com.nexus.controller.commands.AllowTransitionHandler;
import com.nexus.controller.commands.AssignUserHandler;
import com.nexus.controller.commands.BottleneckHandler;
import com.nexus.controller.commands.ChangeStatusHandler;
import com.nexus.controller.commands.CreateProjectHandler;
import com.nexus.controller.commands.CreateStatusHandler;
import com.nexus.controller.commands.CreateTaskHandler;
import com.nexus.controller.commands.CreateUserHandler;
import com.nexus.controller.commands.CreateWorkflowHandler;
import com.nexus.controller.commands.LinkWorkflowHandler;
import com.nexus.controller.commands.OverloadedUsersHandler;
import com.nexus.controller.commands.ProjectHealthHandler;
import com.nexus.controller.commands.ReportStatusHandler;
import com.nexus.controller.commands.SetInitialStatusHandler;
import com.nexus.controller.commands.TopPerformersHandler;
import com.nexus.controller.commands.ViewKanbanHandler;
import com.nexus.exception.CommandFormatException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.service.TelemetryRegistry;
import com.nexus.service.Workspace;
import com.nexus.service.core.ProjectService;
import com.nexus.service.core.ReportService;
import com.nexus.service.core.TaskService;
import com.nexus.service.core.UserService;
import com.nexus.service.core.WorkflowService;
import com.nexus.service.infra.CommandParser;
import com.nexus.service.query.WorkspaceQueryServiceInterface;

public class CommandDispatcher {

    private final Map<String, CommandHandler> handlers;

    private final TelemetryRegistry telemetryRegistry;
    private final Workspace workspace;

    public CommandDispatcher(
            WorkspaceQueryServiceInterface queryService,
            TelemetryRegistry telemetryRegistry,
            Workspace workspace,
            WorkflowService workflowService,
            TaskService taskService,
            UserService userService,
            ReportService reportService,
            ProjectService projectService) {

        this.telemetryRegistry = telemetryRegistry;
        this.workspace = workspace;

        this.handlers = createHandlers(
                queryService,
                workflowService,
                taskService,
                userService,
                reportService,
                projectService);
    }

    private Map<String, CommandHandler> createHandlers(
            WorkspaceQueryServiceInterface queryService,
            WorkflowService workflowService,
            TaskService taskService,
            UserService userService,
            ReportService reportService,
            ProjectService projectService) {

        Map<String, CommandHandler> h = new HashMap<>();
        // User Services
        h.put("CREATE_USER", new CreateUserHandler(userService));

        // Workflow Services
        h.put("ALLOW_TRANSITION", new AllowTransitionHandler(workflowService));
        h.put("CREATE_STATUS", new CreateStatusHandler(workflowService));
        h.put("CREATE_WORKFLOW", new CreateWorkflowHandler(workflowService));
        h.put("SET_INITIAL_STATUS", new SetInitialStatusHandler(workflowService));

        // Task Services
        h.put("ASSIGN_USER", new AssignUserHandler(taskService));
        h.put("CHANGE_STATUS", new ChangeStatusHandler(taskService, userService));
        h.put("MOVE_TASK", new ChangeStatusHandler(taskService, userService));
        h.put("CREATE_TASK", new CreateTaskHandler(taskService));

        // Project Services
        h.put("CREATE_PROJECT", new CreateProjectHandler(projectService));
        h.put("LINK_WORKFLOW", new LinkWorkflowHandler(projectService));
        h.put("ADD_MEMBER", new AddMemberHandler(projectService));

        // Report services
        h.put("REPORT_STATUS", new ReportStatusHandler(reportService));
        h.put("TOP_PERFORMERS", new TopPerformersHandler(reportService));
        h.put("PROJECT_HEALTH", new ProjectHealthHandler(reportService));
        h.put("BOTTLENECK", new BottleneckHandler(reportService));
        h.put("OVERLOADED_USERS", new OverloadedUsersHandler(reportService));

        // Kanban
        h.put("VIEW_KANBAN", new ViewKanbanHandler(queryService));
        
        return h;
    }

    public void process(String line) {

        Command command = CommandParser.parseCommand(line);

        if (command == null) {
            throw new CommandFormatException("Comando inválido");
        }

        dispatch(command);
    }

    public void dispatch(Command command) {
        CommandHandler handler = handlers.get(command.getAction());
        if (handler == null) {
            throw new CommandFormatException(
                    "Comando desconhecido: " + command.getAction());
        }
        try {
            handler.handle(command, workspace);
        } catch (NexusSystemException ex) {
            telemetryRegistry.incrementValidationErrors();
            throw ex;
        }
    }
}
