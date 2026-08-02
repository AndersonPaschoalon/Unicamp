package com.nexus.service.persistence;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson adapter for serializing and deserializing {@link LocalDate} values.
 */
public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Serializes a {@link LocalDate} as an ISO-8601 date string.
     *
     * @param date the local date to serialize
     * @param typeOfSrc the source type
     * @param context the serialization context
     * @return a JSON primitive containing the formatted date
     */
    @Override
    public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(date.format(formatter));
    }

    /**
     * Deserializes an ISO-8601 date string into a {@link LocalDate}.
     *
     * @param json the JSON element containing the date string
     * @param typeOfT the target type
     * @param context the deserialization context
     * @return the parsed local date
     * @throws JsonParseException if the date cannot be parsed
     */
    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return LocalDate.parse(json.getAsString(), formatter);
    }
}