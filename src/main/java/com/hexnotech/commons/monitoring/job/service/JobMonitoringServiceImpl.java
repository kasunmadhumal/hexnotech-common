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

package com.hexnotech.commons.monitoring.job.service;

import com.hexnotech.commons.monitoring.job.constants.JobExecutionStatus;
import com.hexnotech.commons.monitoring.job.model.JobExecutionMonitor;
import com.hexnotech.commons.monitoring.job.repository.JobExecutionRepository;
import com.hexnotech.commons.service.BaseHexnotechJpaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
public class JobMonitoringServiceImpl extends BaseHexnotechJpaService<JobExecutionMonitor, Long>
        implements JobMonitoringService {

    private final JobExecutionRepository jobExecutionRepository;

    public JobMonitoringServiceImpl(JobExecutionRepository jobExecutionRepository) {
        super(jobExecutionRepository);
        this.jobExecutionRepository = jobExecutionRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveJobExecution(String jobName, LocalDateTime startTime, LocalDateTime endTime,
                                 JobExecutionStatus status) {
        try {
            JobExecutionMonitor monitor = JobExecutionMonitor.builder()
                    .jobName(jobName)
                    .startTime(startTime)
                    .endTime(endTime)
                    .executionStatus(status)
                    .build();
            jobExecutionRepository.save(monitor);

        } catch (Exception e) {
            log.error("Failed to save job execution monitoring for job: {}", jobName, e);
        }
    }
}
