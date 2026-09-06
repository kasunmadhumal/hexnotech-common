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

package com.hexnotech.commons.monitoring.logs;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.hexnotech.commons.type.generic.SafeProcessor;

import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Aspect
public class HexnotechLoggerAspect {

    @Around("@annotation(hexnotechLogger)")
    public Object logMethod(ProceedingJoinPoint joinPoint, HexnotechLogger hexnotechLogger) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        String tag = hexnotechLogger.value().isEmpty() ? "" : " [" + hexnotechLogger.value() + "]";

        String params = parseParams(signature, joinPoint.getArgs());

        long start = System.currentTimeMillis();
        log.info("START {}.{}{} - params: [{}]", className, methodName, tag, params);

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("END {}.{}{} - completed in {}ms", className, methodName, tag, elapsed);
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("ERROR {}.{}{} - failed after {}ms - {}: {}",
                    className, methodName, tag, elapsed, ex.getClass().getSimpleName(), ex.getMessage(), ex);

            if (hexnotechLogger.onError() == ErrorMode.SILENT) {
                return null;
            }
            throw ex;
        }
    }

    private String parseParams(MethodSignature signature, Object[] args) {
        String[] paramNames = signature.getParameterNames();

        if (args != null && args.length > 0 && paramNames != null) {
            return IntStream.range(0, args.length)
                    .mapToObj(i -> {
                        String name = i < paramNames.length ? paramNames[i] : "arg" + i;
                        String argStr = SafeProcessor.of(() -> String.valueOf(args[i])).get(() -> "<toString failed>");
                        return name + "=" + argStr;
                    })
                    .collect(Collectors.joining(", "));
        } else {
            return "";
        }
    }

}
