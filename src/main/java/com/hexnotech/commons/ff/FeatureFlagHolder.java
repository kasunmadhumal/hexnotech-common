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

import com.hexnotech.commons.util.HexnotechUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FeatureFlagHolder {

    private Map<String, Map<String, Boolean>> tenantFeatureMap = new HashMap<>();
    private Map<String, Map<String, Boolean>> carrierFeatureMap = new HashMap<>();
    private Map<String, Boolean> globalFeatureMap = new HashMap<>();

    public FeatureFlagHolder(List<FeatureFlag> featureFlags) {
        HexnotechUtil.nvlToList(featureFlags).stream()
                .filter(feature -> HexnotechUtil.isNotEmpty(feature.getFeatureName()))
                .forEach(flag -> {
                    globalFeatureMap.put(flag.getFeatureName(), flag.isEnabled());
                    if (HexnotechUtil.isNotEmpty(flag.getTenant())) {
                        tenantFeatureMap.computeIfAbsent(
                                flag.getTenant(),
                                key -> new HashMap<>()
                        ).put(flag.getFeatureName(), flag.isEnabled());
                    }
                    if (HexnotechUtil.isNotEmpty(flag.getCarrier())) {
                        carrierFeatureMap.computeIfAbsent(
                                flag.getCarrier(),
                                key -> new HashMap<>()
                        ).put(flag.getFeatureName(), flag.isEnabled());
                    }
                });
    }

    public Optional<Boolean> globalFeatureStatus(String featureName) {
        return Optional.ofNullable(globalFeatureMap.get(featureName));
    }

    public Optional<Boolean> tenantFeatureStatus(String tenant, String featureName) {
        return Optional.ofNullable(tenantFeatureMap.get(tenant))
                .map(featureMap -> featureMap.get(featureName));
    }

    public Optional<Boolean> carrierFeatureStatus(String carrier, String featureName) {
        return Optional.ofNullable(carrierFeatureMap.get(carrier))
                .map(featureMap -> featureMap.get(featureName));
    }

}
