package com.nexus.view.statesgui.info;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.model.Command;
import com.nexus.model.valueobject.Role;
import com.nexus.view.NexusGUI;
import com.nexus.view.WebSessionContext;
import com.nexus.view.statesgui.VaadinViewState;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class AddProjectState implements VaadinViewState {

    private final NexusGUI mainView;
    private final VaadinViewState parent;

    public AddProjectState(NexusGUI mainView, VaadinViewState parent) {
        this.mainView = mainView;
        this.parent = parent;
    }

    @Override
    public void render(HasComponents container) {
        VerticalLayout form = new VerticalLayout();
        form.setWidth("400px");
        form.add(new H3("Novo Projeto"));

        TextField nameField = new TextField("Nome do Projeto");
        nameField.setWidthFull();
        nameField.setPlaceholder("ex: Nexus Backend");

        TextField orcamentoField = new TextField("Orçamento Total (horas)");
        orcamentoField.setWidthFull();
        orcamentoField.setPlaceholder("ex: 200");

        Button saveBtn = new Button("Salvar", e -> {
            WebSessionContext session = WebSessionContext.current();
            if (session == null || !session.hasAtLeastRole(Role.MANAGER)) {
                Notification.show("Apenas MANAGER pode criar projetos.", 3000, Notification.Position.MIDDLE);
                return;
            }

            String nome = nameField.getValue().trim();
            String orcamento = orcamentoField.getValue().trim();

            if (nome.isEmpty() || orcamento.isEmpty()) {
                Notification.show("Preencha todos os campos.", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                Command command = new Command(null, "CREATE_PROJECT", new String[] { nome, orcamento });
                CommandDispatcher.getInstance().dispatchCommand(command);
                Notification.show("Projeto '" + nome + "' criado com sucesso!");
                mainView.navigateTo(parent);
            } catch (NexusDomainException ex) {
                Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });

        Button cancelBtn = new Button("Cancelar", e -> mainView.navigateTo(parent));

        form.add(nameField, orcamentoField, saveBtn, cancelBtn);
        container.add(form);
    }
}
