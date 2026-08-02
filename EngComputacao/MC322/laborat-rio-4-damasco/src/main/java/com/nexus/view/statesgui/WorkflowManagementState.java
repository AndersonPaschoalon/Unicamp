package com.nexus.view.statesgui.menus;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.model.Command;
import com.nexus.model.valueobject.Role;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.view.NexusGUI;
import com.nexus.view.WebSessionContext;
import com.nexus.view.statesgui.VaadinViewState;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;


public class WorkflowManagementState implements VaadinViewState {

    private final NexusGUI mainView;
    private final VaadinViewState parent;

    public WorkflowManagementState(NexusGUI mainView, VaadinViewState parent) {
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
            Notification.show("Apenas MANAGER pode gerenciar workflows.", 3000, Notification.Position.MIDDLE);
            mainView.navigateTo(parent);
            return;
        }

        WorkspaceQueryService queryService = WorkspaceQueryServiceImpl.getInstance();

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H2("Gerenciar Workflows"));
        layout.add(new Span("Workflows: " + queryService.listWorkflowNames()));
        layout.add(new Span("Status disponíveis: " + queryService.listStatusNames()));

        layout.add(criarWorkflowForm());
        layout.add(vincularWorkflowForm());
        layout.add(statusInicialForm());
        layout.add(transicaoForm());

        Button voltarBtn = new Button("Voltar", e -> mainView.navigateTo(parent));
        layout.add(voltarBtn);

        container.add(layout);
    }

    private VerticalLayout criarWorkflowForm() {
        VerticalLayout form = new VerticalLayout();
        form.add(new H3("Criar Novo Workflow"));

        TextField nomeField = new TextField("Nome do Workflow");
        Button criarBtn = new Button("Criar", e -> {
            String nome = nomeField.getValue().trim();
            if (nome.isEmpty()) {
                Notification.show("Informe o nome do workflow.", 3000, Notification.Position.MIDDLE);
                return;
            }
            dispatchAndReport("CREATE_WORKFLOW", new String[] { nome }, "Workflow criado com sucesso!");
        });

        form.add(nomeField, criarBtn);
        return form;
    }

    private VerticalLayout vincularWorkflowForm() {
        VerticalLayout form = new VerticalLayout();
        form.add(new H3("Vincular Workflow a um Projeto"));

        TextField projetoField = new TextField("Nome do Projeto");
        TextField workflowField = new TextField("Nome do Workflow");
        Button vincularBtn = new Button("Vincular", e -> {
            String projeto = projetoField.getValue().trim();
            String workflow = workflowField.getValue().trim();
            if (projeto.isEmpty() || workflow.isEmpty()) {
                Notification.show("Preencha todos os campos.", 3000, Notification.Position.MIDDLE);
                return;
            }
            dispatchAndReport("LINK_WORKFLOW", new String[] { projeto, workflow },
                    "Workflow vinculado ao projeto com sucesso!");
        });

        form.add(projetoField, workflowField, vincularBtn);
        return form;
    }

    private VerticalLayout statusInicialForm() {
        VerticalLayout form = new VerticalLayout();
        form.add(new H3("Definir Status Inicial"));

        TextField workflowField = new TextField("Nome do Workflow");
        TextField statusField = new TextField("Nome do Status Inicial");
        Button definirBtn = new Button("Definir", e -> {
            String workflow = workflowField.getValue().trim();
            String status = statusField.getValue().trim();
            if (workflow.isEmpty() || status.isEmpty()) {
                Notification.show("Preencha todos os campos.", 3000, Notification.Position.MIDDLE);
                return;
            }
            dispatchAndReport("SET_INITIAL_STATUS", new String[] { workflow, status },
                    "Status inicial definido com sucesso!");
        });

        form.add(workflowField, statusField, definirBtn);
        return form;
    }

    private VerticalLayout transicaoForm() {
        VerticalLayout form = new VerticalLayout();
        form.add(new H3("Adicionar Transição"));

        TextField workflowField = new TextField("Nome do Workflow");
        TextField origemField = new TextField("Status de Origem");
        TextField destinoField = new TextField("Status de Destino");
        Button adicionarBtn = new Button("Adicionar", e -> {
            String workflow = workflowField.getValue().trim();
            String origem = origemField.getValue().trim();
            String destino = destinoField.getValue().trim();
            if (workflow.isEmpty() || origem.isEmpty() || destino.isEmpty()) {
                Notification.show("Preencha todos os campos.", 3000, Notification.Position.MIDDLE);
                return;
            }
            dispatchAndReport("ALLOW_TRANSITION", new String[] { workflow, origem, destino },
                    "Transição registrada com sucesso!");
        });

        form.add(workflowField, origemField, destinoField, adicionarBtn);
        return form;
    }

    private void dispatchAndReport(String action, String[] params, String successMessage) {
        try {
            CommandDispatcher.getInstance().dispatchCommand(new Command(null, action, params));
            Notification.show(successMessage);
            mainView.navigateTo(new WorkflowManagementState(mainView, parent));
        } catch (NexusDomainException e) {
            Notification.show("Erro: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }
}
