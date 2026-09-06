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

import java.util.Optional;

public class FeatureEvaluatorService {

    private final FeatureFlagHolder featureFlagHolder;
    private final FeatureFlagContextSupplier contextSupplier;

    public FeatureEvaluatorService(FeatureFlagContextSupplier featureFlagContextSupplier) {
        this.contextSupplier = featureFlagContextSupplier;
        this.featureFlagHolder = new FeatureFlagHolder(featureFlagContextSupplier.getFeatureFlags());
    }

    public boolean isFeatureEnabledMissingTrue(String tenant, String carrier, String featureName) {
        return isFeatureEnabled(true, tenant, carrier, featureName);
    }

    public boolean isFeatureEnabled(boolean isMissingTrue, String tenant, String carrier, String featureName) {
        Optional<Boolean> carrierFeatureStatus = featureFlagHolder.carrierFeatureStatus(carrier, featureName);
        Optional<Boolean> tenantFeatureStatus = featureFlagHolder.tenantFeatureStatus(tenant, featureName);
        Optional<Boolean> globalFeatureStatus = featureFlagHolder.globalFeatureStatus(featureName);
        return carrierFeatureStatus.or(() -> tenantFeatureStatus)
                .or(() -> globalFeatureStatus)
                .orElse(isMissingTrue);
    }

    public boolean isCarrierFeatureEnabledMissingTrue(String carrier, String featureName) {
        return isCarrierFeatureEnabled(true, carrier, featureName);
    }

    public boolean isCarrierFeatureEnabled(boolean isMissingTrue, String carrier, String featureName) {
        return featureFlagHolder.carrierFeatureStatus(carrier, featureName).orElse(isMissingTrue);
    }


    public boolean isTenantFeatureEnabledMissingTrue(String tenant, String featureName) {
        return isTenantFeatureEnabled(true, tenant, featureName);
    }

    public boolean isTenantFeatureEnabled(boolean isMissingTrue, String tenant, String featureName) {
        return featureFlagHolder.tenantFeatureStatus(tenant, featureName).orElse(isMissingTrue);
    }

    public boolean isGloballyFeatureEnabledMissingTrue(String featureName) {
        return isGloballyFeatureEnable(true, featureName);
    }

    public boolean isGloballyFeatureEnable(boolean isMissingTrue, String featureName) {
        return featureFlagHolder.globalFeatureStatus(featureName).orElse(isMissingTrue);
    }
    
    public String getCurrentTenant() {
        return contextSupplier.getCurrentTenant();
    }

}
