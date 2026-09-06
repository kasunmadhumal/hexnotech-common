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

import lombok.Getter;

import com.hexnotech.commons.exception.HexnotechValidationException;

@Getter
public class Range<E extends Comparable<? super E>> implements RangeContract<E> {

    private E from;
    private E to;

    public Range(E from, E to) {
        if (from == null || to == null || from.compareTo(to) > 0) {
            throw new HexnotechValidationException("`from` must be less than or equal to `to`");
        }
        this.from = from;
        this.to = to;
    }

    public static <E extends Comparable<? super E>> Range<E> of(E from, E to) {
        return new Range<>(from, to);
    }

    public static <E extends Comparable<? super E>> Range<E> of(E single) {
        return new Range<>(single, single);
    }

    private void extend(E param) {
        extend(param, param);
    }

    public void extend(E newFrom, E newTo) {
        prepone(newFrom);
        postpone(newTo);
    }

    public void prepone(E newFrom) {
        if (null != newFrom && newFrom.compareTo(this.from) < 0) {
            this.from = newFrom;
        }
    }

    public void postpone(E newTo) {
        if (null != newTo && newTo.compareTo(this.to) > 0) {
            this.to = newTo;
        }
    }

}
