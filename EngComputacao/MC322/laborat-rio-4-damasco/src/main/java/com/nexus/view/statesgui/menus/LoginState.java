package com.nexus.view.statesgui.menus;

import com.nexus.model.valueobject.Role;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.view.NexusGUI;
import com.nexus.view.WebSessionContext;
import com.nexus.view.statesgui.VaadinViewState;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class LoginState implements VaadinViewState {

    private final NexusGUI mainView;

    public LoginState(NexusGUI mainView) {
        this.mainView = mainView;
    }

    @Override
    public void render(HasComponents container) {
        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        layout.add(new H2("Nexus — Login"));
        layout.add(new Span("Digite seu username para acessar o sistema."));

        TextField usernameField = new TextField("Username");
        usernameField.setWidth("300px");
        usernameField.setPlaceholder("ex: joao.silva");

        Button entrarBtn = new Button("Entrar", e -> realizarLogin(usernameField.getValue().trim()));

        usernameField.addKeyPressListener(
                Key.ENTER,
                ev -> realizarLogin(usernameField.getValue().trim()));

        layout.add(usernameField, entrarBtn);
        container.add(layout);
    }

    private void realizarLogin(String username) {
        if (username.isEmpty()) {
            Notification.show("O username não pode estar vazio.", 3000, Notification.Position.MIDDLE);
            return;
        }

        WorkspaceQueryService queryService = WorkspaceQueryServiceImpl.getInstance();
        if (!queryService.listUserNames().contains(username)) {
            Notification.show("Usuário '" + username + "' não encontrado.", 3000, Notification.Position.MIDDLE);
            return;
        }

        Role role = Role.valueOf(queryService.getUserRoleName(username));
        WebSessionContext.set(new WebSessionContext(username, role));
        mainView.navigateTo(new MainMenuGuiState(mainView));
    }
}
