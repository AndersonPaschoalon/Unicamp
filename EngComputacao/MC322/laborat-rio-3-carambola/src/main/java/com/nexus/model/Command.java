package com.nexus.model;

/**
 * Represents a parsed command with its original line, action, and parameters.
 */
public class Command {
    private String line;
    private String action;
    private String[] parameters;

    /**
     * Constructs a Command with the given line, action, and parameters.
     *
     * @param line       the original command line
     * @param action     the action to perform
     * @param parameters the parameters for the action
     */
    public Command(String line, String action, String[] parameters) {
        this.line = line;
        this.action = action;
        this.parameters = parameters.clone();
    }

    public Command(String action, String... parameters) {
        this.line = action + ";" + String.join(";", parameters);
        this.action = action;
        this.parameters = parameters.clone();
    }

    /**
     * Gets the original command line.
     *
     * @return the command line
     */
    public String getLine() {
        return line;
    }

    /**
     * Gets the action of the command.
     *
     * @return the action string
     */
    public String getAction() {
        return action;
    }

    /**
     * Gets the parameters of the command.
     *
     * @return the parameters array
     */
    public String[] getParameters() {
        return parameters.clone();
    }

}
