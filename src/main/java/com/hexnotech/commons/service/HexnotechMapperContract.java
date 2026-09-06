package com.hexnotech.commons.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface HexnotechMapperContract<T, F> {
    T convert(F from);

    default List<T> convert(Collection<F> fromList) {
        return Optional.ofNullable(fromList).orElse(Collections.emptyList()).stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }
}
