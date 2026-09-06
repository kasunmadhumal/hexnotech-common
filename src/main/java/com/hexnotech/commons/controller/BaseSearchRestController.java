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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hexnotech.commons.jpa.entity.BaseEntity;
import com.hexnotech.commons.service.BaseHexnotechRestServiceContract;
import com.hexnotech.commons.type.api.HexnotechResponse;
import com.hexnotech.commons.type.api.SearchFilter;

@RequiredArgsConstructor
public abstract class BaseSearchRestController<SearchReq, Res, E extends BaseEntity<I>, I> {

    protected final BaseHexnotechRestServiceContract<SearchReq, Void, Res, E, I> searchService;

    @GetMapping
    public ResponseEntity<HexnotechResponse<Res>> search(
            SearchReq searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        SearchFilter searchFilter = SearchFilter.of(size, page);
        return ResponseEntity.ok(searchService.findPage(searchRequest, searchFilter));
    }

    @GetMapping("/all")
    public ResponseEntity<HexnotechResponse<Res>> searchAll(SearchReq searchRequest) {
        SearchFilter searchFilter = SearchFilter.of(Integer.MAX_VALUE, 0);
        return ResponseEntity.ok(searchService.findAll(searchRequest, searchFilter));
    }

    @GetMapping("/one")
    public ResponseEntity<HexnotechResponse<Res>> searchOne(SearchReq searchRequest) {
        SearchFilter searchFilter = SearchFilter.of(1, 0);
        return ResponseEntity.ok(searchService.findOne(searchRequest, searchFilter));
    }
}
