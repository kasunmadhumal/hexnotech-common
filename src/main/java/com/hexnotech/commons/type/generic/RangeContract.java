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

import com.hexnotech.commons.exception.HexnotechValidationException;

public interface RangeContract<E extends Comparable<? super E>> {

    E getFrom();

    E getTo();

    default boolean isWithin(E value) {
        if (value == null) {
            return false;
        }
        return this.getFrom().compareTo(value) <= 0 && this.getTo().compareTo(value) >= 0;
    }

    default boolean isWithin(E otherFrom, E otherTo) {
        try {
            return isWithin(of(otherFrom, otherTo));
        } catch (HexnotechValidationException ex) {
            return false;
        }
    }

    default boolean isWithin(RangeContract<E> other) {
        if (other == null) {
            return false;
        }
        return other.getFrom().compareTo(this.getFrom()) <= 0 && other.getTo().compareTo(this.getTo()) >= 0;
    }

    default boolean isOverlap(E otherFrom, E otherTo) {
        return isOverlap(of(otherFrom, otherTo));
    }

    default boolean isOverlap(RangeContract<E> other) {
        if (this.getFrom() == null || this.getTo() == null || other.getFrom() == null || other.getTo() == null) {
            return false;
        }
        return this.getFrom().compareTo(other.getTo()) <= 0 && other.getFrom().compareTo(this.getTo()) <= 0;
    }

    default boolean isRangeEqual(E otherFrom, E otherTo) {
        return isRangeEqual(of(otherFrom, otherTo));
    }

    default boolean isRangeEqual(RangeContract<E> other) {

        if (other != null) {
            E from = this.getFrom();
            E to = this.getTo();
            E othersFrom = other.getFrom();
            E otherTo = other.getTo();
            boolean fromEqual = (from == null && othersFrom == null)
                    || (from != null && othersFrom != null && from.compareTo(othersFrom) == 0);
            boolean toEqual = (to == null && otherTo == null)
                    || (to != null && otherTo != null && to.compareTo(otherTo) == 0);
            return fromEqual && toEqual;
        } else {
            return false;
        }
    }

    static <E extends Comparable<? super E>> RangeContract<E> of(E from, E to) {
        return new RangeContract<E>() {
            @Override
            public E getFrom() {
                return from;
            }

            @Override
            public E getTo() {
                return to;
            }
        };
    }

}