package com.nexus.service.persistence;

import java.time.LocalDate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonFactory {

    private static final Gson INSTANCE = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    private GsonFactory() {
    }

    public static Gson getInstance() {
        return INSTANCE;
    }
}
