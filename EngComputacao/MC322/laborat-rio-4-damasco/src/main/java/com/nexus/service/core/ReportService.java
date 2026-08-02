package com.nexus.service.core;

import java.util.List;
import java.util.Map;

import com.nexus.model.entity.Project;
import com.nexus.model.entity.Task;
import com.nexus.model.entity.User;
import com.nexus.service.TelemetryRegistry;
import com.nexus.service.Workspace;

import java.util.Comparator;
import java.util.Optional;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import com.nexus.model.entity.Status;

/**
 * Service class for generating various reports as formatted text.
 *
 * <p>
 * This service provides functionality to generate comprehensive status reports,
 * lists of users, tasks, and project health information in formatted tables.
 * </p>
 *
 * <p>
 * All methods return the formatted report as a {@link String} so the
 * presentation layer can render it through {@code PromptService}.
 * </p>
 */
public class ReportService {
    private final Workspace workspace;
    private final TelemetryRegistry telemetryRegistry;

    /**
     * Constructs a report service with the given workspace.
     *
     * @param workspace the workspace used to compute reports
     */
    public ReportService(Workspace workspace) {
        this.workspace = workspace;
        this.telemetryRegistry = TelemetryRegistry.getInstance();
    }

    public List<User> getTopPerformers() {
        return workspace.findAllTasks().stream()
                .filter(t -> t.getOwnerId() != null && isTerminalStatus(t.getStatusId()))
                .collect(groupingBy(Task::getOwnerId, counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .map(e -> workspace.getUser(e.getKey()).orElse(null))
                .filter(u -> u != null)
                .toList();
    }

    public List<User> getOverloadedUsers() {
        return workspace.findAllTasks().stream()
                .filter(t -> t.getOwnerId() != null && "IN_PROGRESS".equals(t.getStatusId()))
                .collect(groupingBy(Task::getOwnerId, counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() > 10)
                .map(e -> workspace.getUser(e.getKey()).orElse(null))
                .filter(u -> u != null)
                .toList();
    }

    public double getProjectHealth(Project project) {
        List<Task> projectTasks = workspace.findAllTasks().stream()
                .filter(t -> project.getId().equals(t.getProjectId()))
                .toList();
        long total = projectTasks.size();
        if (total == 0)
            return 0;
        long done = projectTasks.stream()
                .filter(t -> isTerminalStatus(t.getStatusId()))
                .count();
        return done * 100.0 / total;
    }

    public String getGlobalBottlenecks() {
        return workspace.findAllTasks().stream()
                .filter(t -> t.getStatusId() != null && t.getOwnerId() != null && !isTerminalStatus(t.getStatusId()))
                .collect(groupingBy(Task::getStatusId, counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private boolean isTerminalStatus(String statusId) {
        if (statusId == null)
            return false;
        return workspace.getStatus(statusId).map(Status::isTerminal).orElse(false);
    }

    /**
     * Generates a comprehensive status report including top performers, overloaded
     * users,
     * project health, and global bottlenecks.
     *
     * @return the formatted status report
     */
    public String generateStatusReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("[LOG] Status Report:\n");
        sb.append("[LOG] Top Performers:\n");
        sb.append(formatUsers(getTopPerformers()));
        sb.append("[LOG] Overloaded Users:\n");
        sb.append(formatUsers(getOverloadedUsers()));
        sb.append("[LOG] Project Health:\n");
        sb.append(formatProjectsHealth());
        String bottleneck = getGlobalBottlenecks();
        sb.append("[LOG] Global Bottleneck: ").append(bottleneck == null ? "Nenhum" : bottleneck).append("\n");
        return sb.toString();
    }

    /**
     * Formats a list of users as a table.
     *
     * @param users the list of users to format
     * @return the formatted user table, or a warning if the list is empty
     */
    public String formatUsers(List<User> users) {
        if (users == null || users.isEmpty()) {
            return "[!] Nenhum usuário encontrado.\n";
        }
        StringBuilder sb = new StringBuilder();
        String header = "+----+----------------------+--------------------------+";
        sb.append(header).append("\n");
        sb.append(String.format("| %-20s | %-24s |%n", "USERNAME", "EMAIL"));
        sb.append(header).append("\n");
        for (User u : users) {
            sb.append(String.format("| %-20s | %-24s |%n",
                    truncar(u.consultUsername(), 20),
                    truncar(u.consultEmail(), 24)));
        }
        sb.append(header).append("\n");
        return sb.toString();
    }

    /**
     * Formats all tasks currently stored in the {@link Workspace} as a table.
     *
     * @return the formatted task table, or a warning if no tasks exist
     */
    public String listTasks() {
        List<Task> tasks = workspace.findAllTasks();
        if (tasks.isEmpty()) {
            return "\n[!] Nenhuma tarefa no sistema.\n";
        }
        StringBuilder sb = new StringBuilder();
        String header = "+------------+----------------------+-------------+------------+-----------------+";
        sb.append("\n").append(header).append("\n");
        sb.append(String.format("| %-10s | %-20s | %-11s | %-10s | %-15s |%n", "ID", "TÍTULO", "STATUS", "DEADLINE",
                "ESFORÇO ESTIMADO"));
        sb.append(header).append("\n");
        for (Task t : tasks) {
            String statusName = (t.getStatusId() != null) ? t.getStatusId() : "null";
            sb.append(String.format("| %-10s | %-20s | %-11s | %-10s | %-15d |%n",
                    t.getId(),
                    truncar(t.getTitle(), 20),
                    truncar(statusName, 11),
                    t.getDeadline(),
                    t.getEstimatedEffort()));
        }
        sb.append(header).append("\n");
        sb.append("Total de tarefas: ").append(telemetryRegistry.getTotalTasksCreated()).append("\n");
        return sb.toString();
    }

    /**
     * Formats the health status of all projects as a table.
     *
     * @return the formatted project health table, or a warning if no projects exist
     */
    public String formatProjectsHealth() {
        List<Project> projects = workspace.findAllProjects();
        if (projects.isEmpty()) {
            return "[!] Nenhum projeto encontrado.\n";
        }
        StringBuilder sb = new StringBuilder();
        String header = "+----+----------------------+--------------------------+";
        sb.append(header).append("\n");
        sb.append(String.format("| %-20s | %-24s |%n", "PROJETO", "PERCENTUAL CONCLUÍDO"));
        sb.append(header).append("\n");
        for (Project project : projects) {
            double completion = getProjectHealth(project);
            sb.append(String.format("| %-20s | %22.2f%% |%n",
                    truncar(project.getName(), 20),
                    completion));
        }
        sb.append(header).append("\n");
        return sb.toString();
    }

    /**
     * Truncates a string to a maximum length, appending "..." if truncation occurs.
     *
     * @param str the string to be truncated. If null, returns an empty string.
     * @param tam the maximum length of the resulting string (including the "..."
     *            suffix if truncated).
     * @return a truncated string with "..." appended if the original length exceeds
     *         tam,
     *         or the original string if it fits within tam. Returns an empty string
     *         if str is null.
     */
    private String truncar(String str, int tam) {
        if (str == null)
            return "";
        return str.length() > tam ? str.substring(0, tam - 3) + "..." : str;
    }

    /**
     * Generates the top performers report.
     *
     * @return the formatted top performers report
     */
    public String generateTopPerformersReport() {
        return "[LOG] Top Performers:\n" + formatUsers(getTopPerformers());
    }

    /**
     * Generates the overloaded users report.
     *
     * @return the formatted overloaded users report
     */
    public String generateOverloadedUsersReport() {
        return "[LOG] Overloaded Users:\n" + formatUsers(getOverloadedUsers());
    }

    /**
     * Generates the project health report.
     *
     * @return the formatted project health report
     */

    public String generateProjectHealthReport() {
        return "[LOG] Project Health:\n" + formatProjectsHealth();
    }

    /**
     * Generates the global bottleneck report.
     *
     * @return the formatted bottleneck report
     */
    public String generateBottleneckReport() {
        String bottleneck = getGlobalBottlenecks();
        return "[LOG] Global Bottleneck: " + (bottleneck == null ? "Nenhum" : bottleneck) + "\n";
    }

}