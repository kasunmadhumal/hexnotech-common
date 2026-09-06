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

package com.hexnotech.commons.security.util;

import lombok.experimental.UtilityClass;

import com.hexnotech.commons.exception.HexnotechValidationException;
import com.hexnotech.commons.constants.User;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for privilege and carrier-related operations.
 *
 * @author Your Team
 * @since 1.0
 */
@UtilityClass
public final class PrivilegeUtil {

    /**
     * Gets the currently authenticated user.
     *
     * @return the current user
     */
    public static User getCurrentUser() {
        return null; //need to update (UserUtil.getCurrentUser())
    }

    /**
     * Gets all privileges for the current user.
     *
     * @return list of privilege strings
     */
    public static List<String> getCurrentUserPrivileges() {
        return getCurrentUser().getPrivileges();
    }

    /**
     * Extracts carrier codes from user privileges matching the given prefix.
     *
     * Example: For prefix "aeroOps.flightWatch.view." and privileges
     * ["aeroOps.flightWatch.view.G9", "aeroOps.flightWatch.view.3L"],
     * returns ["G9", "3L"]
     *
     * @param prefix the privilege prefix (should end with a dot)
     * @return set of carrier codes the user has access to
     */
    public static Set<String> extractCarriersForPrivilege(String prefix) {
        return getCurrentUserPrivileges().stream()
                .filter(privilege -> privilege.startsWith(prefix))
                .map(privilege -> privilege.substring(prefix.length())) // Extract carrier after prefix
                .filter(carrier -> !carrier.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Checks if the current user has a specific privilege.
     *
     * @param privilege the privilege to check
     * @return true if user has the privilege
     */
    public static boolean hasPrivilege(String privilege) {
        return getCurrentUserPrivileges().contains(privilege);
    }

    /**
     * Checks if the current user has access to a specific carrier for a privilege.
     *
     * @param privilegePrefix the privilege prefix (e.g., "aeroOps.flightWatch.view.")
     * @param carrierCode the carrier code to check (e.g., "G9")
     * @return true if user has access to the carrier
     */
    public static boolean hasAccess(String privilegePrefix, String carrierCode) {
        return extractCarriersForPrivilege(privilegePrefix).contains(carrierCode);
    }

    /**
     * Extracts the carrier code from a flight reference.
     * Flight references are formatted as: [2-char carrier code]_[flight number]
     *
     * Examples: "G9_3057305", "3L_79006", "AA_1234"
     *
     * @param flightReference the flight reference string
     * @return the 2-character carrier code
     * @throws HexnotechValidationException if the flight reference is invalid
     */
    public static String getCarrierByReference(String flightReference) {
        if (flightReference == null || flightReference.length() < 3) {
            throw new HexnotechValidationException(
                    "Invalid flight reference: must not be null and must have at least 3 characters");
        }

        String carrierCode = flightReference.substring(0, 2);

        if (!carrierCode.matches("[A-Z0-9]{2}")) {
            throw new HexnotechValidationException(
                    "Invalid carrier code in flight reference: " + flightReference);
        }

        return carrierCode;
    }
}