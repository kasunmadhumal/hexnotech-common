/*
 *
 *  * Copyright(C) 2025 ACCELaero
 *  * All rights reserved.
 *  * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF
 *  * Information Systems Associates (pvt) Ltd.
 *  *
 *  * This copy of the Source Code is intended for Information Systems Associates (pvt) Ltd's internal
 *  * use only and is intended for view by persons duly authorized by the management of
 *  * Information Systems Associates (pvt) Ltd. No part of this file may be reproduced or distributed
 *  * in any form or by any means without the written approval of the Management of
 *  * Information Systems Associates (pvt) Ltd.
 *
 */

package com.hexnotech.commons.service;

import com.hexnotech.commons.jpa.entity.BaseEntity;
import com.hexnotech.commons.type.api.HexnotechResponse;
import com.hexnotech.commons.type.api.SearchFilter;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.Set;

public interface BaseHexnotechRestServiceContract<SearchReq, Req, Res, E extends BaseEntity<I>, I> {

    HexnotechResponse<Res> findAll(SearchReq req, SearchFilter searchFilter);

    HexnotechResponse<Res> findPage(SearchReq req, SearchFilter searchFilter);

    HexnotechResponse<Res> findAllByFilter(Specification<E> specification);

    HexnotechResponse<Res> findPageByFilter(Specification<E> specification, SearchFilter searchFilter);

    <CustomRes> HexnotechResponse<CustomRes> findByCustomQuery(Specification<E> specification,
                                                              HexnotechMapperContract<CustomRes, E> mapperContract);

    <CustomRes> HexnotechResponse<CustomRes> findPageByCustomQuery(Specification<E> specification,
                                                                  HexnotechMapperContract<CustomRes, E> mapperContract,
                                                                  PageRequest pageRequest);

    HexnotechResponse<Res> findOne(SearchReq req, SearchFilter searchFilter);

    <CustomRes> HexnotechResponse<CustomRes> findOne(Specification<E> specification,
                                                    HexnotechMapperContract<CustomRes, E> mapperContract);

    HexnotechResponse<Res> findById(I id);

    HexnotechResponse<Res> create(Req request);

    HexnotechResponse<Res> create(Collection<Req> request);

    HexnotechResponse<Res> update(I id, Req request);

    void deleteById(I id);

    E deleteOrThrowById(I id);

    HexnotechResponse<Res> partialUpdate(I id, Req request, Set<String> fieldsToUpdate);

}
