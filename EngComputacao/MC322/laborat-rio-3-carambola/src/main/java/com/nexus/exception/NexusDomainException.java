package com.nexus.exception;


/*
Representa quebras de contrato de negócio provocadas pelo usuário (ex: projeto inexistente, falta de permissão). O sistema deve capturar essas exceções e informar o usuário para que ele corrija a ação
*/
public class NexusDomainException extends NexusException {
    

    public NexusDomainException(String message) {
        super(message);
    }
}
