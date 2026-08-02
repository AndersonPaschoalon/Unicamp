package com.nexus.service.persistence;

import com.nexus.service.Workspace;
import com.nexus.service.core.UserService;

public class NexusData {

    private Workspace workspace;
    private UserService userService;

    public NexusData() {
    }

    public NexusData(Workspace workspace, UserService userService) {
        this.workspace = workspace;
        this.userService = userService;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}