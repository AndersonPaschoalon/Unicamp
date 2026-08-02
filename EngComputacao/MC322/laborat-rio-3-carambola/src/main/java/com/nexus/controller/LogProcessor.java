package com.nexus.controller;

import com.nexus.model.*;
import com.nexus.model.entity.Project;
import com.nexus.model.entity.Task;
import com.nexus.model.entity.User;
import com.nexus.service.Workspace;
import com.nexus.service.core.ReportService;
import com.nexus.service.core.UserService;
import com.nexus.service.infra.CommandParser;
import com.nexus.service.infra.FileLoader;
import com.nexus.exception.NexusSystemException;
import java.io.IOException;

/**
 * LogProcessor is responsible for parsing and executing commands from a log
 * file.
 * 
 * This class reads a log file from the project's classpath and processes each
 * line
 * as a command to manipulate users, projects, and tasks within a workspace.
 * 
 * Supported commands:
 * - CREATE_USER: Creates a new user with username and password
 * - CREATE_PROJECT: Creates a new project with name and budget
 * - CREATE_TASK: Creates a new task with name, due date, and estimated hours
 * - ASSIGN_USER: Assigns a user to a task
 * - CHANGE_STATUS: Changes the status of a task (IN_PROGRESS, DONE, BLOCKED)
 * - REPORT_STATUS: Generates a status report of the workspace
 * 
 * @author Nexus Team
 * @version 1.0
 */
public class LogProcessor {
    private static final FileLoader fileLoader = new FileLoader();
    private final CommandDispatcher commandDispatcher;

    /**
     * Constructs a LogProcessor with the required dependencies.
     *
     * @param reportService     the ReportService to use
     * @param telemetryRegistry the TelemetryRegistry to use
     */
    public LogProcessor(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    /**
     * Processes a log file and executes commands to manipulate workspace data.
     * 
     * This method reads a file from the classpath, parses each line as a
     * semicolon-delimited
     * command, and executes corresponding operations on the provided workspace and
     * users list.
     * Lines starting with '#' or empty lines are ignored.
     * 
     * Validation errors are caught and logged without interrupting the processing
     * of subsequent lines.
     * 
     * @param fileName    the name of the log file to be loaded from the classpath
     * @param workspace   the {@link Workspace} object to be modified by the
     *                    commands
     * @param userService the {@link UserService} object to be used for user-related
     *                    operations
     * 
     * @throws IOException              if the file is not found in the classpath
     *                                  (caught and logged as fatal error)
     * @throws NexusValidationException if command parameters are invalid (caught
     *                                  and logged as validation error)
     * 
     * @see NexusValidationException
     * @see Workspace
     * @see User
     * @see Project
     * @see Task
     */
    public void processLog(String fileName, Workspace workspace, UserService userService)
            throws IOException, NexusSystemException {
        try {
            fileLoader.loadFile(fileName).forEach(line -> {
                if (line.isBlank() || line.startsWith("#")) {
                    return;
                }
                Command command = CommandParser.parseCommand(line);
                commandDispatcher.dispatch(command);
                // commandDispatcher.process(line, workspace, userService);
            });
        } catch (IOException e) {
            System.err.println("[ERRO FATAL] " + e.getMessage());
        }
    }
}