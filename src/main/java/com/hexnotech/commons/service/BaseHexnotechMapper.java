package com.hexnotech.commons.service;

import com.hexnotech.commons.jpa.entity.BaseEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface BaseHexnotechMapper<Req, Res, E extends BaseEntity<I>, I> {

    E byRequest(Req req);
    Res byEntity(E entity);

    default List<E> byRequests(Collection<Req> requests) {
        return Optional.ofNullable(requests).orElse(Collections.emptyList()).stream()
                .map(this::byRequest)
                .collect(Collectors.toList());
    }

    default List<Res> byEntities(Collection<E> entities) {
        return Optional.ofNullable(entities).orElse(Collections.emptyList()).stream()
                .map(this::byEntity)
                .collect(Collectors.toList());
    }

    default HexnotechMapperContract<Res, E> entityToResponseMapper() {
        return new HexnotechMapperContract<>() {
            @Override
            public List<Res> convert(Collection<E> fromList) {
                return byEntities(fromList);
            }

            @Override
            public Res convert(E from) {
                return byEntity(from);
            }
        };
    }
}
