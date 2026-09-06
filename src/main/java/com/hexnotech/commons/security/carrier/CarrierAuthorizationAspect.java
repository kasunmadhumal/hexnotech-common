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

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.hexnotech.commons.exception.HexnotechProcessException;
import com.hexnotech.commons.exception.HexnotechUnauthorizedException;
import com.hexnotech.commons.security.util.PrivilegeUtil;
import com.hexnotech.commons.util.HexnotechUtil;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aspect 1 of 2 in the carrier security pipeline.
 *
 * Not annotated with @Component — registered only when consuming application
 * uses @EnableHexnotechCommonSecurity.
 *
 * Handles both single privilege (privilege = "...") and multiple privileges
 * (privileges = {"...", "..."}) declared on @AuthorizeCarriers.
 * Authorized carriers are the union across all specified privileges.
 */
@Slf4j
@Aspect
@Order(1)
public class CarrierAuthorizationAspect implements ApplicationContextAware {

    private final ExpressionParser parser = new SpelExpressionParser();
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Around("@annotation(authorizeCarriers)")
    public Object authorize(ProceedingJoinPoint joinPoint, AuthorizeCarriers authorizeCarriers)
            throws Throwable {

        try {
            // Step 1: Resolve all privilege keys from either privilege() or privileges()
            List<String> privilegeKeys = resolvePrivilegeKeys(authorizeCarriers);

            // Step 2: Union of authorized carriers across all privilege keys
            Set<String> authorizedCarriers = privilegeKeys.stream()
                    .flatMap(p -> PrivilegeUtil.extractCarriersForPrivilege(p + ".").stream())
                    .collect(Collectors.toSet());

            if (authorizedCarriers.isEmpty()) {
                throw new HexnotechUnauthorizedException(
                        "Access denied: no carrier privileges for " + privilegeKeys);
            }

            // Step 3: Extract requested carriers from method parameters via SpEL
            Object[] args = joinPoint.getArgs();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            StandardEvaluationContext context = buildEvaluationContext(args, signature);
            List<String> requestedCarriers = extractCarriers(authorizeCarriers.carrierExpression(), context);

            // Step 4: Validate / intersect based on operation type
            Set<String> resolvedCarriers = resolveAllowedCarriers(
                    requestedCarriers, authorizedCarriers,
                    authorizeCarriers.operationType(), privilegeKeys);

            log.info("Carrier authorization OK — privileges={}, op={}, authorized={}, requested={}, resolved={}",
                    privilegeKeys, authorizeCarriers.operationType(),
                    authorizedCarriers,
                    requestedCarriers.isEmpty() ? "ALL" : requestedCarriers,
                    resolvedCarriers);

            // Step 5: Publish to ThreadLocal for HibernateCarrierFilterAspect
            CarrierSecurityContext.setAllowedCarriers(resolvedCarriers);

            return joinPoint.proceed(args);

        } finally {
            CarrierSecurityContext.clear();
        }
    }

    /**
     * Resolves the effective list of privilege keys from the annotation.
     *
     * Rules:
     *   - privilege()  set, privileges() empty → use privilege()
     *   - privileges() set, privilege() empty  → use privileges()
     *   - both set                             → throw (ambiguous)
     *   - neither set                          → throw (nothing to check)
     */
    private List<String> resolvePrivilegeKeys(AuthorizeCarriers authorizeCarriers) {

        List<String> privileges = HexnotechUtil.isNotEmpty(authorizeCarriers.privilege()) ?
                List.of(authorizeCarriers.privilege()) : List.of(authorizeCarriers.privileges());
        if (HexnotechUtil.isNotEmpty(privileges)) {
            return privileges;
        } else {
            throw new HexnotechProcessException(
                    "@AuthorizeCarriers: at least one of privilege() or privileges() must be set.");
        }
    }

    /**
     * READ  + none requested → return all authorized (wildcard)
     * READ  + some requested → return intersection
     * WRITE + none requested → throw (carrier must be specified)
     * WRITE + some requested → validate all are authorized, throw if any are not
     */
    private Set<String> resolveAllowedCarriers(List<String> requested, Set<String> authorized,
                                               OperationType operationType, List<String> privileges) {
        boolean noneRequested = requested == null || requested.isEmpty();

        if (operationType == OperationType.READ) {
            if (noneRequested) {
                return new LinkedHashSet<>(authorized);
            }
            // READ with specified carriers: return intersection (silently drop unauthorized carriers)
            return requested.stream()
                    .filter(authorized::contains)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        if (noneRequested) {
            throw new HexnotechUnauthorizedException(
                    String.format("Carrier must be specified for %s operation on privilege(s) %s",
                            operationType, privileges));
        }

        Set<String> unauthorized = requested.stream()
                .filter(carrier -> !authorized.contains(carrier))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!unauthorized.isEmpty()) {
            throw new HexnotechUnauthorizedException(
                    String.format("Access denied: carrier(s) %s not authorized for privilege(s) %s",
                            unauthorized, privileges));
        }

        return new LinkedHashSet<>(requested);
    }

    private StandardEvaluationContext buildEvaluationContext(Object[] args, MethodSignature signature) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        if (applicationContext != null) {
            context.setBeanResolver(new BeanFactoryResolver((BeanFactory) applicationContext));
        }

        for (int i = 0; i < args.length; i++) {
            context.setVariable("arg" + i, args[i]);
        }

        String[] paramNames = signature.getParameterNames();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        return context;
    }

    private List<String> extractCarriers(String expression, StandardEvaluationContext context) {
        try {
            Expression exp = parser.parseExpression(expression);
            Object result = exp.getValue(context);

            if (result == null) return Collections.emptyList();

            if (result instanceof Collection) {
                return ((Collection<?>) result).stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList());
            }

            if (result instanceof String) {
                String s = (String) result;
                return s.isBlank() ? Collections.emptyList() : Collections.singletonList(s);
            }

            log.warn("Unexpected type from carrier expression '{}': {}", expression, result.getClass().getName());
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to evaluate carrier expression '{}'", expression, e);
            throw new IllegalArgumentException("Invalid carrier expression: " + expression, e);
        }
    }
}