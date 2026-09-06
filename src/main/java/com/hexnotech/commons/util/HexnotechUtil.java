/* * Copyright(C) 2025 ACCELaero
 * All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF
 * Information Systems Associates (pvt) Ltd.
 *
 * This copy of the Source Code is intended for Information Systems Associates (pvt) Ltd's internal
 * use only and is intended for view by persons duly authorized by the management of
 * Information Systems Associates (pvt) Ltd. No part of this file may be reproduced or distributed
 * in any form or by any means without the written approval of the Management of
 * Information Systems Associates (pvt) Ltd.
 */

package com.hexnotech.commons.util;

import com.hexnotech.commons.exception.HexnotechProcessException;
import com.hexnotech.commons.type.generic.SimpleProcess;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;


import com.hexnotech.commons.exception.HexnotechInternalException;

import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.security.SecureRandom;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import java.util.stream.Stream;
import java.util.UUID;

@Slf4j
public class HexnotechUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final char[] ALPHANUMERIC_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private HexnotechUtil() {
    }

    //----------------- Start UUID Utility Methods -----------------
    public static String nvlUuid(UUID value) {
        return nvlUuid(value, "");
    }

    public static String nvlUuid(UUID value, String defaultValue) {
        return value != null ? value.toString() : defaultValue;
    }

    //----------------- Start Long Utility Methods -----------------
    public static long nvlLong(Long value) {
        return nvlLong(value, 0);
    }

    public static long nvlLong(Long value, long defaultValue) {
        return value != null ? value : defaultValue;
    }

    //----------------- Start Double Utility Methods -----------------
    public static double nvlDouble(Double value) {
        return nvlDouble(value, 0);
    }

    public static double nvlDouble(Double value, double defaultValue) {
        return value != null ? value : defaultValue;
    }

    //------------------ Start Integer Utility Methods ------------------

    public static int nvlInteger(Integer value) {
        return nvlInteger(value, 0);
    }

    public static int nvlInteger(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }

    // ----------------- Start String Utility Methods -----------------

    public static void consumeNonEmpty(String value, Consumer<String> consumer) {
        if (isNotEmpty(value)) {
            consumer.accept(value);
        }
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static String nvl(String value) {
        return nvl(value, "");
    }

    public static String nvl(String value, String defaultValue) {
        return nvl(value, true, defaultValue);
    }

    public static String nvl(String value, boolean checkEmpty, String defaultValue) {
        return nvl(value, checkEmpty, param -> param, defaultValue);
    }

    public static String nvl(String value, UnaryOperator<String> formater, String defaultValue) {
        return nvl(value, true, formater, defaultValue);
    }

    public static String nvl(String value, boolean checkEmpty, UnaryOperator<String> formater, String defaultValue) {
        boolean isEmpty = value == null || (checkEmpty && StringUtils.isEmpty(value));
        return !isEmpty ? formater.apply(value) : defaultValue;
    }

    public static boolean isTrimmedEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    public static <T> String nvl(T value, Function<T, String> convertor) {
        return nvl(value, convertor, "");
    }

    public static String nvlTrim(String value) {
        return nvlTrim(value, HexnotechConstant.Symbol.EMPTY);
    }

    public static String nvlTrim(String value, String defaultValue) {
        return value != null ? value.trim() : defaultValue;
    }

    public static boolean withinCharacterLimit(String value, int min, int max) {
        if (value == null) return false;

        int length = value.length();
        return withinRange(length, min, max);
    }

    public static boolean nvlEqualIgnoreCase(String value1, String value2) {
        String formatedValue1 = HexnotechUtil.nvlTrim(value1, "");
        String formatedValue2 = HexnotechUtil.nvlTrim(value2, "");
        return formatedValue1.equalsIgnoreCase(formatedValue2);
    }

    public static String safeSubstring(String str, int start, int end) {
        String validatedStr = nvl(str);
        if (start >= 0 && end <= validatedStr.length() && start <= end ) {
            return validatedStr.substring(start, end);
        }
        return "";
    }

    public static String concat(String delimiter, String... values) {
        return concat(delimiter, Arrays.asList(values));
    }

    public static String concat(String delimiter, Collection<String> values) {
        return concat(delimiter, values, Function.identity());
    }

    public static <T> String concat(String delimiter, Collection<T> values, Function<T, String> converter) {
        if (isNotEmpty(values)) {
            return values.stream()
                    .map(converter)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.joining(delimiter));
        } else {
            return "";
        }
    }

    public static String toSnakeCase(String text) {
        return nvl(text).replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    public static String toKebabCase(String text) {
        return nvl(text).replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
    }

    /** Random alphanumeric string (A-Z, a-z, 0-9) using SecureRandom; delegates to Commons Lang. */
    public static String generateRandomString(int length) {
        if (length <= 0) {
            return "";
        }
        return RandomStringUtils.random(length, 0, ALPHANUMERIC_CHARS.length, false, false,
                ALPHANUMERIC_CHARS, new SecureRandom());
    }

    // ----------------- End String Utility Methods -----------------

    // ----------------- Start Number Utility Methods -----------------

    public static boolean isNumber(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositive(Number number) {
        return number != null && number.doubleValue() > 0;
    }
    public static boolean isPositiveOrZero(Number number) {
        return number != null && number.doubleValue() >= 0;
    }

    public static <T extends Number> void consumeNonNegative(Consumer<T> consumer, T value) {
        if (isPositiveOrZero(value)) {
            consumer.accept(value);
        }
    }

    // ----------------- End Number Utility Methods -----------------

    // ----------------- Start Collection Utility Methods -----------------

    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }

    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }
    public static <T> Collection<T> nvlList(Collection<T> list) {
        return nvlList(list, Collections.emptyList());
    }

    public static <C extends Collection<T>, T> C nvlList(C list, C defaultList) {
        return isNotEmpty(list) ? list : defaultList;
    }

    public static <T> Stream<T> toStream(Collection<T> collection) {
        return nvlList(collection).stream();
    }


    public static <T> Optional<T> getFirstElement(List<T> list) {
        return isEmpty(list) ? Optional.empty() : Optional.ofNullable(list.get(0));
    }

    public static <T> Optional<T> getLastElement(List<T> list) {
        return isEmpty(list) ? Optional.empty() : Optional.ofNullable(list.get(list.size() - 1));
    }

    public static <T> List<T> nvlToList(List<T> list) { return nvlList(list, Collections.emptyList()); }

    public static <T, R> List<R> nvlToTypeList(List<T> list, Function<T, R> mapper) {
        return nvlToTypeList(list, mapper, result -> true);
    }

    public static <T, R> List<R> nvlToTypeList(List<T> list, Function<T, R> mapper, List<R> defaultList) {
        return nvlToTypeList(list, mapper, result -> true, Collections.emptyList());
    }

    public static <T, R> List<R> nvlToTypeList(List<T> list, Function<T, R> mapper, Predicate<R> resultFilter) {
        return nvlToTypeList(list, mapper, resultFilter, Collections.emptyList());
    }

    public static <T, R> List<R> nvlToTypeList(List<T> list, Function<T, R> mapper, Predicate<R> resultFilter,
                                               List<R> defaultList) {
        List<R> mappedList = nvlToList(list).stream()
                .map(mapper)
                .filter(resultFilter)
                .collect(Collectors.toList());
        return nvlList(mappedList, defaultList);
    }

    public static List<Integer> splitToIntList(String value) {
        return splitToList(value, Integer::parseInt);
    }

    public static <T> List<T> splitToList(String value, Function<String, T> converter) {
        return splitToList(value, ",", converter, Collections.emptyList());
    }

    public static <T> List<T> splitToList(String value, String delimiter,
                                          Function<String, T> converter, List<T> defaultValue) {
        Function<String, List<T>> stringToTypeFunction = str -> Arrays.stream(str.split(delimiter))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(converter)
                .collect(Collectors.toList());
        return nvl(value, stringToTypeFunction, defaultValue);
    }

    public static <T> void consumeNonEmpty(Collection<T> collection, Consumer<Collection<T>> consumer) {
        if (isNotEmpty(collection)) {
            consumer.accept(collection);
        }
    }


    // ----------------- End Collection Utility Methods   -----------------

    // ----------------- Start Map Utility Methods -----------------

    public static boolean isMapNotEmpty(Map<?, ?> map) {
        return !isMapEmpty(map);
    }

    public static boolean isMapEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static <K, V> Map<K, V> nvlMap(Map<K, V> map) {
        return nvlMap(map, Collections.emptyMap());
    }

    public static <K, V> Map<K, V> nvlMap(Map<K, V> map, Map<K, V> defaultMap) {
        return isMapNotEmpty(map) ? map : defaultMap;
    }

    public static <K, V> Map<K, V> mergeMaps(Map<K, V> base, Map<K, V> overlay, BinaryOperator<V> mergeWhenPresent) {
        Map<K, V> result = new HashMap<>(base);
        if (isMapNotEmpty(overlay)) {
            overlay.forEach((k, v) -> result.merge(k, v, mergeWhenPresent));
        }
        return result;
    }

    public static <K, T, R, V> Map<R, V> nvlToMap(Map<K, T> map,
                                                  Predicate<Map.Entry<K, T>> filter,
                                                  Function<Map.Entry<K, T>, R> keyMapper,
                                                  Function<Map.Entry<K, T>, V> valueMapper) {
        return nvlMap(map).entrySet().stream()
                .filter(filter)
                .collect(Collectors.toMap(keyMapper, valueMapper));
    }

    public static <T, K, V> Map<K, V> nvlToListValueMap(Map<K, List<T>> map, BiFunction<K, List<T>, V> valueBuilder) {
        return nvlToMap(map,
                entry -> isNotEmpty(entry.getValue()),
                Map.Entry::getKey,
                entry -> valueBuilder.apply(entry.getKey(), entry.getValue()));
    }

    public static <K, V, U extends Comparable<? super U>> Map<K, V> sortMapByValueAttribute(
            Map<K, V> map, Function<V, U> valueExtractor) {

        return map.entrySet().stream()
                .sorted(Comparator.comparing(entry -> valueExtractor.apply(entry.getValue())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public static <K, V> Map<K, V> createMapWithKeys(Collection<K> keys, Function<K, V> valueSupplier) {
        return createMapWith(keys, key -> key, valueSupplier);
    }

    public static <K, V> Map<K, V> createMapWith(Collection<V> values, Function<V, K> keySupplier) {
        return createMapWith(values, keySupplier, value -> value);
    }

    public static <K, V, T> Map<K, V> createMapWith(Collection<T> keys, Function<T, K> keySupplier,
                                                    Function<T, V> valueSupplier) {
        Map<K, V> map = new HashMap<>();
        HexnotechUtil.nvlList(keys).forEach(type -> map.put(keySupplier.apply(type), valueSupplier.apply(type)));
        return map;
    }

    public static <T, K, R> Map<K, R> groupBy(Collection<T> items, Function<T, K> keyExtractor,
                                              Collector<T, ?, R> downstream) {
        if (isEmpty(items)) {
            return Collections.emptyMap();
        }
        return items.stream().collect(Collectors.groupingBy(keyExtractor, downstream));
    }

    public static <T> Map<Boolean, List<T>> partition(List<T> list, Predicate<T> predicate) {
        return list.stream().collect(Collectors.partitioningBy(predicate));
    }

    public static String toKeyValueString(Map<String, ?> map) {
        return map.entrySet().stream()
               .map(entry -> nvl(entry.getKey()) + "=" + nvl(entry.getValue(), Object::toString))
               .collect(Collectors.joining(", "));
    }

    // ----------------- End Map Utility Methods -----------------

    // ------------------ Start Stream Utility Methods -----------------
    public static <T, K> Collector<T, ?, Map<K, List<T>>> groupForList(Function<T, K> keyExtractor) {
        return groupByKeyTo(keyExtractor, Collectors.toList());
    }

    public static <T, K> Collector<T, ?, Map<K, Set<T>>> groupForSet(Function<T, K> keyExtractor) {
        return groupByKeyTo(keyExtractor, Collectors.toSet());
    }

    public static <T, K, C extends Collection<T>> Collector<T, ?, Map<K, C>> groupByKeyTo(
            Function<T, K> keyExtractor,
            Collector<T, ?, C> downstream) {
        return Collectors.groupingBy(keyExtractor, downstream);
    }
    // ----------------- End Collection Utility Methods   -----------------

    // ----------------- Start Object Utility Methods -----------------

    public static <T> boolean isEmpty(T value) {
        return value == null;
    }

    public static <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public static <T, R> R nvl(T source, Function<T, R> mapper, R defaultValue) {
        return Optional.ofNullable(source).map(mapper).orElse(defaultValue);
    }

    public static <T> T testAndGet(T value, Predicate<T> predicate, T defaultValue) {
        return testAndGet(value, predicate, t -> t, defaultValue);
    }

    public static <T, R> R testAndGet(T value, Predicate<T> predicate, Function<T, R> converter, R defaultValue) {
        return predicate.test(value) ? converter.apply(value) : defaultValue;
    }

    public static <T> Optional<T> handleExecution(Supplier<T> supplier) {
        return handleExecution(supplier, exception -> log.error("Exception Occurred", exception));
    }

    public static <T> Optional<T> handleExecution(Supplier<T> supplier, Consumer<Exception > exceptionConsumer) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Exception exception) {
            exceptionConsumer.accept(exception);
            return Optional.empty();
        }
    }

    public static <C extends Collection<T>, R extends Collection<X>, T, X> R nvlToTypeList(C list,
                                                                                           Function<C, R> resultMapper,
                                                                                           Supplier<R> defaultList) {
        return isNotEmpty(list) ? resultMapper.apply(list) : defaultList.get();
    }

    public static <T> boolean nvlEqual(T value1, T value2) {
        return Objects.equals(value1, value2);
    }

    public static <T> void copyProperties(T source, T target, Set<String> fieldsToUpdate) {
        BeanWrapper srcWrapper = new BeanWrapperImpl(source);
        BeanWrapper trgWrapper = new BeanWrapperImpl(target);

        for (PropertyDescriptor propertyDescriptor : srcWrapper.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            if (fieldsToUpdate.contains(propertyName)) {
                Object value = srcWrapper.getPropertyValue(propertyName);
                trgWrapper.setPropertyValue(propertyName, value);
            }
        }
    }


    public static byte[] generatePdfFromHtml(String module, String htmlContent) {
        log.info("{} Starting PDF generation from HTML content", module);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(outputStream);
            builder.run();

            log.info("{} PDF generation successful, size={} bytes", module, outputStream.size());
            return outputStream.toByteArray();
        } catch (Exception exception) {
            log.error(module + " PDF generation failed", exception);
            throw new HexnotechInternalException(module + " PDF generation failed");
        }
    }

    public static <T extends Comparable> boolean withinRange(T value, T min, T max) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    public static <E extends Enum<E>> List<String> enumToStringList(Collection<E> enumList) {
        return toStream(enumList)
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    public static <E extends Enum<E>> List<E> stringToEnumList(Collection<String> stringList, Class<E> enumClass) {
        return stringToEnumList(stringList, Function.identity(), enumClass);
    }

    public static <T, E extends Enum<E>> List<E> stringToEnumList(Collection<T> list, Function<T, String> enumSupplier,
                                                                  Class<E> enumClass) {
        return toStream(list)
                .map(enumSupplier)
                .map(name -> Enum.valueOf(enumClass, name))
                .collect(Collectors.toList());
    }

    public static <E extends Enum<E>> Optional<E> getByValue(Class<E> enumClass, String value) {
        if (isNotEmpty(value)) {
            return Arrays.stream(enumClass.getEnumConstants())
                    .filter(e -> e.name().equalsIgnoreCase(value))
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static <T, K> List<T> mergeByKey(Collection<T> items, Function<? super T, ? extends K> keyExtractor,
                                            BinaryOperator<T> merger) {
        if (isEmpty(items)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(items.stream()
                .collect(Collectors.toMap(keyExtractor, Function.identity(), merger, LinkedHashMap::new))
                .values());
    }

    public static <T> void consumeNonNull(T value, Consumer<T> consumer) {
        if (!HexnotechUtil.isEmpty(value)) {
            consumer.accept(value);
        }
    }

    public static <T, R> R applyNonNull(T value, Function<T, R> function, R defaultValue) {
        if (!HexnotechUtil.isEmpty(value)) {
            return function.apply(value);
        }
        return defaultValue;
    }

    // ----------------- End Object Utility Methods -----------------
    
    // ----------------- JSON Utility Methods -----------------

    public static ObjectNode createJsonObjectNode() {
        return objectMapper.createObjectNode();
    }

    private static JsonNode parseJsonOrThrow(String messageBody) {
        try {
            return objectMapper.readTree(messageBody);
        } catch (Exception e) {
            throw new HexnotechProcessException("Invalid JSON", e);
        }
    }

    public static <T> Optional<T> fromJson(String json, TypeReference<T> typeReference) {
        try {
            T result = fromJsonOrThrow(json, typeReference);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static <T> T fromJsonOrThrow(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("Error deserializing JSON to {}: {}", typeReference.getType(), e.getMessage(), e);
            throw new HexnotechProcessException("Error deserializing JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Error deserializing JSON to {}: {}", clazz.getSimpleName(), e.getMessage(), e);
            throw new HexnotechProcessException("Error deserializing JSON", e);
        }
    }

    public static <T> String toJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Error serializing {} to JSON: {}",
                    object != null ? object.getClass().getSimpleName() : "null",
                    e.getMessage(), e);
            throw new HexnotechProcessException("Error serializing to JSON", e);
        }
    }

    public static Optional<String> getParsedMessageValue(String messageBody, String jsonPointer) {
        JsonNode root = parseJsonOrThrow(messageBody);
        return extractValue(root, jsonPointer);
    }

    public static Map<String, String> getParsedMessageValueMap(String messageBody, Collection<String> jsonPointers) {
        JsonNode root = parseJsonOrThrow(messageBody);

        Map<String, String> result = new HashMap<>();
        for (String jsonPointer : jsonPointers) {
            Optional<String> elementJsonTextHolder = extractValue(root, jsonPointer);
            elementJsonTextHolder.ifPresent(elementJsonText -> result.put(jsonPointer, elementJsonText));
        }
        return result;
    }

    public static Optional<String> extractValue(JsonNode jsonNode, String jsonPointer) {
        String normalizedPointer = jsonPointer.startsWith("/") ? jsonPointer : "/" + jsonPointer;
        JsonNode element = jsonNode.at(normalizedPointer);
        if (null != element && !element.isMissingNode() && !element.isNull()) {
            String elementJsonText = element.isValueNode() ? element.asText() : element.toString();
            return Optional.ofNullable(elementJsonText);
        }
        return Optional.empty();
    }

    // ----------------- End JSON Utility Methods -----------------

    // ----------------- Start Generic Utility Methods -----------------
    public static void consume(boolean condition, SimpleProcess action) {
        if (condition) {
            action.execute();
        }
    }
    // ------------------ End Generic Utility Methods -----------------
}
