package com.nexus.model;

/**
 * Contrato comum a todas as Entidades (Raízes de Agregação) do domínio Nexus.
 */
public interface Entity {

    /**
     * Retorna o identificador único e estável desta Entidade.
     *
     * @return o identificador da Entidade;
     */
    String getId();
}