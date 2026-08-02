package com.nexus.exception;

/*
Lançada quando um usuário tenta executar uma ação (como MOVE_TASK) sem possuir o Role adequado.
*/
public class PermissionDeniedException extends NexusDomainException {
    
    public PermissionDeniedException(String message){
        super(message);
    }
}
