package com.nexus.view.statesgui.menus;

import java.util.List;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.model.Command;
import com.nexus.service.WorkspaceQueryService;
import com.nexus.service.WorkspaceQueryServiceImpl;
import com.nexus.view.NexusGUI;
import com.nexus.view.WebSessionContext;
import com.nexus.view.statesgui.VaadinViewState;
import com.nexus.view.statesgui.info.AddTaskState;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class KanbanBoardState implements VaadinViewState {

    private final NexusGUI mainView;
    private final VaadinViewState parent;
    private final String projectName;

    public KanbanBoardState(NexusGUI mainView, VaadinViewState parent, String projectName) {
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

        WorkspaceQueryService queryService = WorkspaceQueryServiceImpl.getInstance();

        VerticalLayout page = new VerticalLayout();
        page.setSizeFull();
        page.add(new H2("Kanban: " + projectName));

        List<String> statusList = queryService.listStatusNamesByProject(projectName);

        if (statusList.isEmpty()) {
            page.add(new Span(
                    "Este projeto não possui um workflow vinculado. Vincule um workflow pela tela de Workflows antes de usar o Kanban."));
        } else {
            HorizontalLayout colunas = new HorizontalLayout();
            colunas.setWidthFull();
            colunas.setAlignItems(FlexComponent.Alignment.START);

            for (String statusName : statusList) {
                VerticalLayout coluna = new VerticalLayout();
                coluna.setWidth("220px");
                coluna.add(new H3(statusName));

                for (String taskId : queryService.listTaskIdsByProjectAndStatus(projectName, statusName)) {
                    String title = queryService.getTaskTitle(taskId);
                    String owner = queryService.getTaskOwnerName(taskId);
                    boolean overdue = queryService.isTaskOverdue(taskId);

                    Div card = new Div();
                    StringBuilder texto = new StringBuilder(title);
                    if (!"Não atribuído".equals(owner)) {
                        texto.append(" (").append(owner).append(")");
                    }
                    if (overdue) {
                        texto.append(" [ATRASADA]");
                    }
                    card.setText(texto.toString());
                    card.getStyle()
                            .set("border", "1px solid #ccc")
                            .set("padding", "6px")
                            .set("margin", "4px")
                            .set("cursor", "pointer");

                    card.addClickListener(ev -> abrirTransicao(taskId));
                    coluna.add(card);
                }

                colunas.add(coluna);
            }
            page.add(colunas);
        }

        HorizontalLayout acoes = new HorizontalLayout();
        Button adicionarTarefaBtn = new Button("+ Nova Tarefa",
                e -> mainView.navigateTo(new AddTaskState(mainView, this, projectName)));
        Button adicionarMembroBtn = new Button("+ Membro",
                e -> mainView.navigateTo(
                        new com.nexus.view.statesgui.info.AddMemberState(mainView, this, projectName)));
        Button sprintsBtn = new Button("Sprints",
                e -> mainView.navigateTo(new SprintManagementState(mainView, this, projectName)));
        Button voltarBtn = new Button("Voltar", e -> mainView.navigateTo(parent));
        acoes.add(adicionarTarefaBtn, adicionarMembroBtn, sprintsBtn, voltarBtn);
        page.add(acoes);

        container.add(page);
    }

    private void abrirTransicao(String taskId) {
        WorkspaceQueryService queryService = WorkspaceQueryServiceImpl.getInstance();
        List<String> permitidas = queryService.listAllowedTransitions(taskId);

        Dialog dialog = new Dialog();
        dialog.add(new H3("Mover: " + queryService.getTaskTitle(taskId)));

        if (permitidas.isEmpty()) {
            dialog.add(new Span("Nenhuma transição disponível para esta tarefa."));
        }
        for (String novoStatus : permitidas) {
            Button opcao = new Button(novoStatus, e -> {
                WebSessionContext session = WebSessionContext.current();
                try {
                    Command command = new Command(null, "CHANGE_STATUS",
                            new String[] { taskId, novoStatus, session.getUsername() });
                    CommandDispatcher.getInstance().dispatchCommand(command);
                    dialog.close();
                    mainView.navigateTo(new KanbanBoardState(mainView, parent, projectName));
                } catch (NexusDomainException ex) {
                    Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                }
            });
            dialog.add(opcao);
        }

        Button fechar = new Button("Fechar", e -> dialog.close());
        dialog.add(fechar);
        dialog.open();
    }
}
