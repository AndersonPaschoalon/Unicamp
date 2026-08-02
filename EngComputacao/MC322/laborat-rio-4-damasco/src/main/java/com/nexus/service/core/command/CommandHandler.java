package com.nexus.service.core.command;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.nexus.exception.CommandFormatException;
import com.nexus.model.Command;

public interface CommandHandler {
    void handle(Command command);

    /**
     * Executes the command and returns optional displayable output.
     *
     * <p>The default implementation delegates to {@link #handle(Command)}
     * and returns {@code null} (no output). Report handlers override this
     * method to return the formatted report text.</p>
     *
     * @param command the command to handle
     * @return formatted output to display, or {@code null} if none
     */
    default String handleWithOutput(Command command) {
        handle(command);
        return null;
    }

    default Long extractLong(String[] args, int index, String fieldName) {
        try {
            return Long.parseLong(args[index]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new CommandFormatException("Argumento inválido para o campo: " + fieldName);
        }
    }

    default LocalDate extractDate(String[] args, int index, String fieldName) {
        try {
            return LocalDate.parse(args[index]);
        } catch (DateTimeParseException | ArrayIndexOutOfBoundsException e) {
            throw new CommandFormatException("Argumento inválido para o campo: " + fieldName);
        }
    }

    default Boolean extractBoolean(String[] args, int index, String fieldName) {
        try {
            return Boolean.parseBoolean(args[index]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CommandFormatException("Argumento inválido para o campo: " + fieldName);
        }
    }
}
