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

package com.hexnotech.commons.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.hexnotech.commons.jpa.entity.BaseEntity;
import com.hexnotech.commons.service.BaseHexnotechRestServiceContractV2;
import com.hexnotech.commons.type.api.HexnotechResponse;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public abstract class BaseRestController<Req, Res, E extends BaseEntity<I>, I> {

    protected final BaseHexnotechRestServiceContractV2<E, I> jpaService;

    @GetMapping("/{id}")
    public ResponseEntity<Res> findById(@PathVariable I id) {
        var entity = jpaService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + id));
        return ResponseEntity.ok(toResponse(entity));
    }

    @PostMapping
    public ResponseEntity<Res> create(@RequestBody Req request) {
        E entity = toEntity(request);
        E saved = jpaService.save(entity);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Res>> createBulk(@RequestBody Collection<Req> requests) {
        List<E> entities = requests.stream()
                .map(this::toEntity)
                .toList();
        List<E> saved = jpaService.saveAll(entities);
        return ResponseEntity.ok(saved.stream()
                .map(this::toResponse)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Res> update(@PathVariable I id, @RequestBody Req request) {
        E entity = toEntity(request);
        E updated = jpaService.update(id, entity);
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable I id) {
        jpaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    protected abstract Res toResponse(E entity);

    protected abstract E toEntity(Req request);
}
