package com.nexus.view;

import java.util.List;

import com.nexus.app.AppContext;
import com.nexus.controller.CommandDispatcher;
import com.nexus.service.query.WorkspaceQueryServiceInterface;
import com.nexus.view.handlers.AddMemberCLIHandler;
import com.nexus.view.handlers.AllowTransitionCLIHandler;
import com.nexus.view.handlers.AssignUserCLIHandler;
import com.nexus.view.handlers.BottleneckCLIHandler;
import com.nexus.view.handlers.ChangeStatusCLIHandler;
import com.nexus.view.handlers.CreateProjectCLIHandler;
import com.nexus.view.handlers.CreateStatusCLIHandler;
import com.nexus.view.handlers.CreateTaskCLIHandler;
import com.nexus.view.handlers.CreateUserCLIHandler;
import com.nexus.view.handlers.CreateWorkflowCLIHandler;
import com.nexus.view.handlers.GenerateReportCLIHandler;
import com.nexus.view.handlers.LinkWorkflowCLIHandler;
import com.nexus.view.handlers.ListProjectsCLIHandler;
import com.nexus.view.handlers.ListStatusCLIHandler;
import com.nexus.view.handlers.ListTasksCLIHandler;
import com.nexus.view.handlers.ListUsersCLIHandler;
import com.nexus.view.handlers.ListWorkflowsCLIHandler;
import com.nexus.view.handlers.OverloadedUsersCLIHandler;
import com.nexus.view.handlers.ProjectHealthCLIHandler;
import com.nexus.view.handlers.ReportStatusCLIHandler;
import com.nexus.view.handlers.SetInitialStatusCLIHandler;
import com.nexus.view.handlers.TopPerformersCLIHandler;
import com.nexus.view.handlers.ViewKanbanCLIHandler;;

public class CLIHandlerRegistry {

    /**
     * Cria e retorna a lista de handlers do CLI, passando o contexto da aplicação
     * para os handlers que precisam dos serviços para funcionar.
     * 
     * @param context
     * @return
     */
    public static List<InteractiveCLIHandler> create(AppContext context) {

        CommandDispatcher dispatcher = context.getDispatcher();
        WorkspaceQueryServiceInterface queryService = context.getQueryService();

        return List.of(
                // User services
                new CreateUserCLIHandler(dispatcher),

                // Workflow Services
                new CreateStatusCLIHandler(dispatcher),
                new CreateWorkflowCLIHandler(dispatcher),
                new SetInitialStatusCLIHandler(dispatcher, queryService),
                new AllowTransitionCLIHandler(dispatcher, queryService),

                // Task Services
                new AssignUserCLIHandler(dispatcher, context.getQueryService()),
                new ChangeStatusCLIHandler(dispatcher, queryService),
                new CreateTaskCLIHandler(dispatcher, queryService),

                // Project Services
                new CreateProjectCLIHandler(dispatcher),
                new LinkWorkflowCLIHandler(dispatcher, queryService),
                new AddMemberCLIHandler(dispatcher, queryService),

                // Report Services
                new GenerateReportCLIHandler(dispatcher),
                new ReportStatusCLIHandler(dispatcher),
                new TopPerformersCLIHandler(dispatcher),
                new ProjectHealthCLIHandler(dispatcher),
                new BottleneckCLIHandler(dispatcher),
                new OverloadedUsersCLIHandler(dispatcher),
                new ViewKanbanCLIHandler(dispatcher, queryService),

                // List commands
                new ListProjectsCLIHandler(queryService),
                new ListStatusCLIHandler(queryService),
                new ListTasksCLIHandler(queryService),
                new ListUsersCLIHandler(queryService),
                new ListWorkflowsCLIHandler(queryService));
    }
}
