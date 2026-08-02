package com.nexus.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.nexus.model.Workflow;
import com.nexus.model.entity.Project;
import com.nexus.model.entity.Status;
import com.nexus.model.entity.Task;
import com.nexus.model.entity.User;
import com.nexus.service.persistence.GsonFactory;
import com.nexus.service.persistence.JsonlRepository;

/**
 * Workspace class manages tasks and projects within a system.
 *
 * This class provides functionality to:
 * <ul>
 * <li>Add and retrieve tasks and projects</li>
 * <li>Identify top performing users based on completed tasks</li>
 * <li>Detect overloaded users with excessive in-progress tasks</li>
 * <li>Calculate project completion health metrics</li>
 * <li>Identify system bottlenecks by task status</li>
 * <li>Query tasks and projects by ID or name</li>
 * </ul>
 * 
 * <p>
 * All methods that work with task statuses include proper null checking to
 * handle tasks
 * that may not have a status assigned yet.
 * </p>
 *
 * @author Workspace Team
 * @version 1.0
 */
public class Workspace {
    private static final Path DATA_DIR = Path.of("data");

    private final JsonlRepository<Task> taskRepository;
    private final JsonlRepository<Project> projectRepository;
    private final JsonlRepository<User> userRepository;
    private final JsonlRepository<Workflow> workflowRepository;
    private final JsonlRepository<Status> statusRepository;

    public Workspace() {
        this.taskRepository = new JsonlRepository<>(DATA_DIR.resolve("tasks.jsonl"), Task.class,
                GsonFactory.getInstance());
        this.projectRepository = new JsonlRepository<>(DATA_DIR.resolve("projects.jsonl"), Project.class,
                GsonFactory.getInstance());
        this.userRepository = new JsonlRepository<>(DATA_DIR.resolve("users.jsonl"), User.class,
                GsonFactory.getInstance());
        this.workflowRepository = new JsonlRepository<>(DATA_DIR.resolve("workflows.jsonl"), Workflow.class,
                GsonFactory.getInstance());
        this.statusRepository = new JsonlRepository<>(DATA_DIR.resolve("statuses.jsonl"), Status.class,
                GsonFactory.getInstance());
    }

    // ---- Task ----
    public Optional<Task> getTask(String id) {
        return taskRepository.findById(id);
    }

    public void saveTask(Task task) {
        taskRepository.save(task);
    }

    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    // ---- Project ----
    public Optional<Project> getProject(String id) {
        return projectRepository.findById(id);
    }

    public void saveProject(Project project) {
        projectRepository.save(project);
    }

    public List<Project> findAllProjects() {
        return projectRepository.findAll();
    }

    // ---- User ----
    public Optional<User> getUser(String id) {
        return userRepository.findById(id);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // ---- Workflow ----
    public Optional<Workflow> getWorkflow(String id) {
        return workflowRepository.findById(id);
    }

    public void saveWorkflow(Workflow workflow) {
        workflowRepository.save(workflow);
    }

    public List<Workflow> findAllWorkflows() {
        return workflowRepository.findAll();
    }

    // ---- Status ----
    public Optional<Status> getStatus(String name) {
        return statusRepository.findById(name);
    }

    public void saveStatus(Status status) {
        statusRepository.save(status);
    }

    public List<Status> findAllStatuses() {
        return statusRepository.findAll();
    }
}