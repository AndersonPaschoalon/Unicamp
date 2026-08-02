package com.nexus.exception;

public class UnknownCommandException extends NexusDomainException {
    public UnknownCommandException(String message) {
        super(message);
    }
}
