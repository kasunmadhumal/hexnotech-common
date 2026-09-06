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

package com.hexnotech.commons.type.generic;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class SafeProcessor<E> {

    private final Supplier<E> supplier;

    public static <E> SafeProcessor<E> of(Supplier<E> supplier) {
        return new SafeProcessor<>(supplier);
    }

    public void handle() {
        getOrNull();
    }

    public void handle(Consumer<Throwable> exceptionConsumer) {
        get(exceptionConsumer);
    }

    public E getOrNull() {
        return get(throwable -> {}).orElse(null);
    }

    public E getOrNull(Consumer<Throwable> exceptionConsumer) {
        return get(exceptionConsumer).orElse(null);
    }

    public E  get(Supplier<E> defaultValueSupplier) {
        return get(throwable -> {}).orElseGet(defaultValueSupplier);
    }

    public Optional<E> get(Consumer<Throwable> exceptionConsumer) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Exception exception) {
            exceptionConsumer.accept(exception);
            return Optional.empty();
        }
    }
}
