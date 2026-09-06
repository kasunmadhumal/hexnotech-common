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

package com.hexnotech.commons.security.carrier;

import com.hexnotech.commons.util.HexnotechUtil;

import java.util.Collections;
import java.util.Set;

/**
 * ThreadLocal holder for the current request's allowed carrier (carrier) codes.
 *
 * Flow:
 *  1. {@link CarrierAuthorizationAspect} resolves allowed carriers from the user token
 *     and stores them here.
 *  2. {@link HibernateCarrierFilterAspect} reads from here and enables the Hibernate
 *     session filter so every repository query is automatically scoped.
 *  3. Both aspects clear the context in a finally block to avoid leaking state
 *     across requests (especially important in thread-pool environments).
 */
public final class CarrierSecurityContext {

    private static final ThreadLocal<Set<String>> ALLOWED_CARRIERS = new ThreadLocal<>();

    private CarrierSecurityContext() {}

    public static void setAllowedCarriers(Set<String> carriers) {
        ALLOWED_CARRIERS.set(Collections.unmodifiableSet(carriers));
    }

    public static Set<String> getAllowedCarriers() {
        return HexnotechUtil.nvlList(ALLOWED_CARRIERS.get(), Collections.emptySet());
    }

    public static void clear() {
        ALLOWED_CARRIERS.remove();
    }
}