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

import com.hexnotech.commons.exception.HexnotechValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.Supplier;

@UtilityClass
public class HexnotechDateTimeUtil {


    // ------------------ Start Local Date --------

    public static Optional<LocalDate> parseDate(String value) {
        return parseDate(value, HexnotechConstant.DateTimeFormat.YYYY_MM_DD);
    }

    public static Optional<LocalDate> parseUIDate(String value) {
        return parseDate(value, HexnotechConstant.DateTimeFormat.YYYY_MM_DD_HH_MM_SS);
    }

    public static Optional<LocalDate> parseDate(String value, String pattern) {
        try {
            return Optional.of(parseDateOrThrow(value, pattern));
        } catch (HexnotechValidationException e) {
            return Optional.empty();
        }
    }

    public static LocalDate parseDateOrThrow(String value, String pattern) {
        return parseDateOrThrow(value, pattern, () -> "Date is invalid. Expected format: " + pattern);
    }

    public static LocalDate parseCommonDateOrThrow(String value, String fieldName) {
        String pattern = HexnotechConstant.DateTimeFormat.YYYY_MM_DD;
        return parseDateOrThrow(value, pattern, () -> fieldName + " is invalid. Expected format: " + pattern);
    }

    public static LocalDate parseDateOrThrow(String value, String fieldName, String pattern) {
        return parseDateOrThrow(value, pattern, () -> fieldName + " is invalid. Expected format: " + pattern);
    }

    public static LocalDate parseDateOrThrow(String value, String pattern, Supplier<String> exceptionMessageSupplier) {
        try {
            return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException | IllegalArgumentException e) {
            throw new HexnotechValidationException(exceptionMessageSupplier.get());
        }
    }

    // ------------------ End Local Date --------

    // ------------------ Start Local Time --------

    public static Optional <LocalTime> parseTime(String value) {
        return parseTime(value, HexnotechConstant.DateTimeFormat.HH_MM);
    }

    public static Optional <LocalTime> parseTime(String value, String pattern) {
        try {
            return Optional.of(parseTimeOrThrow(value, pattern));
        } catch (HexnotechValidationException e) {
            return Optional.empty();
        }
    }

    public static LocalTime parseTimeOrThrow(String value, String pattern) {
        return parseTimeOrThrow(value, pattern, () -> "Time is invalid. Expected format: " + pattern);
    }

    public static LocalTime parseCommonTimeOrThrow(String value, String fieldName) {
        String pattern = HexnotechConstant.DateTimeFormat.HH_MM;
        return parseTimeOrThrow(value, pattern, () -> fieldName + " is invalid. Expected format: " + pattern);
    }

    public static LocalTime parseTimeOrThrow(String value, String fieldName, String pattern) {
        return parseTimeOrThrow(value, pattern, () -> fieldName + " is invalid. Expected format: " + pattern);
    }

    public static LocalTime parseTimeOrThrow(String value, String pattern, Supplier<String> exceptionMessageSupplier) {
        try {
            return LocalTime.parse(value, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            throw new HexnotechValidationException(exceptionMessageSupplier.get());
        }
    }
    // ------------------ End Local Time --------

    // ------------------ Start Local DateTime --------
    public static LocalDateTime parseDateTimeOrThrow(String dateTime, String fieldName) {
        String pattern = HexnotechConstant.DateTimeFormat.UI_DATE_FORMAT;
        return parseDateTimeOrThrow(dateTime, pattern, () -> fieldName + " is invalid. Expected format: " + pattern);

    }

    public static LocalDateTime parseDateTimeOrThrow(String dateTime, String pattern,
                                                     Supplier<String> exceptionMessageSupplier) {
        try {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            throw new HexnotechValidationException(exceptionMessageSupplier.get());
        }
    }

    // --------------------- Start Time Conversions --------
    public static int parseHhMmToMinutes(String time) {
        String formatedTime = HexnotechUtil.nvl(time).trim();
        if (!formatedTime.contains(HexnotechConstant.Symbol.COLON)) {
            formatedTime = formatedTime + HexnotechConstant.Symbol.COLON + "00";
        }
        LocalTime localTime = parseTimeOrThrow(formatedTime, HexnotechConstant.DateTimeFormat.HH_MM);
        return localTime.getHour() * 60 + localTime.getMinute();
    }
    // ---------------------- End Time Conversions --------
}
