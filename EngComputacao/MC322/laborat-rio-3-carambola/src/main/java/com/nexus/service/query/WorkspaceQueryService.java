package com.nexus.service.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nexus.model.entity.Project;
import com.nexus.model.entity.Status;
import com.nexus.model.entity.Task;
import com.nexus.model.entity.User;
import com.nexus.model.Workflow;
import com.nexus.model.valueobject.Role;
import com.nexus.service.Workspace;

import com.nexus.service.core.UserService;

public class WorkspaceQueryService
        implements WorkspaceQueryServiceInterface {

    private final Workspace workspace;

    private final UserService userService;

    public WorkspaceQueryService(
            Workspace workspace,
            UserService userService) {

        this.workspace = workspace;
        this.userService = userService;
    }

    @Override
    public List<String> listUserNames() {
        return userService.getUsers()
                .stream()
                .map(User::consultUsername)
                .toList();
    }

    @Override
    public List<String> listProjectNames() {
        return workspace.getProjects()
                .stream()
                .map(Project::getName)
                .toList();
    }

    @Override
    public List<String> listRoles() {
        return Arrays.stream(Role.values())
                .map(Enum::name)
                .toList();
    }

    @Override
    public List<String> listWorkflowNames() {
        return workspace.getWorkflows()
                .stream()
                .map(Workflow::getName)
                .toList();
    }

    @Override
    public List<String> listStatusNames() {
        return workspace.getStatuses()
                .stream()
                .map(Status::getName)
                .toList();
    }

    @Override
    public List<String> listTaskIds() {
        return workspace.getTasks()
                .stream()
                .map(Task::getId)
                .toList();
    }

    @Override
    public List<String> listStatusesFromWorkflow(String workflowName) {

        Workflow workflow = workspace.getWorkflowByName(workflowName);

        return workflow.getAllStatuses()
                .stream()
                .map(Status::getName)
                .toList();
    }

    @Override
    public List<String> listAllowedTransitions(String taskId) {

        Task task = workspace.getTaskById(taskId);

        return workspace.getAllowedTransitions(task)
                .stream()
                .map(Status::getName)
                .toList();
    }

    @Override
    public List<String> listProjectTaskIds(String projectName) {
        return workspace.getTasksFromProject(projectName)
                .stream()
                .map(Task::getId)
                .toList();
    }

    @Override
    public String getWorkflowByProject(String projectName) {

        return workspace.getWorkflowFromProject(projectName).getName();
    }

    @Override
    public Project getProjectByName(String projectName) {
        return workspace.getProjectByName(projectName);
    }

   @Override
    public List<Task> findTasksByProjectAndStatus(Project project, String statusTarget) {
        if (project == null || statusTarget == null) {
            return List.of();
        }

        return this.workspace.getTasks()
                .stream()
                .filter(project::isTaskInProject)
                .filter(task -> task.getStatus() != null 
                        && task.getStatus().getName() != null 
                        && task.getStatus().getName().trim().equalsIgnoreCase(statusTarget.trim()))
                .toList();
    }

    @Override
    public Task getTaskById(String id) {
        return workspace.getTaskById(id);
    }

    @Override
    public Map<String, List<String>> listTasksGroupedByProject() {

        return workspace.getProjects()
                .stream()
                .collect(Collectors.toMap(
                        Project::getName,
                        project -> project.getTasks()
                                .stream()
                                .map(task -> task.getId() + " - " + task.getTitle())
                                .toList()));
    }

    @Override
    public Map<String, List<String>> listUsersWithRolesByProject() {

        return workspace.getProjects()
                .stream()
                .collect(Collectors.toMap(
                        Project::getName,
                        project -> project.getTeamMembers()
                                .entrySet()
                                .stream()
                                .map(entry -> "[" + entry.getValue().name() + "] "
                                        + entry.getKey().consultUsername())
                                .toList()));
    }

}