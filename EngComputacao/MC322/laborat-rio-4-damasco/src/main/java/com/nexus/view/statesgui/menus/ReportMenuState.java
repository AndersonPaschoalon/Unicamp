package com.nexus.view.statesgui.menus;

import com.nexus.controller.CommandDispatcher;
import com.nexus.model.Command;
import com.nexus.view.NexusGUI;
import com.nexus.view.WebSessionContext;
import com.nexus.view.statesgui.VaadinViewState;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


public class ReportMenuState implements VaadinViewState {

    private final NexusGUI mainView;
    private final VaadinViewState parent;
    private Pre resultado;

    public ReportMenuState(NexusGUI mainView, VaadinViewState parent) {
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

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H2("Relatórios"));

        HorizontalLayout botoes = new HorizontalLayout();
        botoes.add(new Button("Top Performers", e -> runReport("TOP_PERFORMERS")));
        botoes.add(new Button("Usuários Sobrecarregados", e -> runReport("OVERLOADED_USERS")));
        botoes.add(new Button("Saúde do Projeto", e -> runReport("PROJECT_HEALTH")));
        botoes.add(new Button("Gargalo Global", e -> runReport("GLOBAL_BOTTLENECK")));
        botoes.add(new Button("Relatório Completo", e -> runReport("REPORT_STATUS")));
        layout.add(botoes);

        resultado = new Pre("Selecione um relatório acima.");
        resultado.getStyle()
                .set("border", "1px solid #ccc")
                .set("padding", "10px")
                .set("min-height", "150px")
                .set("white-space", "pre-wrap");
        layout.add(resultado);

        Button voltarBtn = new Button("Voltar", e -> mainView.navigateTo(parent));
        layout.add(voltarBtn);

        container.add(layout);
    }

    private void runReport(String action) {
        Command command = new Command(null, action, new String[] {});
        String output = CommandDispatcher.getInstance().dispatchCommand(command);
        resultado.setText(output != null ? output : "(sem retorno)");
    }
}
