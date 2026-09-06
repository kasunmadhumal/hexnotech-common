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

package com.hexnotech.commons.monitoring.job.config;

import com.hexnotech.commons.monitoring.job.aspect.JobMonitoringAspect;
import com.hexnotech.commons.monitoring.job.repository.JobExecutionRepository;
import com.hexnotech.commons.monitoring.job.service.JobMonitoringService;
import com.hexnotech.commons.monitoring.job.service.JobMonitoringServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@Configuration
@EnableAspectJAutoProxy
public class JobMonitoringAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JobMonitoringService.class)
    public JobMonitoringService jobMonitoringService(JobExecutionRepository repository) {
        return new JobMonitoringServiceImpl(repository);
    }

    @Bean
    public JobMonitoringAspect jobMonitoringAspect(JobMonitoringService jobMonitoringService) {
        return new JobMonitoringAspect(jobMonitoringService);
    }
}
