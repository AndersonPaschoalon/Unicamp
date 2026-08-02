package com.nexus.view.statesgui.menus;

import java.time.LocalDate;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.model.Command;
import com.nexus.model.valueobject.Role;
import com.nexus.view.NexusGUI;
import com.nexus.view.WebSessionContext;
import com.nexus.view.statesgui.VaadinViewState;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;


public class SprintManagementState implements VaadinViewState {

    private final NexusGUI mainView;
    private final VaadinViewState parent;
    private final String projectName;

    public SprintManagementState(NexusGUI mainView, VaadinViewState parent, String projectName) {
        this.mainView = mainView;
        this.parent = parent;
        this.projectName = projectName;
    }

    @Override
    public void render(HasComponents container) {
        WebSessionContext session = WebSessionContext.current();
        if (session == null) {
            mainView.navigateTo(new LoginState(mainView));
            return;
        }
        if (!session.hasAtLeastRole(Role.MANAGER)) {
            Notification.show("Apenas MANAGER pode gerenciar sprints.", 3000, Notification.Position.MIDDLE);
            mainView.navigateTo(parent);
            return;
        }

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H2("Sprints: " + projectName));

        layout.add(criarSprintForm());
        layout.add(addTaskForm());

        Button voltarBtn = new Button("Voltar", e -> mainView.navigateTo(parent));
        layout.add(voltarBtn);

        container.add(layout);
    }

    private VerticalLayout criarSprintForm() {
        VerticalLayout form = new VerticalLayout();
        form.add(new H3("Criar Nova Sprint"));

        TextField nomeField = new TextField("Nome da Sprint");
        DatePicker inicioField = new DatePicker("Data Início");
        DatePicker fimField = new DatePicker("Data Fim");

        Button criarBtn = new Button("Criar", e -> {
            String nome = nomeField.getValue().trim();
            LocalDate inicio = inicioField.getValue();
            LocalDate fim = fimField.getValue();

            if (nome.isEmpty() || inicio == null || fim == null) {
                Notification.show("Preencha todos os campos.", 3000, Notification.Position.MIDDLE);
                return;
            }

            dispatchAndReport("CREATE_SPRINT",
                    new String[] { projectName, nome, inicio.toString(), fim.toString() },
                    "Sprint criada com sucesso!");
        });

        form.add(nomeField, inicioField, fimField, criarBtn);
        return form;
    }

    private VerticalLayout addTaskForm() {
        VerticalLayout form = new VerticalLayout();
        form.add(new H3("Adicionar Tarefa à Sprint"));

        TextField sprintField = new TextField("Nome da Sprint");
        TextField taskIdField = new TextField("ID da Tarefa");

        Button adicionarBtn = new Button("Adicionar", e -> {
            String sprint = sprintField.getValue().trim();
            String taskId = taskIdField.getValue().trim();

            if (sprint.isEmpty() || taskId.isEmpty()) {
                Notification.show("Preencha todos os campos.", 3000, Notification.Position.MIDDLE);
                return;
            }

            dispatchAndReport("ADD_TASK_TO_SPRINT",
                    new String[] { projectName, sprint, taskId },
                    "Tarefa adicionada à sprint com sucesso!");
        });

        form.add(sprintField, taskIdField, adicionarBtn);
        return form;
    }

    private void dispatchAndReport(String action, String[] params, String successMessage) {
        try {
            CommandDispatcher.getInstance().dispatchCommand(new Command(null, action, params));
            Notification.show(successMessage);
            mainView.navigateTo(new SprintManagementState(mainView, parent, projectName));
        } catch (NexusDomainException e) {
            Notification.show("Erro: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }
}
