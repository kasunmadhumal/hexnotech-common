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

package com.hexnotech.commons.type.async;

import com.hexnotech.commons.exception.HexnotechProcessException;

import java.util.concurrent.Future;
import java.util.function.Supplier;

public class AsyncDataProvider<T> {

    private Future<T> reference;
    private final Supplier<T> supplier;
    private boolean isCompleted;

    public AsyncDataProvider(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    T process() throws Exception {
        try {
            return supplier.get();
        } catch (Exception exception) {
            throw new HexnotechProcessException("Error processing async task", exception);
        } finally {
            isCompleted = true;
        }
    }

    void setReference(Future<?> reference) {
        this.reference = (Future<T>) reference;
    }

    public T get() {
        try {
            return reference.get();
        } catch (Exception ex) {
            throw new HexnotechProcessException("Exception while async supplier", ex);
        }
    }
}
