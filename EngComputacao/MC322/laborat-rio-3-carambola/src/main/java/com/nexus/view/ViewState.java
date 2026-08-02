package com.nexus.view;

public interface ViewState {

    /**
     * Renderiza o conteúdo da tela no terminal.
     */
    void render(PromptService prompt);

    /**
     * Processa a escolha do usuário e retorna o próximo estado (Tela).
     * Retorna 'this' para se manter na mesma tela, ou 'null' para sair do modo de navegação.
     */
    ViewState handleInput(String input);

}
