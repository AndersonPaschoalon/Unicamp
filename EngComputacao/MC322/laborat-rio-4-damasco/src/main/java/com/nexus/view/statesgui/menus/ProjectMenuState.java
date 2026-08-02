package com.nexus.view.statesgui.menus;

import java.util.List;

import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.view.NexusGUI;
import com.nexus.view.WebSessionContext;
import com.nexus.view.statesgui.VaadinViewState;
import com.nexus.view.statesgui.info.AddProjectState;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ProjectMenuState implements VaadinViewState {

    private final NexusGUI mainView;
    private final VaadinViewState parent;

    public ProjectMenuState(NexusGUI mainView, VaadinViewState parent) {
        this.mainView = mainView;
        this.parent = parent;
    }

    @Override
    public void render(HasComponents container) {
        WebSessionContext session = WebSessionContext.current();
        if (session == null) {
            mainView.navigateTo(new LoginState(mainView));
            return;
        }

        WorkspaceQueryService queryService = WorkspaceQueryServiceImpl.getInstance();

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H2("Projetos"));

        List<String> projectNames = queryService.listProjectNames();
        if (projectNames.isEmpty()) {
            layout.add(new Span("Nenhum projeto encontrado."));
        } else {
            for (String projectName : projectNames) {
                Button projectBtn = new Button(projectName, e -> mainView
                        .navigateTo(new KanbanBoardState(mainView, this, projectName)));
                projectBtn.setWidth("300px");
                layout.add(projectBtn);
            }
        }

        Button addProjectBtn = new Button("+ Novo Projeto", e -> mainView.navigateTo(new AddProjectState(mainView, this)));
        Button voltarBtn = new Button("Voltar", e -> mainView.navigateTo(parent));

        layout.add(addProjectBtn, voltarBtn);
        container.add(layout);
    }
}
