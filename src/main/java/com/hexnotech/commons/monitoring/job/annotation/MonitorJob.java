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

package com.hexnotech.commons.monitoring.job.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a scheduler method for automatic execution monitoring.
 * Apply to any {@code @Scheduled} method. The aspect will persist
 * start time, end time, and execution status to the DB automatically.
 *
 * <pre>{@code
 * @Scheduled(cron = "0 * * * * *")
 * @MonitorJob(jobName = JobNames.ACARS_MESSAGE_QUEUE_CONSUMER)
 * public void processAcarsMessages() { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitorJob {
    String jobName();
}
