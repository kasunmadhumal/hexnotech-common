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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

public class FeatureFlagConfig {

    @ConditionalOnMissingBean
    @Bean
    public FeatureFlagContextSupplier featureFlagSupplier() {
        return new FeatureFlagContextSupplier() {
            @Override public String getCurrentTenant() {
                return null;
            }

            @Override public List<FeatureFlag> getFeatureFlags() {
                return null;
            }
        };
    }

    @Bean
    public FeatureEvaluatorService featureEvaluator(FeatureFlagContextSupplier featureFlagContextSupplier) {
        return new FeatureEvaluatorService(featureFlagContextSupplier);
    }


    @Bean
    public FeatureEvaluatorAspect featureEvaluatorAspect(FeatureEvaluatorService featureEvaluatorService) {
        return new FeatureEvaluatorAspect(featureEvaluatorService);
    }
}
