package com.nexus.exception;

/*
 Lançada quando o sistema recebe uma instrução inexistente.
*/

public class UnknownCommandException extends NexusDomainException {
 
    public UnknownCommandException(String message){
        super(message);
    }


}


