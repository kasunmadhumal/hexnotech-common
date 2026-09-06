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

package com.hexnotech.commons.security.carrier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthorizeCarriers {

    /**
     * Single privilege key without trailing dot or carrier suffix.
     * Use this for the common case of checking one privilege.
     *
     * Example:
     *   privilege = "aeroOps.flightWatch.view"
     *
     * Cannot be used together with privileges[].
     * If both are empty, an IllegalArgumentException is thrown at runtime.
     */
    String privilege() default "";

    /**
     * Multiple privilege keys for cases where access should be granted
     * if the user has a carrier under ANY of the listed privileges (union).
     *
     * Example:
     *   privileges = {"aeroOps.flight.flight.cancel", "aeroOps.flight.reinstate"}
     *
     * Cannot be used together with privilege().
     * If both are empty, an IllegalArgumentException is thrown at runtime.
     */
    String[] privileges() default {};

    /**
     * SpEL expression evaluated against method parameters, yielding either a
     * String (single carrier) or a Collection of String (multiple carriers).
     *
     * Parameters are bound by their Java name (#request, etc.) when the
     * -parameters compiler flag is active, and always by index (#arg0, #arg1).
     *
     * Examples:
     *   "#request.carrierCodeList"    — repeated proto field (List)
     *   "#request.carrierCode"        — singular proto field
     *   "@flightService.getCarrierCodeVal(#request.flightReference)"
     *                                 — derived via Spring bean
     */
    String carrierExpression();

    /**
     * Whether this is a READ, CREATE, UPDATE, or DELETE operation.
     * Defaults to READ.
     */
    OperationType operationType() default OperationType.READ;
}