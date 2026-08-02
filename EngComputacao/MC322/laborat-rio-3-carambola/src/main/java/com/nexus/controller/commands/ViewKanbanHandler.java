package com.nexus.controller.commands;


import com.nexus.controller.CommandHandler;
import com.nexus.model.Command;
import com.nexus.service.Workspace;
import com.nexus.service.query.WorkspaceQueryServiceInterface;


public class ViewKanbanHandler implements CommandHandler{

    private final WorkspaceQueryServiceInterface queryService;

    public ViewKanbanHandler(WorkspaceQueryServiceInterface queryService){
        this.queryService = queryService;
    }

    @Override
    public void handle(Command command, Workspace workspace) {
        
        this.requireParamCount(command.getParameters(), 0, "VIEW_KANBAN");

    }

}
