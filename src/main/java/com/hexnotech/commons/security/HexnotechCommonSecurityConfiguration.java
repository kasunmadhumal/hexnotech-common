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

package com.hexnotech.commons.security;

import com.hexnotech.commons.security.carrier.CarrierAuthorizationAspect;
import com.hexnotech.commons.security.carrier.HibernateCarrierFilterAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Registers carrier security beans when imported via @EnableHexnotechCommonSecurity.
 *
 * Never picked up by component scan — only activated by explicit @Import
 * through the @EnableHexnotechCommonSecurity annotation.
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class HexnotechCommonSecurityConfiguration {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public CarrierAuthorizationAspect carrierAuthorizationAspect() {
        return new CarrierAuthorizationAspect();
    }

    @Bean
    public HibernateCarrierFilterAspect hibernateCarrierFilterAspect() {
        return new HibernateCarrierFilterAspect(entityManager);
    }
}