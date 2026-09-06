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

package com.hexnotech.commons.type.generic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RangeMap<E extends Comparable<? super E>, V> {

    private final Map<RangeContract<E>, V> rangeMap = new HashMap<>();

    public void put(RangeContract<E> range, V value) {
        rangeMap.put(range, value);
    }

    public V get(E key) {
        for (Map.Entry<RangeContract<E>, V> entry : rangeMap.entrySet()) {
            if (entry.getKey().isWithin(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public List<V> getAllWithin(RangeContract<E> range) {
        return rangeMap.entrySet().stream()
                .filter(entry -> entry.getKey().isWithin(range))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public V getFirst() {
        return rangeMap.values().stream().findFirst().orElse(null);
    }

}

