package com.nexus.exception;


/*
Representa falhas da máquina ou de infraestrutura (ex: falha de I/O em arquivos, injeção de dependência falha). O sistema não deve tentar recuperar-se silenciosamente destas; elas indicam um problema estrutural.
*/
public class NexusSystemException extends NexusException {
    

    public NexusSystemException(String message) {
        super(message);
    }
    
}
