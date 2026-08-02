package com.nexus.exception;


/*
Lançada logo no início do processamento se argumentos numéricos contiverem letras ou se a sintaxe interativa for violada.
*/
public class CommandFormatException extends NexusDomainException {
    
    public CommandFormatException(String message){
        super(message);
    }
}
