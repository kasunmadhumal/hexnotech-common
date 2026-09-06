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

package com.hexnotech.commons.type.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.hexnotech.commons.util.HexnotechUtil;

import org.springframework.data.domain.Sort;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class HexnotechSort {

    private String field;
    private SortOrder order = SortOrder.ASC;

    public Sort.Order toSort() {
        return SortOrder.ASC == order ? Sort.Order.asc(field) : Sort.Order.desc(field);
    }

    public boolean valid() {
        return HexnotechUtil.isNotEmpty(field);
    }
}
