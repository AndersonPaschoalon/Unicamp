package com.nexus.service.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.nexus.model.Entity;

public class JsonlRepository<T extends Entity> implements Repository<T, String> {
    private final Path filePath;
    private final Class<T> type;
    private final Gson gson;
    private final Map<String, T> cache = new LinkedHashMap<>();

    public JsonlRepository(Path filePath, Class<T> type, Gson gson) {
        this.filePath = filePath;
        this.type = type;
        this.gson = gson;
        rehydrate();
    }

    private void rehydrate() {
        if (!Files.exists(filePath)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                T entity = gson.fromJson(line, type);
                cache.put(entity.getId(), entity);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao reidratar " + filePath, e);
        }
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(cache.values());
    }

    @Override
    public void save(T entity) {
        cache.put(entity.getId(), entity);
        String json = gson.toJson(entity, type);
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            Files.writeString(filePath, json + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao persistir em " + filePath, e);
        }
    }
}