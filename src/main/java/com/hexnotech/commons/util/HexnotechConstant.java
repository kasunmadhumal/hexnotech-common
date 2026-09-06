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

package com.hexnotech.commons.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class HexnotechConstant {

    public static final class Value {
        public static final String ACTIVE = "ACTIVE";
        public static final String INACTIVE = "INACTIVE";
    }

    @UtilityClass
    public static final class Symbol {
        public static final String EMPTY = "";
        public static final String COMMA = ",";
        public static final String DOT = ".";
        public static final String COLON = ":";
        public static final String DASH = "-";
        public static final String SLASH = "/";
    }

    @UtilityClass
    public static final class DateTimeFormat {
        public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
        public static final String DD_MM_YYYY_DASH = "dd-MM-yyyy";
        public static final String YYYY_MM_DD = "yyyy-MM-dd";
        public static final String HH_MM = "HH:mm";
        public static final String HH_MM_SS = "HH:mm:ss";
        public static final String DD_MM_YYYY_SLASH = "dd/MM/yyyy";
        public static final String UI_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    }
}
