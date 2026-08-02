package com.nexus.exception;


/*
Lançada quando as regras do Workflow são violadas (ex: mover uma tarefa finalizada).
*/
public class InvalidTransitionException extends NexusDomainException {
    
    public InvalidTransitionException(String message){
        super(message);
    }
}
