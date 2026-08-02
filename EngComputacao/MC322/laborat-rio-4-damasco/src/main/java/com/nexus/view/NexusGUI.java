package com.nexus.view;

import com.nexus.view.statesgui.VaadinViewState;
import com.nexus.view.statesgui.menus.LoginState;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("nexus") 
public class NexusGUI extends VerticalLayout {

    private VaadinViewState currentState;

    public NexusGUI() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        navigateTo(new LoginState(this));
    }

    public void navigateTo(VaadinViewState nextState) {
        this.currentState = nextState;
        this.removeAll(); 
        this.currentState.render(this); 
    }
}
