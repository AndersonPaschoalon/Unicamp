package com.nexus.view;

import com.nexus.service.query.WorkspaceQueryServiceInterface;

public abstract class AbstractNexusView implements ViewState {

    protected final WorkspaceQueryServiceInterface queryService;

    protected final ViewState parentView;

    public AbstractNexusView(WorkspaceQueryServiceInterface queryService, ViewState parentView) {
        this.queryService = queryService;
        this.parentView = parentView;
    }

    protected void printHeader(String title) {
        System.out.println("\n========================================");
        System.out.println("   NEXUS KANBAN - " + title.toUpperCase());
        System.out.println("========================================");
    }

    protected ViewState handleGlobalCommands(String input) {
        if ("0".equals(input) || "VOLTAR".equalsIgnoreCase(input)) {

            return this.parentView;
        }
        return null;
    }
}