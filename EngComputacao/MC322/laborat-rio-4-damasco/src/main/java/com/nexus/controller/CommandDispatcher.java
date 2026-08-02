package com.nexus.controller;

import com.nexus.model.Command;
import com.nexus.service.core.command.CommandFactory;
import com.nexus.service.core.command.CommandHandler;
import com.nexus.service.infra.CommandParser;
/**
 * Service class for dispatching commands to the appropriate handlers for users, projects, and tasks.
 */
public class CommandDispatcher {
    private final CommandFactory commandFactory;
    private static CommandDispatcher instance;

    /**
     * Constructs a CommandDispatcher with the required dependencies.
     *
     * @param commandFactory The CommandFactory to be used by this dispatcher.
     */
    private CommandDispatcher(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public static CommandDispatcher build(CommandFactory commandFactory) {
        if (instance != null) {
            throw new IllegalStateException("CommandDispatcher already initialized.");
        }
        instance = new CommandDispatcher(commandFactory);
        return instance;
    }

    public static CommandDispatcher getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CommandDispatcher not initialized. Call build() first.");
        }
        return instance;
    }

    /**
     * Parses a command line and dispatches it when parsing succeeds.
     *
     * @param line the raw command line
     * @return optional displayable output, or {@code null}
     */
    public String process(String line) {
        Command command = CommandParser.parseCommand(line);

        if (command != null) {
            return this.dispatchCommand(command);
        }
        return null;
    }

    /**
     * Dispatches a command to the appropriate handler based on the action.
     *
     * @param command the command to dispatch
     * @return optional displayable output, or {@code null}
     */
    public String dispatchCommand(Command command) {
        CommandHandler handler = commandFactory.getHandler(command);
        return handler.handleWithOutput(command);
    }
}