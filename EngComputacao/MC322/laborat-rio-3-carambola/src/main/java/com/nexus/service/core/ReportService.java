package com.nexus.service.core;

import java.util.List;

import com.nexus.model.entity.Task;
import com.nexus.model.entity.User;
import com.nexus.service.TelemetryRegistry;
import com.nexus.service.Workspace;

/**
 * Service class for generating various reports and displaying system information.
 * 
 * <p>This service provides functionality to generate comprehensive status reports,
 * display lists of users, tasks, and project health information in formatted tables.
 * It integrates with the TelemetryRegistry to include system metrics in reports.</p>
 * 
 * <p>All output is formatted as console-friendly tables with proper alignment and
 * borders for improved readability.</p>
 */
public class ReportService {
    private final TelemetryRegistry telemetryRegistry;

    /**
     * Constructs a ReportService with the specified telemetry registry.
     *
     * @param telemetryRegistry the telemetry registry to use for metrics collection
     */
    public ReportService() {
        this.telemetryRegistry = TelemetryRegistry.getInstance();
    }

    /**
     * Generates a comprehensive status report including top performers, overloaded users,
     * project health, and global bottlenecks.
     * 
     * <p>This method produces a complete system overview by calling various other
     * reporting methods to display different aspects of the workspace status.</p>
     *
     * @param workspace the workspace to generate the report for
     */
    public void generateStatusReport(Workspace workspace) {
        System.out.println("[LOG] Status Report:");
        System.out.println("[LOG] Top Performers:");
        listUsers(workspace.getTopPerformers());
        System.out.println("[LOG] Overloaded Users:");
        listUsers(workspace.getOverloadedUsers());
        System.out.println("[LOG] Project Health:");
        listProjectsHealth(workspace);
        String bottleneck = workspace.getGlobalBottlenecks();
        System.out.println("[LOG] Global Bottleneck: " + (bottleneck == null ? "Nenhum" : bottleneck));
    }

    /**
     * Displays a formatted table of users in the console.
     * 
     * This method prints a nicely formatted table containing the username and email
     * of each user in the provided list. If the list is null or empty, a message
     * indicating no users were found is displayed instead.
     * 
     * @param users the list of users to be displayed. If null or empty, a notification
     *              message will be printed and the method will return without further action.
     * 
     * @see User
     * @see #truncar(String, int)
     */
    public void listUsers(List<User> users) {
        if (users == null || users.isEmpty()) {
            System.out.println("[!] Nenhum usuário encontrado.");
            return;
        }

        String header = "+----+----------------------+--------------------------+";
        System.out.println(header);
        System.out.printf("| %-20s | %-24s |%n", "USERNAME", "EMAIL");
        System.out.println(header);

        for (User u : users) {
            System.out.printf("| %-20s | %-24s |%n",
                    truncar(u.consultUsername(), 20),
                    truncar(u.consultEmail(), 24));
        }
        System.out.println(header);
    }

    /**
     * Displays all tasks currently stored in the {@link Workspace} in a simple table format.
     * If no tasks exist, prints a notification message.
     * 
     * <p>The table includes task ID, title, status name, deadline, and estimated effort.
     * Tasks with null status will display the status as the string representation of null.</p>
     * 
     * @param workspace the workspace containing the tasks to display
     */
    public void listTasks(Workspace workspace) {
        List<Task> tasks = workspace.getTasks();
        if (tasks.isEmpty()) {
            System.out.println("\n[!] Nenhuma tarefa no sistema.");
            return;
        }

        String header = "+------------+----------------------+-------------+------------+-----------------+";
        System.out.println("\n" + header);
        System.out.printf("| %-10s | %-20s | %-11s | %-10s | %-15s |%n", "ID", "TÍTULO", "STATUS", "DEADLINE", "ESFORÇO ESTIMADO");
        System.out.println(header);

        for (Task t : tasks) {
            String statusName = (t.getStatus() != null) ? t.getStatus().getName() : "null";
            System.out.printf("| %-10s | %-20s | %-11s | %-10s | %-15d |%n",
                    t.getId(),
                    truncar(t.getTitle(), 20),
                    truncar(statusName, 11),
                    t.getDeadline(),
                    t.getEstimatedEffort());
        }
        System.out.println(header);
        System.out.println("Total de tarefas: " + telemetryRegistry.getTotalTasksCreated());
    }

    /**
     * Displays the health status of all projects in a workspace in a formatted table.
     * 
     * <p>This method prints a table showing each project's name and completion percentage.
     * If the workspace is null or contains no projects, a message indicating no projects
     * were found is displayed instead.</p>
     * 
     * <p>The output format includes:</p>
     * <ul>
     *   <li>Project name (truncated to 20 characters if necessary)</li>
     *   <li>Completion percentage (displayed with 2 decimal places)</li>
     * </ul>
     * 
     * @param workspace the {@link Workspace} object containing the projects to be evaluated.
     *                  If null or empty, an appropriate message will be printed.
     * 
     * @throws NullPointerException if workspace.getProjects() returns null
     */
    public void listProjectsHealth(Workspace workspace) {
        if (workspace == null || workspace.getProjects().isEmpty()) {
            System.out.println("[!] Nenhum projeto encontrado.");
            return;
        }

        String header = "+----+----------------------+--------------------------+";
        System.out.println(header);
        System.out.printf("| %-20s | %-24s |%n", "PROJETO", "PERCENTUAL CONCLUÍDO");
        System.out.println(header);

        workspace.getProjects().forEach(project -> {
            double completion = workspace.getProjectHealth(project);
            System.out.printf("| %-20s | %22.2f%% |%n",
                    truncar(project.getName(), 20),
                    completion);
        });

        System.out.println(header);
    }

    /**
     * Truncates a string to a maximum length, appending "..." if truncation occurs.
     *
     * @param str the string to be truncated. If null, returns an empty string.
     * @param tam the maximum length of the resulting string (including the "..." suffix if truncated).
     * @return a truncated string with "..." appended if the original length exceeds tam,
     *         or the original string if it fits within tam. Returns an empty string if str is null.
     */
    private String truncar(String str, int tam) {
        if (str == null)
            return "";
        return str.length() > tam ? str.substring(0, tam - 3) + "..." : str;
    }

    public void generateTopPerformersReport(Workspace workspace) {
        System.out.println("[LOG] Top Performers:");
        listUsers(workspace.getTopPerformers());
    }

    public void generateOverloadedUsersReport(Workspace workspace) {
        System.out.println("[LOG] Overloaded Users:");
        listUsers(workspace.getOverloadedUsers());
    }

    public void generateProjectHealthReport(Workspace workspace) {
        System.out.println("[LOG] Project Health:");
        listProjectsHealth(workspace);
    }

    public void generateBottleneckReport(Workspace workspace) {
        String bottleneck = workspace.getGlobalBottlenecks();
        System.out.println("[LOG] Global Bottleneck: " + (bottleneck == null ? "Nenhum" : bottleneck));
    }
}