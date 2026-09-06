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

public interface TriPair<X, Y, Z> {

    X getFirst();
    Y getSecond();
    Z getThird();

    static <X, Y, Z> TriPair<X, Y, Z> of(X first, Y second, Z third) {
        return new TriPair<>() {
            @Override
            public X getFirst() {
                return first;
            }

            @Override
            public Y getSecond() {
                return second;
            }

            @Override
            public Z getThird() {
                return third;
            }
        };
    }
}
