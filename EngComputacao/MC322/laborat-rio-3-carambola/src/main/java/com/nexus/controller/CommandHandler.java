package com.nexus.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import com.nexus.exception.CommandFormatException;
import com.nexus.model.Command;
import com.nexus.service.Workspace;

public interface CommandHandler {
    void handle(Command command, Workspace workspace);

    default void requireParamCount(String[] params, int expected, String commandName) {
        if (params == null)
            params = new String[0];
        if (params.length != expected) {
            throw new CommandFormatException(
                    String.format("Esperado %d parâmetros para %s, mas recebeu %d", expected, commandName,
                            params.length));
        }
    }

    default String extractString(String[] params, int idx, String fieldName) {
        Objects.requireNonNull(params, "params");
        try {
            String v = params[idx];
            if (v == null)
                throw new CommandFormatException("Argumento nulo para " + fieldName);
            return v;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CommandFormatException("Argumento ausente para " + fieldName);
        }
    }

    default Long extractLong(String[] params, int idx, String fieldName) {
        String s = extractString(params, idx, fieldName);
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ex) {
            throw new CommandFormatException("Argumento inválido");
        }
    }

    default LocalDate extractDate(String[] params, int idx, String fieldName) {
        String s = extractString(params, idx, fieldName);
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException ex) {
            throw new CommandFormatException("Argumento inválido");
        }
    }

    default Boolean extractBoolean(String[] params, int idx, String fieldName) {
        String s = extractString(params, idx, fieldName);
        if ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) {
            return Boolean.parseBoolean(s);
        }
        throw new CommandFormatException("Argumento inválido (boolean) para " + fieldName + ": " + s);
    }
}
