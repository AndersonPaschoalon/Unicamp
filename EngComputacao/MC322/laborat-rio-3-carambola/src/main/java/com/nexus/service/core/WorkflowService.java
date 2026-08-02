package com.nexus.service.core;

import com.nexus.model.Workflow;
import com.nexus.model.entity.Status;
import com.nexus.service.Workspace;

/**
 * Service class for managing workflows and statuses in the system.
 * 
 * <p>This service provides functionality to create and manage workflows and statuses,
 * including creating new statuses, creating new workflows, retrieving existing workflows
 * and statuses by name, and defining allowed transitions between statuses within workflows.</p>
 * 
 * <p>The service acts as a facade for workflow-related operations and integrates with
 * the Workspace to persist workflow and status information.</p>
 */
public class WorkflowService {
    
    /**
     * Creates a new status and adds it to the workspace.
     *
     * @param name the name of the status to create
     * @param isTerminal true if this status represents a final state, false otherwise
     * @param workspace the workspace to add the status to
     */
    public void createStatus(String name, boolean isTerminal, Workspace workspace) {
        Status status = new Status(name, isTerminal);
        workspace.addStatus(status);
        System.out.println("[LOG] Status criado: " + name);
    }

    /**
     * Creates a new workflow and adds it to the workspace.
     *
     * @param workflowName the name of the workflow to create
     * @param workspace the workspace to add the workflow to
     */
    public void createWorkflow(String workflowName, Workspace workspace) {
        Workflow workflow = new Workflow(workflowName);
        workspace.addWorkflow(workflow);
        System.out.println("[LOG] Workflow criado: " + workflowName);
    }

    /**
     * Retrieves a workflow by its name from the workspace.
     *
     * @param workflowName the name of the workflow to retrieve
     * @param workspace the workspace to search in
     * @return the Workflow with the specified name
     * @throws com.nexus.exception.NexusValidationException if the workflow is not found
     */
    public Workflow getWorkflowByName(String workflowName, Workspace workspace) {
        return workspace.getWorkflowByName(workflowName);
    }

    /**
     * Retrieves a status by its name from the workspace.
     *
     * @param statusName the name of the status to retrieve
     * @param workspace the workspace to search in
     * @return the Status with the specified name
     * @throws com.nexus.exception.NexusValidationException if the status is not found
     */
    public Status getStatusByName(String statusName, Workspace workspace) {
        return workspace.getStatusByName(statusName);
    }

    /**
     * Sets the initial status for a workflow.
     * 
     * <p>The initial status determines what status new tasks will be assigned
     * when they are created within a project that uses this workflow.
     * This must be set before tasks can be created in the project.</p>
     *
     * @param workflowName the name of the workflow to configure
     * @param statusName the name of the status to set as initial
     * @param workspace the workspace containing the workflow and status
     */
    public void setInitialStatus(String workflowName, String statusName, Workspace workspace) {
        Workflow workflow = getWorkflowByName(workflowName, workspace);
        Status status = getStatusByName(statusName, workspace);
        workflow.setInitialStatus(status);
        System.out.println("[LOG] Status inicial definido para o workflow " + workflowName + ": " + statusName);
    }

    /**
     * Defines an allowed transition between two statuses in a workflow.
     * 
     * <p>This method retrieves the specified workflow and the source and target statuses,
     * then configures the workflow to allow transitions from the source status to the 
     * target status by calling {@link Workflow#addTransition(Status, Status)}.</p>
     *
     * @param workflowName the name of the workflow to configure
     * @param fromStatusName the name of the source status
     * @param toStatusName the name of the target status
     * @param workspace the workspace containing the workflow and statuses
     */
    public void allowTransition(String workflowName, String fromStatusName, String toStatusName, Workspace workspace) {
        Workflow workflow = getWorkflowByName(workflowName, workspace);

        Status fromStatus = getStatusByName(fromStatusName, workspace);

        Status toStatus = getStatusByName(toStatusName, workspace);

        workflow.addTransition(fromStatus, toStatus);
        System.out.println("[LOG] Transição permitida no workflow " + workflowName + ": " + fromStatusName + " -> " + toStatusName);
    }
}