package com.nexus.view;

import com.nexus.model.Command;

public interface InteractiveCLIHandler {
    String getTriggerWord();

    /**
     * Assume o controle do terminal para coletar os dados faltantes
     * e, ao final, despacha o comando para o Controller.
     */
    void processInteractive(PromptService prompt);

}
