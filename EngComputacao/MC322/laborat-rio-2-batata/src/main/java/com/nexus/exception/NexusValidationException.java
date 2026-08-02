package com.nexus.exception;



public class NexusValidationException extends RuntimeException {
    /**
     * Funcao usada para detectar erros de validacao no codigo
     *
     * @param Mensagem uma {@link String} que explica o erro
     */
    public NexusValidationException(String message) {
        super(message);
    }
}
