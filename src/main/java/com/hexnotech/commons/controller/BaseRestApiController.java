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
import org.springframework.web.bind.annotation.RequestParam;

import com.hexnotech.commons.jpa.entity.BaseEntity;
import com.hexnotech.commons.service.BaseHexnotechRestServiceContract;
import com.hexnotech.commons.type.api.HexnotechResponse;
import com.hexnotech.commons.type.api.SearchFilter;

import java.util.Collection;
import java.util.Set;

@RequiredArgsConstructor
public abstract class BaseRestApiController<SearchReq, Req, Res, E extends BaseEntity<I>, I> {

    protected final BaseHexnotechRestServiceContract<SearchReq, Req, Res, E, I> service;

    @GetMapping
    public ResponseEntity<HexnotechResponse<Res>> findAll(
            SearchReq searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        SearchFilter searchFilter = SearchFilter.of(size, page);
        return ResponseEntity.ok(service.findAll(searchRequest, searchFilter));
    }

    @GetMapping("/page")
    public ResponseEntity<HexnotechResponse<Res>> findPage(
            SearchReq searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        SearchFilter searchFilter = SearchFilter.of(size, page);
        return ResponseEntity.ok(service.findPage(searchRequest, searchFilter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HexnotechResponse<Res>> findById(@PathVariable I id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<HexnotechResponse<Res>> create(@RequestBody Req request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PostMapping("/bulk")
    public ResponseEntity<HexnotechResponse<Res>> createBulk(@RequestBody Collection<Req> requests) {
        return ResponseEntity.ok(service.create(requests));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HexnotechResponse<Res>> update(
            @PathVariable I id,
            @RequestBody Req request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PutMapping("/{id}/partial")
    public ResponseEntity<HexnotechResponse<Res>> partialUpdate(
            @PathVariable I id,
            @RequestBody Req request,
            @RequestParam Set<String> fields) {
        return ResponseEntity.ok(service.partialUpdate(id, request, fields));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable I id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/or-throw")
    public ResponseEntity<HexnotechResponse<Res>> deleteOrThrowById(@PathVariable I id) {
        E deleted = service.deleteOrThrowById(id);
        return ResponseEntity.ok(new HexnotechResponse<>(null));
    }
}
