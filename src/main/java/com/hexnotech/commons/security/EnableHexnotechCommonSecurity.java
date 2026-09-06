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

package com.hexnotech.commons.security;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables Hexnotech's common carrier/carrier security model.
 *
 * Add this to any {@literal @}Configuration class in the consuming application to activate
 * carrier filtering via {@literal @}AuthorizeCarriers and {@literal @}FilterCarriers.
 *
 * Usage:
 *   {@literal @}Configuration
 *   {@literal @}EnableHexnotechCommonSecurity
 *   public class AppSecurityConfig { }
 *
 * Without this annotation, no aspects are registered even if the library
 * is on the classpath. Projects that only need utilities are unaffected.
 *
 * Works regardless of whether the consuming project has {@literal @}ComponentScan
 * covering com.hexnotech.* — uses {@literal @}Import so component scan is not required.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(HexnotechCommonSecurityConfiguration.class)
public @interface EnableHexnotechCommonSecurity {
}