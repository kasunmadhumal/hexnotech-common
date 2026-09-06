/*
 * Copyright(C) 2026 ACCELaero
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

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@UtilityClass
public class HexnotechReflectionUtil {

    public static <T> Set<Object> getStaticFieldValues(Class<T> clazz) {
        return getStaticFieldsValues(clazz, field -> true);
    }

    public static <T> Set<String> getStringStaticFieldValues(Class<T> clazz) {
        return getStaticFieldsValues(clazz, field -> field.getType().equals(String.class));
    }

    @SuppressWarnings("unchecked")
    public static <T, R> Set<R> getStaticFieldsValues(Class<T> clazz, Predicate<Field> fieldFilter) {
        Predicate<Field> staticFilter = fieldFilter.and(field -> Modifier.isStatic(field.getModifiers()));
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(staticFilter)
                .map(field -> {
                    try {
                        return (R) field.get(null);
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static <T> Map<Field, Object> getFieldsValueMap(
            T instance, Class<? extends Annotation> annotationClass) {
        return getFieldsValueMap(instance, annotationClass, annotation -> true);
    }

    public static <T, A extends Annotation> Map<Field, Object> getFieldsValueMap(
            T instance, Class<A> annotationClass, Predicate<A> predicate) {
        return getFieldsMap(
                instance, annotationClass, predicate,
                field -> field, field -> ReflectionUtils.getField(field, instance)
        );
    }

    public static <T> Map<String, Object> getFieldsNameValueMap(
            T instance, Class<? extends Annotation> annotationClass) {
        return getFieldsNameValueMap(instance, annotationClass, annotation -> true);
    }

    public static <T, A extends Annotation> Map<String, Object> getFieldsNameValueMap(
            T instance, Class<A> annotationClass, Predicate<A> predicate) {
        return getFieldsMap(
                instance, annotationClass, predicate,
                Field::getName, field -> ReflectionUtils.getField(field, instance)
        );
    }

    public static <T> Map<String, Pair<Type, Object>> getFieldsNameTypeMap(
            T instance, Class<? extends Annotation> annotationClass) {
        return getFieldsMap(
                instance, annotationClass, annotation -> true,
                Field::getName, field -> Pair.of(field.getGenericType(), ReflectionUtils.getField(field, instance))
        );
    }

    public static <T, K, V, A extends Annotation> Map<K, V> getFieldsMap(
            T instance, Class<A> annotationClass, Predicate<A> annotationPredicate,
            Function<Field, K> keyMapper, Function<Field, V> valueMapper) {
        return getFields(instance.getClass(), annotationClass, annotationPredicate).stream()
                .collect(Collectors.toMap(keyMapper, valueMapper));
    }

    public static List<Field> getFields(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return getFields(clazz, annotationClass, annotation -> true);
    }

    public static <T extends Annotation> List<Field> getFields(Class<?> clazz, Class<T> annotationClass,
                                                               Predicate<T> annotationFilter) {
        Predicate<Field> fieldPredicate = field -> Optional.ofNullable(field.getAnnotation(annotationClass))
                .map(annotationFilter::test)
                .orElse(false);
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(fieldPredicate)
                .peek(ReflectionUtils::makeAccessible)
                .collect(Collectors.toList());
    }

}
