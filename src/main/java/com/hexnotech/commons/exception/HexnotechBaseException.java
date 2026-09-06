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

package com.hexnotech.commons.exception;

import lombok.Getter;
import com.hexnotech.commons.constants.Status;

@Getter
public class HexnotechBaseException extends RuntimeException {

    private final Status status;

    public HexnotechBaseException(Status status, String message) {
        super(message);
        this.status = status;
    }

    public HexnotechBaseException(Status status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public static HexnotechBaseException by(Status status, String message) {
        return new HexnotechBaseException(status, message);
    }

    public static HexnotechBaseException by(Status status, String message, Throwable cause) {
        return new HexnotechBaseException(status, message, cause);
    }

}
