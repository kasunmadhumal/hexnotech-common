/*
 * Copyright(C) 2025 ACCELaero
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

package com.hexnotech.commons.jpa.type;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.hexnotech.commons.exception.HexnotechProcessException;
import com.hexnotech.commons.type.notation.FieldAlias;
import com.hexnotech.commons.type.notation.IgnoreField;
import com.hexnotech.commons.util.HexnotechUtil;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import jakarta.persistence.Tuple;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class TupleProcessor<T> {

    private static final ConversionService conversionService = new DefaultConversionService();

    private final Class<T> clazz;

    public static <T> TupleProcessor<T> by(Class<T> clazz) {
        return new TupleProcessor<>(clazz);
    }

    public List<T> process(Collection<Tuple> tuples) {
        return process(tuples, (type, tuple) -> {});
    }

    public List<T> process(Collection<Tuple> tuples, BiConsumer<T, Tuple> customMapper) {
        return tuples.stream()
                .map(tuple -> process(tuple, customMapper))
                .collect(Collectors.toList());
    }

    public T process(Tuple tuple) {
        return process(tuple, (instance, paramTuple) -> {});
    }

    public T process(Tuple tuple, BiConsumer<T, Tuple> customMapper) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(IgnoreField.class)) {
                    continue;
                }
                field.setAccessible(true);
                Set<String> aliases = resolveAliases(field);
                Object value = resolveValue(aliases, tuple, field);
                if (value != null) {
                    field.set(instance, value);
                }
            }

            customMapper.accept(instance, tuple);
            return instance;
        } catch (Exception e) {
            throw new HexnotechProcessException("Error processing tuple to " + clazz.getSimpleName(), e);
        }
    }

    private Object resolveValue(Set<String> aliases, Tuple tuple, Field field) {
        for (String alias : aliases) {
            Object value;
            try {
                value = tuple.get(alias);
            } catch (IllegalArgumentException e) {
                continue;
            }

            if (value == null) {
                continue;
            }

            if (conversionService.canConvert(value.getClass(), field.getType())) {
                return conversionService.convert(value, field.getType());
            }
            return value;
        }
        return null;
    }

    private Set<String> resolveAliases(Field field) {
        if (field.isAnnotationPresent(FieldAlias.class)) {
            FieldAlias alias = field.getAnnotation(FieldAlias.class);
            return Set.of(alias.value());
        }

        String fieldName = field.getName();
        Set<String> aliases = new LinkedHashSet<>();
        aliases.add(fieldName);
        aliases.add(HexnotechUtil.toSnakeCase(fieldName));
        aliases.add(HexnotechUtil.toKebabCase(fieldName));
        aliases.add(fieldName.toLowerCase());
        return aliases;
    }
}
