package com.nexus.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Classe File Loader.
 */
public class FileLoader {
    private List<String> comandos = null;

    /**
     * Carrega um arquico com o nome solicitado
     * 
     * 
     * @param nome_arq um {@link String} com o nome do arquivo
     * @throws IOException se nao encontrar o arquivo
     */
    public void Carregar(String nome_arq) {
        try {
            var resource = getClass().getClassLoader().getResourceAsStream(nome_arq);

            if (resource == null) {
                throw new IOException("Arquivo não encontrado no classpath: " + nome_arq);
            }
            Scanner s = new Scanner(resource).useDelimiter("\\A");
            String content = s.hasNext() ? s.next() : "";
            // this.comandos = List.of(content.split("\\R"));
            this.comandos = Arrays.stream(content.split("\\R")).map(String::trim).toList();

            s.close();

        } catch (IOException e) {
            System.err.println("[ERRO FATAL] " + e.getMessage());
        }
    }

    /**
     * apos obter os comandos, retorna eles
     * 
     * @return um {@link List} com cada linha
     */
    public List<String> getComandos() {
        return comandos;
    }

}
