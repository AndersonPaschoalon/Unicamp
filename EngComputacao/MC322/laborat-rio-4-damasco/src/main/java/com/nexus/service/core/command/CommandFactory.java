package com.nexus.service.core.command;

import java.util.HashMap;
import java.util.Map;

import com.nexus.exception.UnknownCommandException;
import com.nexus.model.Command;
import com.nexus.service.core.ProjectService;
import com.nexus.service.core.ReportService;
import com.nexus.service.core.TaskService;
import com.nexus.service.core.UserService;
import com.nexus.service.core.WorkflowService;

public class CommandFactory {
    private Map<String, CommandHandler> handlers = new HashMap<>();

    public CommandFactory(UserService userService, ProjectService projectService, TaskService taskService,
            WorkflowService workflowService, ReportService reportService) {
        handlers.put("CREATE_USER", new CreateUserHandler(userService));
        handlers.put("CREATE_PROJECT", new CreateProjectHandler(projectService));
        handlers.put("CREATE_TASK", new CreateTaskHandler(taskService));
        handlers.put("ASSIGN_USER", new AssignUserHandler(taskService));
        handlers.put("CREATE_STATUS", new CreateStatusHandler(workflowService));
        handlers.put("CHANGE_STATUS", new ChangeStatusHandler(taskService));
        handlers.put("SET_INITIAL_STATUS", new SetInitialStatusHandler(workflowService));
        handlers.put("CREATE_WORKFLOW", new CreateWorkflowHandler(workflowService));
        handlers.put("ALLOW_TRANSITION", new AllowTransitionHandler(workflowService));
        handlers.put("LINK_WORKFLOW", new LinkWorkflowHandler(projectService));
        handlers.put("ADD_MEMBER", new AddMemberHandler(projectService));
        handlers.put("REPORT_STATUS", new ReportStatusHandler(reportService));
        handlers.put("TOP_PERFORMERS", new TopPerformersHandler(reportService));
        handlers.put("OVERLOADED_USERS", new OverloadedUsersHandler(reportService));
        handlers.put("PROJECT_HEALTH", new ProjectHealthHandler(reportService));
        handlers.put("GLOBAL_BOTTLENECK", new GlobalBottleneckHandler(reportService));
        handlers.put("LIST_TASKS", new ListTasksHandler(reportService));
        handlers.put("CREATE_SPRINT", new CreateSprintHandler(projectService));
        handlers.put("ADD_TASK_TO_SPRINT", new AddTaskToSprintHandler(projectService));
    }

    public CommandHandler getHandler(Command command) {
        String key = command.getAction();
        CommandHandler handler = handlers.get(key);
        if (handler == null) {
            throw new UnknownCommandException("Comando '" + key + "' não encontrado.");
        }
        return handler;
    }
}