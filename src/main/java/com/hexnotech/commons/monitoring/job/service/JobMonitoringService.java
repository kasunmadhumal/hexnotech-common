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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Contract for persisting scheduler job execution records.
 * The default implementation uses JPA. Consumer services can provide
 * a custom bean of this type to override the default behaviour.
 */
public interface JobMonitoringService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveJobExecution(String jobName, LocalDateTime startTime, LocalDateTime endTime, JobExecutionStatus status);
}
