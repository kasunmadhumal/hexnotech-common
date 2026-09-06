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

package com.hexnotech.commons.monitoring.job.aspect;

import com.hexnotech.commons.monitoring.job.annotation.MonitorJob;
import com.hexnotech.commons.monitoring.job.constants.JobExecutionStatus;
import com.hexnotech.commons.monitoring.job.exception.HexnotechScheduleJobFailureException;
import com.hexnotech.commons.monitoring.job.service.JobMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.time.LocalDateTime;


@Aspect
@Slf4j
@RequiredArgsConstructor
public class JobMonitoringAspect {

    private final JobMonitoringService jobMonitoringService;

    @Around("@annotation(monitorJob)")
    public Object monitorJobExecution(ProceedingJoinPoint joinPoint, MonitorJob monitorJob) throws Throwable {

        final String jobName = monitorJob.jobName();
        final LocalDateTime startTime = LocalDateTime.now();
        JobExecutionStatus status = JobExecutionStatus.SUCCESS;

        log.info("Job monitoring started for: {}", jobName);

        try {
            final Object result = joinPoint.proceed();
            log.info("Job completed successfully: {}", jobName);
            return result;

        } catch (HexnotechScheduleJobFailureException e) {
            status = JobExecutionStatus.FAILED;
            return null;

        } catch (Throwable throwable) {
            status = JobExecutionStatus.FAILED;
            throw throwable;

        } finally {
            jobMonitoringService.saveJobExecution(jobName, startTime, LocalDateTime.now(), status);
        }
    }

}