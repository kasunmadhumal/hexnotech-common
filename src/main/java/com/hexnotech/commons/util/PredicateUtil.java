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

package com.hexnotech.commons.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.hexnotech.commons.jpa.entity.BaseEntity;
import com.hexnotech.commons.jpa.type.PredicationResolver;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PredicateUtil {

    public static <E> Specification<E> defaultSpecification() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    public static <E> Specification<E> equalSpecification(String filedName, Object value) {
        return (root, criteriaQuery, criteriaBuilder) -> equalPredicate(criteriaBuilder, root, filedName, value);
    }

    public static <Req, E extends BaseEntity<?>> PredicationResolver<Req, E> defaultTruePredicate() {
        return (req, searchFilter, root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    public static <E> Predicate equalPredicate(CriteriaBuilder criteriaBuilder, Root<E> root,
                                               String fieldName, Object value) {
        return criteriaBuilder.equal(root.get(fieldName), value);
    }

    public static <E> Predicate preLike(CriteriaBuilder criteriaBuilder, Root<E> root,
                                        String fieldName, String value) {
        return like(criteriaBuilder, root, fieldName, "%" + HexnotechUtil.nvl(value));
    }

    public static <E> Predicate postLike(CriteriaBuilder criteriaBuilder, Root<E> root,
                                         String fieldName, String value) {
        return like(criteriaBuilder, root, fieldName, HexnotechUtil.nvl(value) + "%");
    }

    public static <E> Predicate likeWildCard(CriteriaBuilder criteriaBuilder, Root<E> root,
                                             String fieldName, String value) {
        String formatedLikeStr = "%" + HexnotechUtil.nvl(value) + "%";
        return like(criteriaBuilder, root, fieldName, formatedLikeStr);
    }

    public static <E> Predicate like(CriteriaBuilder criteriaBuilder, Root<E> root, String fieldName, String value) {
        return criteriaBuilder.like(root.get(fieldName), HexnotechUtil.nvlTrim(value, "%%"));
    }

    public static <E, V extends Comparable<? super V>> Predicate between(CriteriaBuilder criteriaBuilder, Root<E> root,
                                                                         String fieldName, V from, V to) {
        return criteriaBuilder.between(root.get(fieldName), from, to);
    }

    public static <E, V extends Comparable<? super V>> Predicate greaterThanOrEqualTo(
            CriteriaBuilder criteriaBuilder,
            Root<E> root,
            String fieldName,
            V value) {

        return criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), value);
    }
    
    public static <E, V extends Comparable<? super V>> Predicate greaterThan(
            CriteriaBuilder criteriaBuilder,
            Root<E> root,
            String fieldName,
            V value) {

        return criteriaBuilder.greaterThan(root.get(fieldName), value);
    }

    public static <E, V extends Comparable<? super V>> Predicate lessThanOrEqualTo(
            CriteriaBuilder criteriaBuilder,
            Root<E> root,
            String fieldName,
            V value) {

        return criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), value);
    }

    public static <E, V extends Comparable<? super V>> Predicate lessThan(
            CriteriaBuilder criteriaBuilder,
            Root<E> root,
            String fieldName,
            V value) {

        return criteriaBuilder.lessThan(root.get(fieldName), value);
    }

    public static <E> Predicate modelActiveInTimeRangePredicate(CriteriaBuilder criteriaBuilder, Root<E> root,
                                                                String modelStartDateField, String modelEndDateField,
                                                                Timestamp givenStartDate, Timestamp givenEndDate) {

        Predicate[] predicates = Stream.of(
                        Optional.ofNullable(givenEndDate)
                                .map(end -> criteriaBuilder.lessThanOrEqualTo(root.get(modelStartDateField), end)),
                        Optional.ofNullable(givenStartDate)
                                .map(start -> criteriaBuilder.greaterThanOrEqualTo(root.get(modelEndDateField), start))
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toArray(Predicate[]::new);

        return criteriaBuilder.and(predicates);
    }

    public static <T, V> Predicate inOrAll(CriteriaBuilder criteriaBuilder, Root<T> root, String attributePath,
                                           Collection<V> values) {
        return HexnotechUtil.isNotEmpty(values) ? root.get(attributePath).in(values) : criteriaBuilder.conjunction();
    }

    public static <T, V> Predicate inOrNone(CriteriaBuilder criteriaBuilder, Root<T> root, String attributePath,
                                            Collection<V> values) {
        return HexnotechUtil.isNotEmpty(values) ? root.get(attributePath).in(values) : criteriaBuilder.disjunction();
    }

}
