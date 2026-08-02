package com.nexus.service.core;

import java.util.List;

import com.nexus.exception.EntityNotFoundException;
import com.nexus.model.Workflow;
import com.nexus.model.entity.Status;
import com.nexus.service.Workspace;

/**
 * Service class for managing workflows and statuses in the system.
 * 
 * <p>
 * This service provides functionality to create and manage workflows and
 * statuses,
 * including creating new statuses, creating new workflows, retrieving existing
 * workflows
 * and statuses by name, and defining allowed transitions between statuses
 * within workflows.
 * </p>
 * 
 * <p>
 * The service acts as a facade for workflow-related operations and integrates
 * with
 * the Workspace to persist workflow and status information.
 * </p>
 */
public class WorkflowService {
    private final Workspace workspace;

    /**
     * Constructs a WorkflowService with the given workspace.
     *
     * @param workspace the workspace that holds workflows and statuses
     */
    public WorkflowService(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * Retrieves all workflows registered in the workspace.
     *
     * @return an unmodifiable list of all workflows
     */
    public List<Workflow> getWorkflows() {
        return this.workspace.findAllWorkflows();
    }

    /**
     * Retrieves all statuses registered in the workspace.
     *
     * @return an unmodifiable list of all statuses
     */
    public List<Status> getStatuses() {
        return this.workspace.findAllStatuses();
    }

    /**
     * Creates a new status and adds it to the workspace.
     *
     * @param name       the name of the status to create
     * @param isTerminal true if this status represents a final state, false
     *                   otherwise
     */
    public void createStatus(String name, boolean isTerminal) {
        this.workspace.saveStatus(new Status(name, isTerminal));
    }

    /**
     * Creates a new workflow and adds it to the workspace.
     *
     * @param workflowName the name of the workflow to create
     */
    public void createWorkflow(String workflowName) {
        this.workspace.saveWorkflow(new Workflow(workflowName));
    }

    /**
     * Retrieves a workflow by its name from the workspace.
     *
     * @param workflowName the name of the workflow to retrieve
     * @return the Workflow with the specified name
     * @throws com.nexus.exception.EntityNotFoundException if the workflow is not
     *                                                     found
     */
    public Workflow getWorkflowByName(String workflowName) {
        return this.workspace.getWorkflow(workflowName)
                .orElseThrow(() -> new EntityNotFoundException("Workflow '" + workflowName + "' não encontrado."));

    }

    /**
     * Retrieves a status by its name from the workspace.
     *
     * @param statusName the name of the status to retrieve
     * @return the Status with the specified name
     * @throws com.nexus.exception.EntityNotFoundException if the status is not
     *                                                     found
     */
    public Status getStatusByName(String statusName) {
        return this.workspace.getStatus(statusName)
                .orElseThrow(() -> new EntityNotFoundException("Status '" + statusName + "' não encontrado."));

    }

    /**
     * Sets the initial status for a workflow.
     * 
     * <p>
     * The initial status determines what status new tasks will be assigned
     * when they are created within a project that uses this workflow.
     * This must be set before tasks can be created in the project.
     * </p>
     *
     * @param workflowName the name of the workflow to configure
     * @param statusName   the name of the status to set as initial
     */
    public void setInitialStatus(String workflowName, String statusName) {
        Workflow workflow = getWorkflowByName(workflowName);
        Status status = getStatusByName(statusName);
        this.workspace.saveWorkflow(workflow.withInitialStatusId(status.getName()));
    }

    /**
     * Defines an allowed transition between two statuses in a workflow.
     * 
     * <p>
     * This method retrieves the specified workflow and the source and target
     * statuses,
     * then configures the workflow to allow transitions from the source status to
     * the
     * target status by calling {@link Workflow#addTransition(Status, Status)}.
     * </p>
     *
     * @param workflowName   the name of the workflow to configure
     * @param fromStatusName the name of the source status
     * @param toStatusName   the name of the target status
     */
    public void allowTransition(String workflowName, String fromStatusName, String toStatusName) {
        Workflow workflow = getWorkflowByName(workflowName);
        getStatusByName(fromStatusName); // valida existência
        getStatusByName(toStatusName); // valida existência
        this.workspace.saveWorkflow(workflow.withTransition(fromStatusName, toStatusName));

    }
}