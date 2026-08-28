package com.hexnotech.commons.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.Map;

/**
 * Utility helpers for JSON serialisation / deserialisation using Jackson.
 * <p>
 * A single shared {@link ObjectMapper} is configured with:
 * <ul>
 *   <li>JavaTimeModule — handles {@code LocalDateTime}, {@code ZonedDateTime}, etc.</li>
 *   <li>{@code FAIL_ON_UNKNOWN_PROPERTIES = false} — tolerant reads</li>
 *   <li>{@code WRITE_DATES_AS_TIMESTAMPS = false} — ISO-8601 string dates</li>
 * </ul>
 *
 * <pre>{@code
 * String json  = JsonUtils.toJson(myObject);
 * MyDto  dto   = JsonUtils.fromJson(json, MyDto.class);
 * Map<String, Object> map = JsonUtils.toMap(myObject);
 * }</pre>
 */
public final class JsonUtils {

    public static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private JsonUtils() {}

    /**
     * Serialises an object to a JSON string.
     *
     * @throws IllegalArgumentException if serialisation fails
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialise object to JSON", e);
        }
    }

    /**
     * Serialises an object to a pretty-printed JSON string.
     */
    public static String toPrettyJson(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialise object to JSON", e);
        }
    }

    /**
     * Deserialises a JSON string to an instance of {@code clazz}.
     *
     * @throws IllegalArgumentException if deserialisation fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to deserialise JSON to " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Deserialises a JSON string using a {@link TypeReference} — useful for generics like
     * {@code List<MyDto>} or {@code Map<String, Object>}.
     *
     * <pre>{@code
     * List<UserDto> users = JsonUtils.fromJson(json, new TypeReference<>() {});
     * }</pre>
     */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to deserialise JSON", e);
        }
    }

    /**
     * Converts any object to a {@code Map<String, Object>} via JSON round-trip.
     * Useful for dynamic property access or merging objects.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object obj) {
        return MAPPER.convertValue(obj, Map.class);
    }

    /**
     * Maps one type to another via Jackson's {@code convertValue} (no actual JSON string involved).
     * Handy as a lightweight object mapper alternative to MapStruct for simple cases.
     */
    public static <T> T convert(Object obj, Class<T> targetType) {
        return MAPPER.convertValue(obj, targetType);
    }
}
