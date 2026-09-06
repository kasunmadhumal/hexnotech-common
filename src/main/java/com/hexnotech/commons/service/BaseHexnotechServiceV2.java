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

import lombok.RequiredArgsConstructor;

import com.hexnotech.commons.exception.HexnotechNoDataFoundException;
import com.hexnotech.commons.jpa.entity.BaseEntity;
import com.hexnotech.commons.jpa.repository.BaseHexnotechRepository;
import com.hexnotech.commons.util.HexnotechReflectionUtil;
import com.hexnotech.commons.type.api.SearchFilter;
import com.hexnotech.commons.util.PredicateUtil;
import com.hexnotech.commons.util.ValidatorUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


/**
 * @param <E>
 * @param <I>
 */
@RequiredArgsConstructor
public abstract class BaseHexnotechServiceV2<E extends BaseEntity<I>, I>
        implements BaseHexnotechRestServiceContractV2<E, I> {

    private final BaseHexnotechRepository<E, I> hexnotechRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<E> findAll() {
        return hexnotechRepository.findAll();
    }

    @Override
    public List<E> findAll(SearchFilter searchFilter) {
        return hexnotechRepository.findAll(searchFilter.sort());
    }

    @Override
    public Page<E> findPage(SearchFilter searchFilter) {
        return findAll(PredicateUtil.defaultSpecification(), searchFilter.toPageRequest());
    }

    @Override
    public List<E> findAll(Specification<E> specification) {
        return hexnotechRepository.findAll(specification);
    }

    public Page<E> findAll(Specification<E> specification, PageRequest pageRequest) {
        return hexnotechRepository.findAll(specification, pageRequest);
    }

    @Override
    public Optional<E> findOne(Specification<E> specification) {
        return hexnotechRepository.findOne(specification);
    }

    @Override
    public Optional<E> findById(I id) {
        return hexnotechRepository.findById(id);
    }

    @Override
    public List<E> findByIds(Collection<I> id) {
        return hexnotechRepository.findAllById(id);
    }

    @Override
    @Transactional
    public E save(E entity) {
        entity.setId(null);
        preCreateProcess(entity);
        E savedEntity = hexnotechRepository.save(entity);
        postCreateProcess(entity, savedEntity);
        return savedEntity;
    }

    protected void preCreateProcess(E requestEntity) {
        validate(requestEntity);
    }

    protected void postCreateProcess(E requestEntity, E savedEntity) {
    }

    @Override
    @Transactional
    public List<E> saveAll(Collection<E> entityList) {
        return hexnotechRepository.saveAll(entityList);
    }

    @Override
    @Transactional
    public E update(I id, E entity) {
        entity.setId(id);
        Optional<E> existedEntityOptional = hexnotechRepository.findById(id);
        preUpdateProcess(existedEntityOptional, entity);
        existedEntityOptional.ifPresent(this::detachEntity);
        E savedEntity = hexnotechRepository.save(entity);
        postUpdateProcess(existedEntityOptional, savedEntity);
        return savedEntity;
    }

    protected void preUpdateProcess(Optional<E> existingEntity,E requestEntity) {
        validate(requestEntity);
        preDataPopulate(existingEntity, requestEntity);
    }

    protected void preDataPopulate(Optional<E> existingEntity, E requestEntity) {
        existingEntity.ifPresent(entity -> {
            Predicate<Column> insertOnlyColumsPredicate = column -> column.insertable() && !column.updatable();
            HexnotechReflectionUtil.getFieldsValueMap(entity, Column.class, insertOnlyColumsPredicate)
                    .forEach((field, value) -> ReflectionUtils.setField(field, requestEntity, value));
        });
    }

    protected void postUpdateProcess(Optional<E> existedEntity, E savedEntity) {
    }

    @Override
    @Transactional
    public void deleteById(I id) {
        hexnotechRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByIds(Collection<I> ids) {
        hexnotechRepository.deleteAllById(ids);
    }

    @Override
    @Transactional
    public E deleteOrThrow(I id) {
        return  hexnotechRepository.findById(id)
                .map(entity -> {
                    hexnotechRepository.deleteById(id);
                    return entity;
                })
                .orElseThrow(() -> new HexnotechNoDataFoundException("No entity found with id: " + id));
    }

    protected void validate(E entity) {
        ValidatorUtil.validate(entity);
    }

    public  <T> void detachEntity(T entity) {
        if (entity != null) {
            entityManager.detach(entity);
        }
    }

    @Transactional
    public void deleteInBatch(Iterable<E> entityList) {
        hexnotechRepository.deleteInBatch(entityList);
    }
}
