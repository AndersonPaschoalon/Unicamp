package com.nexus.view.statesgui.info;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.model.Command;
import com.nexus.model.valueobject.Role;
import com.nexus.view.NexusGUI;
import com.nexus.view.WebSessionContext;
import com.nexus.view.statesgui.VaadinViewState;
import com.nexus.view.statesgui.menus.LoginState;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;


public class AddUserState implements VaadinViewState {

    private final NexusGUI mainView;
    private final VaadinViewState parent;

    public AddUserState(NexusGUI mainView, VaadinViewState parent) {
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
        if (!session.hasAtLeastRole(Role.MANAGER)) {
            Notification.show("Apenas MANAGER pode criar novos usuários.", 3000, Notification.Position.MIDDLE);
            mainView.navigateTo(parent);
            return;
        }

        VerticalLayout form = new VerticalLayout();
        form.setWidth("400px");
        form.add(new H3("Novo Usuário"));

        TextField usernameField = new TextField("Username");
        usernameField.setWidthFull();

        TextField emailField = new TextField("Email");
        emailField.setWidthFull();

        Button salvarBtn = new Button("Salvar", e -> {
            String username = usernameField.getValue().trim();
            String email = emailField.getValue().trim();

            if (username.isEmpty() || email.isEmpty()) {
                Notification.show("Preencha todos os campos.", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                Command command = new Command(null, "CREATE_USER", new String[] { username, email });
                CommandDispatcher.getInstance().dispatchCommand(command);
                Notification.show("Usuário criado com sucesso!");
                mainView.navigateTo(parent);
            } catch (NexusDomainException ex) {
                Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });

        Button cancelarBtn = new Button("Cancelar", e -> mainView.navigateTo(parent));

        form.add(usernameField, emailField, salvarBtn, cancelarBtn);
        container.add(form);
    }
}
