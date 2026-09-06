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

package com.hexnotech.commons.ff;

import com.hexnotech.commons.exception.HexnotechFeatureDisabledException;
import com.hexnotech.commons.util.HexnotechUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BooleanSupplier;

@Aspect
@Slf4j
public class FeatureEvaluatorAspect {

    private final FeatureEvaluatorService featureEvaluatorService;

    public FeatureEvaluatorAspect(FeatureEvaluatorService featureEvaluatorService) {
        this.featureEvaluatorService = featureEvaluatorService;
    }

    @Around(
            "@annotation(com.hexnotech.commons.ff.FeatureEvaluate) || " +
                    "@within(com.hexnotech.commons.ff.FeatureEvaluate)"
    )
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        Optional<FeatureEvaluate> featureEvaluate = Optional.ofNullable(method.getAnnotation(FeatureEvaluate.class))
                .or(() -> Optional.ofNullable(joinPoint.getTarget().getClass().getAnnotation(FeatureEvaluate.class)));
        boolean byPass = featureEvaluate.map(FeatureEvaluate::byPass).orElse(false);
        BooleanSupplier shouldProcess = () -> featureEvaluate.map(this::shouldProcess).orElse(true);
        
        if (byPass || shouldProcess.getAsBoolean()) {
            return joinPoint.proceed();
        } else {
            String featureName = featureEvaluate.map(FeatureEvaluate::featureName).orElse("");
            String disabledMessage = featureEvaluate.map(FeatureEvaluate::disabledMessage).orElse("");
            if (!HexnotechUtil.isTrimmedEmpty(disabledMessage)) {
                throw new HexnotechFeatureDisabledException(disabledMessage);
            }
            log.warn("Feature '{}' is disabled. Skipping execution of method '{}'", featureName, method.getName());
            return null;
        }

    }

    private boolean shouldProcess(FeatureEvaluate featureEvaluate) {
        String featureName = featureEvaluate.featureName();
        boolean ifMissing = featureEvaluate.ifMissing();
        boolean considerCarrier = featureEvaluate.considerCarrier();
        String carrierExpression = featureEvaluate.carrierExpression();
        
        boolean globalStatus = featureEvaluatorService.isGloballyFeatureEnable(ifMissing, featureName);
        boolean carrierStatus = false;
        // TODO REFACTORING REQUIRED MOVE CarrierAuthorizationAspect > extractCarriers 
//        if (considerCarrier) {
//            // String carrier = evaluateCarrierExpression(carrierExpression, joinPoint);
//            String carrier = "";
//            carrierStatus = featureEvaluatorService.isCarrierFeatureEnabled(ifMissing, featureName, carrier);
//        }
        String tenant = featureEvaluatorService.getCurrentTenant();
        boolean tenantStatus = featureEvaluatorService.isTenantFeatureEnabled(ifMissing, tenant, featureName);

        return globalStatus || carrierStatus || tenantStatus;
    }

}
