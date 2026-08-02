package com.nexus.exception;

/*
Lançada quando buscas no Workspace ou repositórios falham.
*/
public class EntityNotFoundException extends NexusDomainException{
    
    public EntityNotFoundException(String message){
        super(message);
    }
}
