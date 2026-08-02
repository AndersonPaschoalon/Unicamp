package com.nexus.service.persistence;

import com.nexus.service.Workspace;
import com.nexus.service.core.UserService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.time.LocalDate;

public class PersistenceService {
    private final Gson gson;
    private static final String FILE_PATH = "nexus_state.json";

    public PersistenceService() {
        // O PrettyPrinting deixa o JSON legível para humanos
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter()) // A mágica acontece aqui
                .enableComplexMapKeySerialization() // <<<<<<<<<<<<<<< Nova linha
                .setPrettyPrinting()
                .create();
    }

    public void save(Workspace workspace, UserService userService) {
        NexusData data = new NexusData(workspace, userService);
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(data, writer);
            System.out.println("[SISTEMA] Estado salvo com sucesso em " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao salvar o estado: " + e.getMessage());
        }
    }

    public NexusData load() {
        File file = new File(FILE_PATH);
        if (!file.exists())
            return new NexusData(new Workspace(), new UserService()); // Se não existir, começa limpo

        try (Reader reader = new FileReader(FILE_PATH)) {
            return gson.fromJson(reader, NexusData.class);
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao carregar estado. Iniciando novo.");
            return new NexusData(new Workspace(), new UserService());
        }
    }
}
