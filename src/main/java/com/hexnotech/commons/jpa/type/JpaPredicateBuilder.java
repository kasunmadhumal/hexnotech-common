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

package com.hexnotech.commons.jpa.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.hexnotech.commons.util.HexnotechUtil;
import com.hexnotech.commons.util.PredicateUtil;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class JpaPredicateBuilder<E> {

    private final CriteriaBuilder criteriaBuilder;
    @Getter
    private final Root<E> root;
    private final CriteriaQuery<?> criteriaQuery;
    private final List<Predicate> predicates = new ArrayList<>();

    public static <E> JpaPredicateBuilder<E> of(CriteriaBuilder criteriaBuilder, CriteriaQuery<E> criteriaQuery,
                                                Root<E> root) {
        return new JpaPredicateBuilder<>(criteriaBuilder, root, criteriaQuery);
    }

    public static <E> JpaPredicateBuilder<E> of(CriteriaBuilder criteriaBuilder, Root<E> root, Class<E> clazz) {
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(clazz);
        return new JpaPredicateBuilder<>(criteriaBuilder, root, criteriaQuery);
    }

    public static <E> JpaPredicateBuilder<E> of(CriteriaBuilder criteriaBuilder, Class<E> clazz) {
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(clazz);
        Root<E> root = criteriaQuery.from(clazz);
        return new JpaPredicateBuilder<>(criteriaBuilder, root, criteriaQuery);

    }

    public JpaPredicateBuilder<E> equalStr(String fieldName, String value) {
        if (HexnotechUtil.isNotEmpty(value)) {
            equal(fieldName, value);
        }
        return this;
    }

    public JpaPredicateBuilder<E> preLike(String fieldName, String value) {
        if (HexnotechUtil.isNotEmpty(value)) {
            predicates.add(PredicateUtil.preLike(criteriaBuilder, root, fieldName, value));
        }
        return this;
    }

    public JpaPredicateBuilder<E> postLie(String fieldName, String value) {
        if (HexnotechUtil.isNotEmpty(value)) {
            predicates.add(PredicateUtil.postLike(criteriaBuilder, root, fieldName, value));
        }
        return this;
    }

    public JpaPredicateBuilder<E> likeStr(String fieldName, String value) {
        if (HexnotechUtil.isNotEmpty(value)) {
            predicates.add(PredicateUtil.likeWildCard(criteriaBuilder, root, fieldName, value));
        }
        return this;
    }

    public JpaPredicateBuilder<E> equal(String fieldName, Object value) {
        if (null != value) {
            predicates.add(criteriaBuilder.equal(root.get(fieldName), value));
        }
        return this;
    }

    public <Y extends Comparable<? super Y>> JpaPredicateBuilder<E> between(String fieldName, Y from, Y to) {
        predicates.add(criteriaBuilder.between(root.get(fieldName), from, to));
        return this;
    }

    public JpaPredicateBuilder<E> inOrAll(String fieldName, Collection<?> values) {
        if (HexnotechUtil.isNotEmpty(values)) {
            predicates.add(root.get(fieldName).in(values));
        }
        return this;
    }

    public JpaPredicateBuilder<E> inOrNone(String fieldName, Collection<?> values) {
        Predicate predicate = HexnotechUtil.isNotEmpty(values) ?
                root.get(fieldName).in(values) : criteriaBuilder.disjunction();
        predicates.add(predicate);
        return this;
    }

    public JpaPredicateBuilder<E> add(Predicate... paramPredicates) {
        predicates.addAll(Arrays.asList(paramPredicates));
        return this;
    }

    public JpaPredicateBuilder<E> notNull(String fieldName) {
        predicates.add(criteriaBuilder.isNotNull(root.get(fieldName)));
        return this;
    }

    public <V extends Comparable<? super V>> JpaPredicateBuilder<E> gt(String fieldName, V value) {
        if (value != null) {
            predicates.add(PredicateUtil.greaterThan(criteriaBuilder, root, fieldName, value));
        }
        return this;
    }

    public <V extends Comparable<? super V>> JpaPredicateBuilder<E> gte(String fieldName, V value) {
        if (value != null) {
            predicates.add(PredicateUtil.greaterThanOrEqualTo(criteriaBuilder, root, fieldName, value));
        }
        return this;
    }

    public <V extends Comparable<? super V>> JpaPredicateBuilder<E> lt(String fieldName, V value) {
        if (value != null) {
            predicates.add(PredicateUtil.lessThan(criteriaBuilder, root, fieldName, value));
        }
        return this;
    }

    public <V extends Comparable<? super V>> JpaPredicateBuilder<E> lte(String fieldName, V value) {
        if (value != null) {
            predicates.add(PredicateUtil.lessThanOrEqualTo(criteriaBuilder, root, fieldName, value));
        }
        return this;
    }

    public List<Predicate> generate() {
        return predicates;
    }

    public Predicate[] generateArray() {
        return predicates.toArray(new Predicate[0]);
    }

    @SuppressWarnings("unchecked")
    public CriteriaQuery<E> toTypedQuery() {
        criteriaQuery.where(predicates.toArray(new Predicate[0]));
        return (CriteriaQuery<E>) criteriaQuery;
    }

    public Predicate and() {
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

}
