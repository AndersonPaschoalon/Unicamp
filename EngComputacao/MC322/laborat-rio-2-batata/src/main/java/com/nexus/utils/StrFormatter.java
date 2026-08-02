package com.nexus.utils;

import com.nexus.model.*;
import com.nexus.service.Workspace;

import java.util.List;

/**
 * Auxiliary methods to format strings.
 */
public class StrFormatter {

    /**
     * Truncates a string to a maximum length, appending "..." if truncation occurs.
     *
     * @param str the string to be truncated. If null, returns an empty string.
     * @param tam the maximum length of the resulting string (including the "..." suffix if truncated).
     * @return a truncated string with "..." appended if the original length exceeds tam,
     *         or the original string if it fits within tam. Returns an empty string if str is null.
     */
    public static String truncar(String str, int tam) {
        if (str == null)
            return "";
        return str.length() > tam ? str.substring(0, tam - 3) + "..." : str;
    }

    public static void listUsers(List<User> users) {
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

    public static void listProjectsHealth(Workspace workspace) {
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

    



}
