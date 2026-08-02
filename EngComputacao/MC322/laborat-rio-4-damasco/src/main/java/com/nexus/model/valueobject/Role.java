package com.nexus.model.valueobject;

/**
 * Enumeration representing the different roles a user can have within a project.
 * 
 * <p>This enum defines the hierarchy and permissions for project team members:</p>
 * <ul>
 *   <li>{@code VIEWER} - Can only view project information and tasks</li>
 *   <li>{@code CONTRIBUTOR} - Can view and modify tasks, change task statuses</li>
 *   <li>{@code MANAGER} - Has full permissions including project management capabilities</li>
 * </ul>
 * 
 * <p>Roles are used to control access to various operations within the project
 * management system, ensuring that users can only perform actions appropriate
 * to their assigned role level.</p>
 */
public enum Role {
    /** Read-only access to project information */
    VIEWER, 
    
    /** Can contribute to tasks and change task statuses */
    CONTRIBUTOR, 
    
    /** Full project management permissions */
    MANAGER
}