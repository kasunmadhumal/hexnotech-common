/*
 * Copyright(C) 2025 ACCELaero
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

package com.hexnotech.commons.security.carrier;

import com.hexnotech.commons.exception.HexnotechUnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.core.annotation.Order;

import jakarta.persistence.EntityManager;
import java.util.Set;

/**
 * Aspect 2 of 2 in the carrier security pipeline.
 *
 * Not annotated with {@literal @}Component — registered only when consuming application
 * uses {@literal @}EnableHexnotechCommonSecurity.
 *
 * Enables the Hibernate session filter "carrierFilter" so that standard JPA
 * queries (JPQL, Spring Data derived queries, EntityManager.find) are
 * automatically scoped to allowed carriers.
 *
 * Requires an active Hibernate session at the point this aspect runs.
 * A {@literal @}Transactional annotation on the calling method (or on the repository base class)
 * must be present with a lower aspect order than this aspect (order = 2), so that
 * the transaction — and therefore the session — is already open when this aspect fires.
 * {@literal @}EnableTransactionManagement(order = 0) in the consuming application satisfies this.
 *
 * For native SQL and Criteria API queries, read from CarrierSecurityContext directly:
 *   predicates.add(root.get("carrierCode").in(CarrierSecurityContext.getAllowedCarrierList()));
 */
@Slf4j
@Aspect
@Order(2)
@RequiredArgsConstructor
public class HibernateCarrierFilterAspect {

    private final EntityManager entityManager;

    @Around("@annotation(filterCarriers)")
    public Object applyCarrierFilter(ProceedingJoinPoint joinPoint, FilterCarriers filterCarriers)
            throws Throwable {

        Set<String> allowedCarriers = CarrierSecurityContext.getAllowedCarriers();

        if (allowedCarriers.isEmpty()) {
            log.error("CarrierSecurityContext is empty at @FilterCarriers boundary — @AuthorizeCarriers must run first.");
            throw new HexnotechUnauthorizedException("Access denied.");
        }

        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter(CarrierSecurityConstant.Filter.FILTER_NAME);
        filter.setParameterList(CarrierSecurityConstant.Filter.PARAM_NAME, allowedCarriers);
        log.debug("Hibernate carrier filter enabled with carriers: {}", allowedCarriers);

        try {
            return joinPoint.proceed();
        } finally {
            session.disableFilter(CarrierSecurityConstant.Filter.FILTER_NAME);
            log.debug("Hibernate carrier filter disabled");
        }
    }
}