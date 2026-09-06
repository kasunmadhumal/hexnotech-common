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

/**
 * Defines the type of operation for carrier filtering.
 */
public enum OperationType {
    /** Read operation - allows returning all authorized carriers if none specified */
    READ,

    /** Create operation - requires carrier to be specified */
    CREATE,

    /** Update operation - requires carrier to be specified */
    UPDATE,

    /** Delete operation - requires carrier to be specified */
    DELETE
}