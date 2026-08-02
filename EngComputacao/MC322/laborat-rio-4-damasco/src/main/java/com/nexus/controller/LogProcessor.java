package com.nexus.controller;

import java.io.IOException;

import com.nexus.exception.NexusDomainException;
import com.nexus.exception.NexusSystemException;
import com.nexus.model.Command;
import com.nexus.service.TelemetryRegistry;
import com.nexus.service.infra.CommandParser;
import com.nexus.service.infra.FileLoader;

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
     * @param commandDispatcher The CommandDispatcher to be used for executing
     *                          parsed commands.
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
     * @param fileName the name of the log file to be loaded from the classpath
     * 
     * @throws NexusSystemException if the log file cannot be read
     * 
     * @see NexusSystemException
     */
    public void processLog(String fileName) throws NexusSystemException {
        try {
            fileLoader.loadFile(fileName).forEach(line -> {
                if (line.isBlank() || line.startsWith("#")) {
                    return;
                }
                try {
                    System.out.println("[CONSOLE] > " + line);
                    Command command = CommandParser.parseCommand(line);
                    String output = commandDispatcher.dispatchCommand(command);
                    if (output != null) {
                        System.out.println(output);
                    }
                } catch (NexusDomainException e) {
                    TelemetryRegistry.getInstance().incrementValidationErrors();
                    System.out.println("[!] Erro ao processar linha: " + line + " -> " + e.getMessage());
                }
            });
        } catch (IOException e) {
            throw new NexusSystemException("Falha ao ler arquivo de log: " + fileName, e);
        }
    }

}