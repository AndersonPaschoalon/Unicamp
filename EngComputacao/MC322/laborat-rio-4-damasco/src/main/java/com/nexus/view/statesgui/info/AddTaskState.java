package com.nexus.view.statesgui.info;

import java.time.LocalDate;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.model.Command;
import com.nexus.view.NexusGUI;
import com.nexus.view.WebSessionContext;
import com.nexus.view.statesgui.VaadinViewState;
import com.nexus.view.statesgui.menus.LoginState;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class AddTaskState implements VaadinViewState {

    private final NexusGUI mainView;
    private final VaadinViewState parent;
    private final String projectName;

    public AddTaskState(NexusGUI mainView, VaadinViewState parent, String projectName) {
        this.mainView = mainView;
        this.parent = parent;
        this.projectName = projectName;
    }

    @Override
    public void render(HasComponents container) {
        // Só exige login para impedir acesso direto sem sessão; criar tarefa não
        // exige um gate de Role específico (nível CONTRIBUTOR/qualquer logado).
        WebSessionContext session = WebSessionContext.current();
        if (session == null) {
            mainView.navigateTo(new LoginState(mainView));
            return;
        }

        VerticalLayout form = new VerticalLayout();
        form.setWidth("400px");
        form.add(new H3("Nova Tarefa — " + projectName));

        TextField tituloField = new TextField("Título da Tarefa");
        tituloField.setWidthFull();
        tituloField.setPlaceholder("ex: Implementar endpoint de login");

        DatePicker prazoField = new DatePicker("Prazo");
        prazoField.setMin(LocalDate.now());
        prazoField.setWidthFull();

        TextField esforcoField = new TextField("Esforço Estimado (horas)");
        esforcoField.setWidthFull();
        esforcoField.setPlaceholder("ex: 8");

        DatePicker dueDateField = new DatePicker("Data de Vencimento (opcional)");
        dueDateField.setWidthFull();

        Button salvarBtn = new Button("Criar Tarefa", e -> {
            String titulo = tituloField.getValue().trim();
            LocalDate prazo = prazoField.getValue();
            String esforco = esforcoField.getValue().trim();
            LocalDate dueDate = dueDateField.getValue();

            if (titulo.isEmpty() || prazo == null || esforco.isEmpty()) {
                Notification.show("Preencha os campos obrigatórios.", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                String[] params = (dueDate != null)
                        ? new String[] { titulo, prazo.toString(), esforco, projectName, dueDate.toString() }
                        : new String[] { titulo, prazo.toString(), esforco, projectName };
                Command command = new Command(null, "CREATE_TASK", params);
                CommandDispatcher.getInstance().dispatchCommand(command);
                Notification.show("Tarefa criada com sucesso!");
                mainView.navigateTo(parent);
            } catch (NexusDomainException ex) {
                Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });

        Button cancelarBtn = new Button("Cancelar", e -> mainView.navigateTo(parent));

        form.add(tituloField, prazoField, esforcoField, dueDateField, salvarBtn, cancelarBtn);
        container.add(form);
    }
}
