package com.nexus.view.statesgui.menus;

import com.nexus.model.valueobject.Role;
import com.nexus.view.NexusGUI;
import com.nexus.view.WebSessionContext;
import com.nexus.view.statesgui.VaadinViewState;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class MainMenuGuiState implements VaadinViewState {

    private final NexusGUI mainView;

    public MainMenuGuiState(NexusGUI mainView) {
        this.mainView = mainView;
    }

    @Override
    public void render(HasComponents container) {
        WebSessionContext session = WebSessionContext.current();
        if (session == null) {
            mainView.navigateTo(new LoginState(mainView));
            return;
        }

        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        layout.add(new H2("Nexus — Menu Principal"));
        layout.add(new Span("Logado como: " + session.getUsername() + " (" + session.getRole() + ")"));

        Button projetosBtn = new Button("Projetos", e -> mainView.navigateTo(new ProjectMenuState(mainView, this)));
        Button relatoriosBtn = new Button("Relatórios", e -> mainView.navigateTo(new ReportMenuState(mainView, this)));
        projetosBtn.setWidth("200px");
        relatoriosBtn.setWidth("200px");
        layout.add(projetosBtn, relatoriosBtn);

        if (session.hasAtLeastRole(Role.MANAGER)) {
            Button workflowsBtn = new Button("Workflows",
                    e -> mainView.navigateTo(new WorkflowManagementState(mainView, this)));
            workflowsBtn.setWidth("200px");
            layout.add(workflowsBtn);

            Button usuariosBtn = new Button("+ Novo Usuário",
                    e -> mainView.navigateTo(new com.nexus.view.statesgui.info.AddUserState(mainView, this)));
            usuariosBtn.setWidth("200px");
            layout.add(usuariosBtn);
        }

        Button sairBtn = new Button("Sair", e -> {
            WebSessionContext.clear();
            mainView.navigateTo(new LoginState(mainView));
        });
        sairBtn.setWidth("200px");
        layout.add(sairBtn);

        container.add(layout);
    }
}
