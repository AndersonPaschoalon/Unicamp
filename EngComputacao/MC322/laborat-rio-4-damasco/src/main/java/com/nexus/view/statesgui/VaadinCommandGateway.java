package com.nexus.view.statesgui;

import com.nexus.controller.CommandDispatcher;
import com.nexus.exception.NexusDomainException;
import com.nexus.model.Command;
import com.vaadin.flow.component.notification.Notification;


public final class VaadinCommandGateway {
    private VaadinCommandGateway() {
    }

    public static void dispatch(Command command, Runnable onSuccess) {
        try {
            CommandDispatcher.getInstance().dispatchCommand(command);
            onSuccess.run();
        } catch (NexusDomainException e) {
            Notification.show(e.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }
}
