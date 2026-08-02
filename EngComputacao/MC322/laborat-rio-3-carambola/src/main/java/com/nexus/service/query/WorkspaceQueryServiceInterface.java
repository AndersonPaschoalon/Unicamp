package com.nexus.service.query;

import java.util.List;
import java.util.Map;

import com.nexus.model.entity.*;

/**
 * Serviço responsável por consultar de forma read-only informações do workspace
 * em forma de dados primitivos (Strings, números, etc). Usado principalmente
 * para a camada de apresentação (CLI, API, etc) obter informações do workspace
 * sem acoplar-se às entidades de domínio.
 */
public interface WorkspaceQueryServiceInterface {
    // USERS
    List<String> listUserNames();

    // PROJECTS
    List<String> listProjectNames();

    Project getProjectByName(String projectName);

    // TASKS
    List<String> listTaskIds();

    List<String> listProjectTaskIds(String projectName);

    List<Task> findTasksByProjectAndStatus(Project project, String statusTarget);

    Task getTaskById(String id);

    // WORKFLOWS
    List<String> listWorkflowNames();

    String getWorkflowByProject(String projectName);

    // STATUS
    List<String> listStatusNames();

    List<String> listStatusesFromWorkflow(String workflowName);

    List<String> listAllowedTransitions(String taskId);

    // RBAC
    List<String> listRoles();

    // LIST HELPERS
    Map<String, List<String>> listTasksGroupedByProject();

    Map<String, List<String>> listUsersWithRolesByProject();
}
