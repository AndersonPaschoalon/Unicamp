package com.nexus.model.id;

/**
 * Estratégia trocável para geração de identificadores de Entidades.
 */
public interface IdStrategy {

    /**
     * Gera um identificador novo e único a partir de um valor.
     */
    String generate(String seed);
}