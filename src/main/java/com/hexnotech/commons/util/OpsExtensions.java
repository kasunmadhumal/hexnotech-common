/*
 *
 *  * Copyright(C) 2025 ACCELaero
 *  * All rights reserved.
 *  * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF
 *  * Information Systems Associates (pvt) Ltd.
 *  *
 *  * This copy of the Source Code is intended for Information Systems Associates (pvt) Ltd's internal
 *  * use only and is intended for view by persons duly authorized by the management of
 *  * Information Systems Associates (pvt) Ltd. No part of this file may be reproduced or distributed
 *  * in any form or by any means without the written approval of the Management of
 *  * Information Systems Associates (pvt) Ltd.
 *
 */

package com.hexnotech.commons.util;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class OpsExtensions {
    public static <K, T, R> R checkForDefault(Map<K, T> map, K key, Function<T, R> mapper, R defaulValue) {
        return Optional.ofNullable(map.get(key)).map(mapper).orElse(defaulValue);
    }
}
