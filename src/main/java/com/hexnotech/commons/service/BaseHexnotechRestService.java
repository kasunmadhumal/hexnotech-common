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

import lombok.RequiredArgsConstructor;

import com.hexnotech.commons.exception.HexnotechNoDataFoundException;
import com.hexnotech.commons.jpa.entity.BaseEntity;
import com.hexnotech.commons.jpa.type.PredicationResolver;
import com.hexnotech.commons.type.api.HexnotechResponse;
import com.hexnotech.commons.type.api.SearchFilter;
import com.hexnotech.commons.util.HexnotechUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public abstract class BaseHexnotechRestService<SearchReq, Req, Res, E extends BaseEntity<I>, I>
        implements BaseHexnotechRestServiceContract<SearchReq, Req, Res, E, I> {

    private final BaseHexnotechRestServiceContractV2<E, I> baseService;
    private final BaseHexnotechMapper<Req, Res, E, I> mapper;
    private final PredicationResolver<SearchReq, E> predicationResolver;
    private final RequestValidator<SearchReq, Req> requestValidator;

    protected BaseHexnotechRestService(BaseHexnotechRestServiceContractV2<E, I> baseService,
                                      BaseHexnotechMapper<Req, Res, E, I> mapper,
                                      PredicationResolver<SearchReq, E> predicationResolver) {
        this.baseService = baseService;
        this.mapper = mapper;
        this.predicationResolver = predicationResolver;
        this.requestValidator = new DefaultRequestValidatorV2<>();
    }

    @Override
    public HexnotechResponse<Res> findAll(SearchReq req, SearchFilter searchFilter) {
        Specification<E> specification = predicationResolver.specification(req, searchFilter);
        return findByCustomQuery(specification, mapper.entityToResponseMapper());
    }

    @Override
    public HexnotechResponse<Res> findPage(SearchReq req, SearchFilter searchFilter) {
        Specification<E> specification = predicationResolver.specification(req, searchFilter);
        PageRequest pageRequest = searchFilter.toPageRequest();
        return findPageByCustomQuery(specification, mapper.entityToResponseMapper(), pageRequest);
    }

    @Override
    public HexnotechResponse<Res> findAllByFilter(Specification<E> specification) {
        return findByCustomQuery(specification, mapper.entityToResponseMapper());
    }

    @Override
    public HexnotechResponse<Res> findPageByFilter(Specification<E> specification, SearchFilter searchFilter) {
        PageRequest pageRequest = searchFilter.toPageRequest();
        return findPageByCustomQuery(specification, mapper.entityToResponseMapper(), pageRequest);
    }

    @Override
    public <CustomRes> HexnotechResponse<CustomRes> findByCustomQuery(
            Specification<E> specification,
            HexnotechMapperContract<CustomRes, E> mapperContract) {
        List<E> resultEntities = baseService.findAll(specification);
        List<CustomRes> responseList = mapperContract.convert(resultEntities);
        return new HexnotechResponse<>(responseList);
    }

    @Override
    public <CustomRes> HexnotechResponse<CustomRes> findPageByCustomQuery(
            Specification<E> specification, HexnotechMapperContract<CustomRes, E> mapperContract,
            PageRequest pageRequest) {
        Page<E> page = baseService.findAll(specification, pageRequest);
        List<CustomRes> responseList = mapperContract.convert(page.getContent());
        return new HexnotechResponse<>(responseList, page.getNumber(), page.getTotalPages());
    }

    @Override
    public HexnotechResponse<Res> findOne(SearchReq req, SearchFilter searchFilter) {
        Specification<E> specification = predicationResolver.specification(req, searchFilter);
        return findOne(specification, mapper::byEntity);
    }

    @Override
    public <CustomRes> HexnotechResponse<CustomRes> findOne(Specification<E> specification,
                                                           HexnotechMapperContract<CustomRes, E> mapperContract) {
        E resultEntity = baseService.findOne(specification).orElseThrow(() -> HexnotechNoDataFoundException.by("No data found"));
        CustomRes response = mapperContract.convert(resultEntity);
        return new HexnotechResponse<>(response);
    }

    @Override
    public HexnotechResponse<Res> findById(I id) {
        E entityById = baseService.findById(id)
                .orElseThrow(() -> HexnotechNoDataFoundException.by("No data found for id: " + id));
        Res response = mapper.byEntity(entityById);
        return new HexnotechResponse<>(response);
    }

    @Override
    public HexnotechResponse<Res> create(Req request) {
        validateRequest(request);
        preCreateProcess(request);
        E entity = mapper.byRequest(request);
        E savedEntity = baseService.save(entity);
        Res resultResponse = mapper.byEntity(savedEntity);
        postCreateProcess(request, savedEntity, resultResponse);
        return new HexnotechResponse<>(resultResponse);
    }

    protected void preCreateProcess(Req request) {
    }

    protected void postCreateProcess(Req request, E entity, Res resultResponse) {
    }

    @Override
    public HexnotechResponse<Res> create(Collection<Req> requests) {
        preCreateProcess(requests);
        List<E> entityList = mapper.byRequests(requests);
        List<E> savedEntityList = baseService.saveAll(entityList);
        postCreateProcess(requests, savedEntityList);
        List<Res> responseList = mapper.byEntities(savedEntityList);
        return new HexnotechResponse<>(responseList);
    }

    protected void preCreateProcess(Collection<Req> requests) {
    }

    protected void postCreateProcess(Collection<Req> requests, List<E> entities) {
    }

    public E update(E entity) {
        return baseService.update(entity.getId(), entity);
    }

    public E save(E entity) {
        return baseService.save(entity);
    }

    @Override
    public HexnotechResponse<Res> update(I id, Req request) {
        validateRequest(request);
        preUpdateProcess(id, request);
        E entity = mapper.byRequest(request);
        E savedEntity = baseService.update(id, entity);
        Res response = mapper.byEntity(savedEntity);
        postUpdateProcess(id, request, savedEntity, response);
        return new HexnotechResponse<>(response);
    }

    protected void preUpdateProcess(I id, Req request) {
    }

    @Override
    public void deleteById(I id) {
        baseService.deleteById(id);
    }

    @Override
    public E deleteOrThrowById(I id) {
        return baseService.deleteOrThrow(id);
    }

    protected void postUpdateProcess(I id, Req request, E entity, Res resultResponse) {}

    protected void validateRequest(Req request) {
        requestValidator.validate(request);
    }

    public HexnotechResponse<Res> partialUpdate(I id, Req request, Set<String> fieldsToUpdate) {
        validateRequest(request);
        E existingEntity = baseService.findById(id)
                .orElseThrow(() -> HexnotechNoDataFoundException.by("No data found for id: " + id));
        E requestEntity = mapper.byRequest(request);
        HexnotechUtil.copyProperties(requestEntity, existingEntity, fieldsToUpdate);
        E updatedEntity = baseService.update(id, existingEntity);
        Res response = mapper.byEntity(updatedEntity);
        return new HexnotechResponse<>(response);
    }
}
