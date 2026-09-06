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

package com.hexnotech.commons.service;

import com.hexnotech.commons.exception.HexnotechNoDataFoundException;
import com.hexnotech.commons.jpa.entity.BaseEntity;
import com.hexnotech.commons.type.api.SearchFilter;
import com.hexnotech.commons.util.HexnotechUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @param <E>
 * @param <I>
 */
public interface BaseHexnotechRestServiceContractV2<E extends BaseEntity<I>, I> {

    List<E> findAll();

    List<E> findAll(SearchFilter searchFilter);

    Page<E> findPage(SearchFilter searchFilter);

    List<E> findAll(Specification<E> specification);

    Page<E> findAll(Specification<E> specification, PageRequest pageRequest);

    Optional<E> findOne(Specification<E> specification);

    Optional<E> findById(I id);

    default E findByIdOrThrow(I id, Supplier<String> messageSupplier) {
        return findById(id).orElseThrow(() -> HexnotechNoDataFoundException.by(messageSupplier.get()));
    }

    List<E> findByIds(Collection<I> id);

    default Map<I, E> findAndMapByIds(Collection<I> ids) {
        if (HexnotechUtil.isNotEmpty(ids)) {
            return findByIds(ids).stream().collect(
                    Collectors.toMap(BaseEntity::getId, entity -> entity)
            );
        } else {
            return Map.of();
        }
    }

    E save(E entity);

    List<E> saveAll(Collection<E> entityList);

    E update(I id, E entity);

    void deleteById(I id);

    void deleteByIds(Collection<I> ids);

    E deleteOrThrow(I id);

}
