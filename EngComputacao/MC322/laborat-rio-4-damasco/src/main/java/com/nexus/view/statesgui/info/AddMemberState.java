package com.nexus.view.statesgui.info;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.model.Command;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.view.NexusGUI;
import com.nexus.view.WebSessionContext;
import com.nexus.view.statesgui.VaadinViewState;
import com.nexus.view.statesgui.menus.LoginState;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


public class AddMemberState implements VaadinViewState {

    private final NexusGUI mainView;
    private final VaadinViewState parent;
    private final String fixedProjectName;

    public AddMemberState(NexusGUI mainView, VaadinViewState parent, String fixedProjectName) {
        this.mainView = mainView;
        this.parent = parent;
        this.fixedProjectName = fixedProjectName;
    }

    @Override
    public void render(HasComponents container) {
        WebSessionContext session = WebSessionContext.current();
        if (session == null) {
            mainView.navigateTo(new LoginState(mainView));
            return;
        }

        WorkspaceQueryService queryService = WorkspaceQueryServiceImpl.getInstance();

        VerticalLayout form = new VerticalLayout();
        form.setWidth("400px");
        form.add(new H3("Adicionar Membro ao Projeto"));

        ComboBox<String> projectField = new ComboBox<>("Projeto");
        if (fixedProjectName != null) {
            projectField.setItems(fixedProjectName);
            projectField.setValue(fixedProjectName);
            projectField.setEnabled(false);
        } else {
            projectField.setItems(queryService.listProjectNames());
        }
        projectField.setWidthFull();

        ComboBox<String> usernameField = new ComboBox<>("Username");
        usernameField.setItems(queryService.listUserNames());
        usernameField.setWidthFull();

        ComboBox<String> roleField = new ComboBox<>("Role no Projeto");
        roleField.setItems(queryService.listRoles());
        roleField.setWidthFull();

        Button salvarBtn = new Button("Adicionar", e -> {
            String projeto = projectField.getValue();
            String username = usernameField.getValue();
            String role = roleField.getValue();

            if (projeto == null || username == null || role == null) {
                Notification.show("Preencha todos os campos.", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                Command command = new Command(null, "ADD_MEMBER", new String[] { projeto, username, role });
                CommandDispatcher.getInstance().dispatchCommand(command);
                Notification.show("Membro adicionado com sucesso!");
                mainView.navigateTo(parent);
            } catch (NexusDomainException ex) {
                Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });

        Button cancelarBtn = new Button("Cancelar", e -> mainView.navigateTo(parent));

        form.add(projectField, usernameField, roleField, salvarBtn, cancelarBtn);
        container.add(form);
    }
}
